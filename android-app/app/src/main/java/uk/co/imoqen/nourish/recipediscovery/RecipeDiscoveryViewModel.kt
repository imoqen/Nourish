package uk.co.imoqen.nourish.recipediscovery

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import javax.inject.Inject
import uk.co.imoqen.nourish.common.saveImage
import uk.co.imoqen.nourish.common.saveMipmap
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.tag.FilterState
import uk.co.imoqen.nourish.tag.TagCreationException
import uk.co.imoqen.nourish.tag.TagSheetAction
import java.io.File
import java.lang.NullPointerException
import kotlin.onSuccess


/**
 * ViewModel for RecipeDiscovery.
 * Uses the ApiService to make a call to the Google Cloud Run Service.
 * This uses retrofit2 to make the request.
 * The following tutorials were followed to assist with this:
 *       https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet#6
 *       https://medium.com/@imkuldeepsinghrai/api-calls-with-retrofit-in-android-kotlin-a-comprehensive-guide-e049e19deba9
 * Furthermore, dependent on the authenticationManager to get the token of the logged in user, for
 * authentication.
 * @param apiService - dependent on the ApiService to make the API call
 * @param authenticationManager - dependent on the AuthenticationManager to access user's token
 */
@HiltViewModel
class RecipeDiscoveryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authenticationManager: AuthenticationManager,
    private val apiManager: ApiManager,
    private val uiEventBus: UiEventBus,
    private val recipeRepository: RecipeRepository,
    private val recipeActions: RecipeActions,
): ViewModel() {
    private val mutableUiState = MutableStateFlow(RecipeDiscoveryUiState())
    val uiState: StateFlow<RecipeDiscoveryUiState> = mutableUiState.asStateFlow()

    // Setters for Ui State values:
    fun setApiResponse(apiResponse: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                apiResponse = apiResponse
            )
        }
    }

    fun setLlmGeneratedRecipe(llmGeneratedRecipe: LlmRecipe?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                llmGeneratedRecipe = llmGeneratedRecipe
            )
        }
    }

    fun setUserRequest(userRequest: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                userRequest = userRequest
            )
        }
    }

    fun setSavedImageMipmap(savedImageMipmap: Mipmap) {
        mutableUiState.update { currentState ->
            currentState.copy(
                savedImageMipmap = savedImageMipmap
            )
        }
    }

    fun setUserChoice(userChoice: LlmMode?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                userChoice = userChoice
            )
        }
    }

    fun setRequestType(requestType: LlmRequestType) {
        mutableUiState.update { currentState ->
            currentState.copy(
                requestType = requestType
            )
        }
    }

    fun setPlaceholderText(placeholderText: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                placeholderText = placeholderText
            )
        }
    }

    fun setDishes(dishes: LlmDishesProposed?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                dishes = dishes
            )
        }
    }

    fun setDisplayImage(displayImage: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                displayImage = displayImage
            )
        }
    }

    fun setFilterDisplayState(filterDisplayState: FilterState) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    filterDisplayState = filterDisplayState
                )
            )
        }
    }

    fun setRecipeTags(recipeTags: List<TagSelection>?) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    recipeTags = recipeTags
                )
            )
        }
    }

    fun setRecipeTypes(recipeTypes: List<RecipeType>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeTypes = recipeTypes
            )
        }
    }

    fun setCreatedTagName(createdTagName: String) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    createdTagName = createdTagName
                )
            )
        }
    }

    fun setCreatedTagColour(createdTagColour: NourishColour?) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    createdTagColour = createdTagColour
                )
            )
        }
    }

    fun setTagCreationError(tagCreationError: String) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    tagCreationError = tagCreationError
                )
            )
        }
    }

    fun setShowTagConfirmationDialog(showTagConfirmationDialog: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    showTagConfirmationDialog = showTagConfirmationDialog
                )
            )
        }
    }

    fun setShowRestartConfirmationDialog(showRestartConfirmationDialog: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showRestartConfirmationDialog = showRestartConfirmationDialog
            )
        }
    }

    fun setRecipeSaved(recipeSaved: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeSaved = recipeSaved
            )
        }
    }

    fun setTagToDelete(tagToDelete: Tag?) {
        Log.i("tag delete", "tag to delete: ${tagToDelete}")
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    tagToDelete = tagToDelete
                )
            )
        }
    }

    fun setSelectedRecipeType(selectedRecipeType: RecipeType?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                selectedRecipeType = selectedRecipeType
            )
        }
    }

    fun setDisplayRecipeTypeSelector(displayRecipeTypeSelector: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                displayRecipeTypeSelector = displayRecipeTypeSelector
            )
        }
    }

    fun setVisible(visible: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                visible = visible
            )
        }
    }

    fun setChosenDish(chosenDish: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                chosenDish = chosenDish
            )
        }
    }

    fun setLlmChatStage(llmChatStage: LlmChatStage) {
        mutableUiState.update { currentState ->
            currentState.copy(
                llmChatStage = llmChatStage
            )
        }
    }

    fun updateToInputStageGivenOptions(
        userChoice: LlmMode?,
        placeholderText: String,
        requestType: LlmRequestType,
    ) {
        setUserChoice(userChoice) // for the display of the ui
        setPlaceholderText(placeholderText) // for the message response
        setRequestType(requestType) // for the llm request api call

        if (userChoice == LlmMode.FROM_DESCRIPTION) { // no image can be uploaded in this mode
            // ensure no image can be uploaded:
            setSavedImageMipmap(Mipmap("", ""))
        }

        // allow user input:
        setLlmChatStage(LlmChatStage.USER_INPUT)
    }

    /**
     * Makes the API call by forming the request, getting the user token and ensuring it is valid.
     * Then, placing this token in the Bearer Authorization header and the request in the body.
     * Enqueues the call and when a response is received, stores it in the UiState to display in
     * the view.
     * If an error occurs, the error is stored as above and displayed in the view.
     * type determines if it should do from image or generate recipe text
     */
    fun makeLlmApiCall(
        type: LlmRequestType,
        imageFilename: String = "", // optional
        filesDir: File? = null, // optional
    ) {
        setLlmChatStage(LlmChatStage.AWAITING_LLM_RESPONSE)
        viewModelScope.launch() {
            runCatching {
                val userPrompt =
                    // GENERATE POSSIBLE DISHES
                    if (type == LlmRequestType.DISHES_FROM_INGREDIENTS || type == LlmRequestType.DISHES_FROM_IMAGE || type == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE ) {
                        mutableUiState.value.userRequest // additional detail along with their inputted ingredients or image
                    }
                    // GENERATE RECIPE FROM DISH CHOICE
                    else if (mutableUiState.value.userChoice != LlmMode.FROM_DESCRIPTION) {
                        // input to generate the recipe must be from the dish chosen
                        mutableUiState.value.chosenDish
                    }
                    // GENERATE RECIPE FROM DESCRIPTION
                    else {
                        // input to generate the recipe is simply the request
                        mutableUiState.value.userRequest
                    }

                // Get filters selected and the meal type:
                var chosenNutritionalFilters =
                    mutableUiState.value.tagSheetState.recipeTags?.filter { it.selected }
                        ?.joinToString(", ") { it.tag.identifier } // only gets the selected filters and puts them into a string
                if (chosenNutritionalFilters == null) {
                    chosenNutritionalFilters = "[]" // signifies empty list
                }
                Log.i("selected tags: ", chosenNutritionalFilters)


                var allNutritionalFilters = mutableUiState.value.tagSheetState.recipeTags?.joinToString(", ") { it.tag.identifier }
                if (allNutritionalFilters == null) {
                    allNutritionalFilters = "[]" // signifies empty list
                }

                val dishType = mutableUiState.value.selectedRecipeType?.title?: "Breakfast, Lunch, Dinner, Dessert, Side, Drink" // if none provided, all.


                // if the user submitted empty string as prompt, and this string is NOT optional for this call type, return error:
                if ((type == LlmRequestType.RECIPE || type == LlmRequestType.DISHES_FROM_INGREDIENTS || type == LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH) && userPrompt == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_BLANK_PROMPT))
                    // error occurred; go back to the USER_INPUT stage (MAY NOT WORK: TEST THIS).
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                    return@launch
                }

                if ((type == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE || type == LlmRequestType.DISHES_FROM_IMAGE || type == LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH) && imageFilename == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("A photo must be uploaded to proceed."))
                    // error occurred; go back to the USER_INPUT stage (MAY NOT WORK: TEST THIS).
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                    return@launch
                }

                // make it into a request:
                val request = LlmRequest(userPrompt, chosenNutritionalFilters, allNutritionalFilters, dishType)

                // validate the user's token:
                val tokenResult = authenticationManager.getUserToken()
                if (tokenResult == null) {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("1" + ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    // error occurred; go back to the USER_INPUT stage (MAY NOT WORK: TEST THIS).
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                    return@launch
                }

                apiManager.makeRetrofitLlmRequestCall(
                    type,
                    request,
                    tokenResult,
                    imageFilename, // will just be "" and null if not used, so optional in that sense. handled appropriately.
                    filesDir,
                    onDishesProposed = { dishesJson ->
                        // safe because only occurs if not null. if it is null, handles by onResponse which is below.
                        if (dishesJson != null) {

                            viewModelScope.launch() {
                                if (dishesJson.dishNotPossible) {
                                    // user inputted invalid prompt / image; recipe cannot be generated.
                                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Valid dishes could not be generated. Please enter something different."))
                                    // error occurred; go back to the USER_INPUT stage (MAY NOT WORK: TEST THIS).
                                    setLlmChatStage(LlmChatStage.USER_INPUT)
                                    return@launch
                                }

                                setDishes(
                                    LlmDishesProposed(
                                        ingredientsIdentified = dishesJson.ingredientsIdentified,
                                        firstMostLikelyDish = dishesJson.firstMostLikelyDish,
                                        secondMostLikelyDish = dishesJson.secondMostLikelyDish,
                                        thirdMostLikelyDish = dishesJson.thirdMostLikelyDish,
                                        fourthMostLikelyDish = dishesJson.fourthMostLikelyDish,
                                        dishNotPossible = false
                                    )
                                )

                                // valid: set to the dish selection stage
                                setLlmChatStage(LlmChatStage.USER_SELECT_DISH)
                            }
                        }
                    },
                ) { response ->

                    viewModelScope.launch() {
                        if (response == null || response.llmRecipe == null) {
                            // Error message in Snackbar:
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("2" + ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                            setLlmChatStage(LlmChatStage.USER_INPUT)
                            return@launch
                        }
                        if (response.string.startsWith("An error occurred.") || response.string.startsWith("Error:")) {
                            // Response error in Snackbar:
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(response.string))
                            setLlmChatStage(LlmChatStage.USER_INPUT)
                            return@launch
                        }
                        if (response.llmRecipe.recipeNotPossible) {
                            // user inputted invalid prompt / image; recipe cannot be generated.
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("A valid recipe could not be generated. Please try another dish."))
                            setLlmChatStage(LlmChatStage.USER_INPUT)
                            return@launch
                        }
                        // Response is valid - output in a recipe card to the user
                        setApiResponse(response.string)
                        setLlmGeneratedRecipe(response.llmRecipe)

                        // valid: set to the dish selection stage
                        setLlmChatStage(LlmChatStage.USER_VIEW_RECIPE)
                    }
                }
            }
                .onFailure {
                    // error occurred
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("3" + ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                }
        }
    }

    fun displayBlankInputError(
        requestType: LlmRequestType
    ) {
        viewModelScope.launch() {
            val errorMessage = when (requestType) {
                LlmRequestType.RECIPE -> "Please enter a dish or description before sending the request."
                LlmRequestType.DISHES_FROM_INGREDIENTS -> "Please enter ingredients or an image before sending the request."
                LlmRequestType.DISHES_FROM_IMAGE, LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE, LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH -> "Please upload an image before sending the request."
            }
            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(errorMessage))
            setLlmChatStage(LlmChatStage.USER_INPUT)
        }
    }

    fun handleImage(
        uri: Uri?,
        context: Context
    ) {
        viewModelScope.launch() {
            runCatching {
                saveMipmap(
                    uri,
                    context
                ) { lowQualityFilename, highQualityFilename ->
                    setSavedImageMipmap(Mipmap(lowQualityFilename, highQualityFilename))
                }
            }
                .onSuccess {
                    Log.i("PhotoPicker", "image saved successfully")
                }
                .onFailure { e ->
                    Log.i("PhotoPicker", "image not saved: $e")
                }
        }

    }

    fun addRecipe(llmRecipe: LlmRecipe) {
        viewModelScope.launch() {
            runCatching {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == null) {
                    throw NullPointerException()
                } // user is null; throw to be handled appropriately
                // Add the recipe to the database:
                recipeRepository.createRecipe(
                    // recipe is saved with the higher quality image, which is stored in second place
                    uid, llmRecipe, imagePath = mutableUiState.value.savedImageMipmap.highQualityFilename,
                    alteration = false // not an alteration; it is a new recipe.
                )
            }
                .onSuccess {
                    setRecipeSaved(true)
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The recipe has been saved. To generate a new recipe, press Restart."
                        )
                    )
                }

                .onFailure { e ->
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The recipe could not be saved. Please try again, or generate a new recipe."
                        )
                    )
                    Log.i("RECIPE SAVE", "$e")
                }
        }
    }

    // to restart from fresh to gen a new recipe
    fun restart() {
        setApiResponse("")
        setUserRequest("")
        setChosenDish("")

        setUserChoice(null)
        setDishes(null)

        getAllTagsForUser(reset=true)
        setSelectedRecipeType(null)

        setLlmChatStage(LlmChatStage.USER_SELECT_OPTION) // the starting stage
        setRequestType(LlmRequestType.RECIPE)

        setSavedImageMipmap(Mipmap("",""))
        setPlaceholderText("")

        setLlmGeneratedRecipe(null)
        setRecipeSaved(false)
    }

    fun determineUnitPlurality(
        ingredientAmount: Double,
    ): Boolean {
        return recipeActions.determineUnitPlurality(ingredientAmount)
    }

    fun determineIngredientPlurality(
        ingredientAmount: Double,
        ingredientUnit: IngredientUnit
    ): Boolean {
        return recipeActions.determineIngredientPlurality(ingredientAmount, ingredientUnit)
    }

    fun formatIngredientAmount(
        ingredientAmount: Double,
        roundToNearestQuarter: Boolean = true
    ): String {
        return recipeActions.formatIngredientAmount(ingredientAmount, roundToNearestQuarter)
    }

    // only used for llmingredient as they are dealt with differently.
    fun formatIngredientText(
        ingredient: LlmIngredient
    ): String {
        runCatching {
            return recipeActions.formatIngredientText(ingredient)
        }
            .onFailure {
                viewModelScope.launch {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("The recipe could not be generated. Please try again."))
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                    return@launch
                }
            }
        return "" // invalid if reached here
    }

    // Tag Sheet Logic: exposes the logic present in RecipeActions for tag selection.
    // no real logic is duplicated, only "thin logic" required to expose the actual methods. this is ok

    fun getAllTagsForUser(
        reset: Boolean = false // to reset to get all tags as not selected; by default set to false
    ) {
        viewModelScope.launch() {
            (recipeActions.getAllTagsForUser(
                mutableUiState.value.tagSheetState.recipeTags, reset
            ))?.let { updatedTags ->
                setRecipeTags(updatedTags) // only sets if not null
            }
        }
    }

    // only sets if the update is not null.
    fun getAllRecipeTypes() {
        viewModelScope.launch() {
            (recipeActions.getAllRecipeTypes())?.let { updatedTypes ->
                setRecipeTypes(updatedTypes) // only sets if not null
            }
        }
    }

    fun updateSelected(tag: Tag, status: Boolean) {
        viewModelScope.launch() {
            setRecipeTags(
                recipeActions.updateSelected(tag, status, mutableUiState.value.tagSheetState.recipeTags)
            )
        }
    }

    fun createTag(
        description: String,
        colour: NourishColour?
    ) {
        viewModelScope.launch() {
            runCatching {
                recipeActions.createTag(description, colour, mutableUiState.value.tagSheetState.recipeTags)
            }
                .onSuccess {
                    // when the tag has been successfully made,
                    getAllTagsForUser() // update the tags as a new one now exists
                    closeTagCreation()
                }
                .onFailure { e ->
                    when (e) {
                        is TagCreationException -> setTagCreationError(
                            e.message ?: "The tag could not be created."
                        )
                        else -> setTagCreationError("The tag could not be created.")
                    }
                }
        }
    }

    fun deleteTag() {
        viewModelScope.launch() {
            runCatching {
                recipeActions.deleteTag(mutableUiState.value.tagSheetState.tagToDelete)
            }
                .onSuccess {
                    // after tag is deleted, refresh the available tags:
                    getAllTagsForUser()
                }

        }
    }

    // closes the tag creation area. Purely for updating the UI state and does not use any shared action.
    fun closeTagCreation() {
        setFilterDisplayState(FilterState.APPLY) // go back to apply filter menu
        setCreatedTagColour(null)
        setCreatedTagName("")
        setTagCreationError("")
    }

    // the actions for the tag sheet:
    fun onTagSheetAction(action: TagSheetAction) {
        when (action) {
            is TagSheetAction.SetFilterDisplayState -> setFilterDisplayState(action.displayState)
        }
    }

}