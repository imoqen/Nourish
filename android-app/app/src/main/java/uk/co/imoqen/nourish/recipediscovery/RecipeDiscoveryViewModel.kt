package uk.co.imoqen.nourish.recipediscovery

import android.content.Context
import android.net.Uri
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
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import javax.inject.Inject
import uk.co.imoqen.nourish.common.saveMipmap
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.common.exception.TagCreationException
import uk.co.imoqen.nourish.common.tag.TagSheetAction
import java.io.File
import java.lang.NullPointerException
import kotlin.onSuccess

/**
 * ViewModel for RecipeDiscovery.
 * Uses the ApiService to make a call to the Google Cloud Run Service, then saves the recipe in the db.
 * This uses retrofit2 to make the request.
 * The following tutorials were followed to assist with this:
 * Furthermore, dependent on the authenticationManager to get the token of the logged in user, for
 * authentication.
 * @param authenticationManager - The AuthenticationManager to get the user's token to send as Bearer.
 * @param apiManager - The ApiManager to communicate with the Google Cloud function, and thus LLM.
 * @param uiEventBus - The UiEventBus to display alerts / Snackbar messages.
 * @param recipeRepository - The Recipe Repository, to access the Recipe db table.
 * @param recipeActions - The Auxiliary class RecipeActions to access and use these common Recipe actions.
 *
 * @author Imogen Pemberton, 2026
 */
@HiltViewModel
class RecipeDiscoveryViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager,
    private val apiManager: ApiManager,
    private val uiEventBus: UiEventBus,
    private val recipeRepository: RecipeRepository,
    private val recipeActions: RecipeActions,
): ViewModel() {
    private val mutableUiState = MutableStateFlow(RecipeDiscoveryUiState())
    val uiState: StateFlow<RecipeDiscoveryUiState> = mutableUiState.asStateFlow()

    // Setters for Ui State values:

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

    fun setRecipeTypes(recipeTypes: List<RecipeType>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeTypes = recipeTypes
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

    /**
     * Updates the input stage present in the view. There are three different forms of input stage:
     * recipe, from image, and from ingredients. The user can select which they wish to do, and the
     * interface will now be in the 'mode' selected.
     * @param userChoice: the mode (LlmMode) that the user wishes to discover a recipe (recipe,
     *                    from ingredients, or from image).
     * @param placeholderText: the text to display in the user input bar in the current mode
     * @param requestType: the type of request, determining whether an image has been uploaded etc.
     */
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
     * type determines if it should do from image or generate recipe text.
     *
     * The following sources were used to assist with this:
     *   https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet#6
     *   https://medium.com/@imkuldeepsinghrai/api-calls-with-retrofit-in-android-kotlin-a-comprehensive-guide-e049e19deba9
     * @param type - The type of the LLM request
     * @param imageFilename - The filename of the image to send to the LLM
     * @param filesDir - The device's files directory.
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
                    // Generate possible dishes:
                    if (type == LlmRequestType.DISHES_FROM_INGREDIENTS ||
                        type == LlmRequestType.DISHES_FROM_IMAGE ||
                        type == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE ) {
                        // Additional detail, along with their inputted ingredients or image
                        mutableUiState.value.userRequest
                    }
                    // Generate Recipe from Dish choice:
                    else if (mutableUiState.value.userChoice != LlmMode.FROM_DESCRIPTION) {
                        // Input to generate the recipe must be from the dish chosen
                        mutableUiState.value.chosenDish
                    }
                    // Generate Recipe from Description:
                    else {
                        // input to generate the recipe is simply the request
                        mutableUiState.value.userRequest
                    }

                // Get filters selected and the meal type:
                var chosenNutritionalFilters =
                    mutableUiState.value.tagSheetState.recipeTags?.filter { it.selected }
                        ?.joinToString(", ") { it.tag.identifier }
                if (chosenNutritionalFilters == null) {
                    chosenNutritionalFilters = "[]" // signifies empty list
                }
                var allNutritionalFilters = mutableUiState.value.tagSheetState.recipeTags?.joinToString(", ") {
                    it.tag.identifier
                }
                if (allNutritionalFilters == null) {
                    allNutritionalFilters = "[]" // signifies empty list
                }

                // if no dish type was selected, provide all by default.
                val dishType = mutableUiState.value.selectedRecipeType?.title?: "Breakfast, Lunch, Dinner, Dessert, Side, Drink"

                // if the user submitted empty string as prompt, and this string is NOT optional
                // for this call type, return error:
                if ((type == LlmRequestType.RECIPE ||
                            type == LlmRequestType.DISHES_FROM_INGREDIENTS ||
                            type == LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH) && userPrompt == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_BLANK_PROMPT))
                    // Error occurred; go back to the USER_INPUT stage.
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                    return@launch
                }

                if ((type == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE ||
                            type == LlmRequestType.DISHES_FROM_IMAGE ||
                            type == LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH) && imageFilename == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("A photo must be uploaded to proceed."))
                    // Error occurred; go back to the USER_INPUT stage.
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                    return@launch
                }

                // Make it into a request:
                val request = LlmRequest(userPrompt, chosenNutritionalFilters, allNutritionalFilters, dishType)

                // Get the user's token, for validation:
                val tokenResult = authenticationManager.getUserToken()
                if (tokenResult == null) {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    // error occurred; go back to the USER_INPUT stage (MAY NOT WORK: TEST THIS).
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                    return@launch
                }

                apiManager.makeRetrofitLlmRequestCall(
                    type,
                    request,
                    tokenResult,
                    // will just be "" and null if not used, so optional in that sense.
                    imageFilename,
                    filesDir,
                    onError = { errorMessage ->
                        viewModelScope.launch() {
                            // Error message in Snackbar:
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(errorMessage))
                            setLlmChatStage(LlmChatStage.USER_INPUT)
                        }
                    },
                    onDishesProposed = { dishesJson ->
                        // safe because only occurs if not null. if it is null,
                        // handled by onResponse which is below.
                        if (dishesJson != null) {

                            viewModelScope.launch() {
                                if (dishesJson.dishNotPossible) {
                                    // user inputted invalid prompt / image; recipe cannot be generated.
                                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                                        "Valid dishes could not be generated. Please enter something different."
                                    ))
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
                        if (response != null) {
                            if (response.recipeNotPossible) {
                                // user inputted invalid prompt / image; recipe cannot be generated.
                                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                                    "A valid recipe could not be generated. Please try another dish."
                                ))
                                setLlmChatStage(LlmChatStage.USER_INPUT)
                                return@launch
                            }
                            // Response is valid - output in a recipe card to the user
                            setLlmGeneratedRecipe(response)

                            // valid: set to the dish selection stage
                            setLlmChatStage(LlmChatStage.USER_VIEW_RECIPE)
                        }
                    }
                }
            }
                .onFailure {
                    // error occurred
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    resetAfterError()
                    setLlmChatStage(LlmChatStage.USER_INPUT)
                }
        }
    }

    /**
     * Resets the Ui State after an error occurred.
     */
    fun resetAfterError() {
        setDishes(null)
        setChosenDish("")
    }

    /**
     * Displays an error when no input was provided.
     */
    fun displayBlankInputError(
        requestType: LlmRequestType
    ) {
        viewModelScope.launch() {
            val errorMessage = when (requestType) {
                LlmRequestType.RECIPE ->
                    "Please enter a dish or description before sending the request."
                LlmRequestType.DISHES_FROM_INGREDIENTS ->
                    "Please enter ingredients or an image before sending the request."
                LlmRequestType.DISHES_FROM_IMAGE,
                LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE,
                LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH ->
                    "Please upload an image before sending the request."
            }
            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(errorMessage))
            setLlmChatStage(LlmChatStage.USER_INPUT)
        }
    }

    /**
     * Saves the image mipmap using the ImageHandler helper function, catching errors if they occur.
     * @param uri - The uri of the image to display.
     * @param context - The Context, used to display the image.
     */
    fun handleImage(
        uri: Uri?,
        context: Context
    ) {
        viewModelScope.launch() {
            runCatching {
                saveMipmap(uri, context) { lowQualityFilename, highQualityFilename ->
                    setSavedImageMipmap(Mipmap(lowQualityFilename, highQualityFilename))
                }
            }
        }
    }

    /**
     * Adds an LLM-generated Recipe to the database.
     * @param llmRecipe - the LLM-generated Recipe.
     */
    fun addRecipe(llmRecipe: LlmRecipe) {
        viewModelScope.launch() {
            runCatching {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == "") {
                    throw NullPointerException()
                }
                // Add the recipe to the database, with the higher quality image:
                recipeRepository.createRecipe(
                    uid,
                    llmRecipe,
                    imagePath = mutableUiState.value.savedImageMipmap.highQualityFilename,
                    shouldNotScaleIngredients = false // ingredients should be scaled.
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
                }
        }
    }

    /**
     * To restart from the beginning, to generate a new Recipe
     */
    fun restart() {
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

    /**
     * Formats the ingredient text, used for displaying LLM ingredients as they are handled differently.
     * @param ingredient - The LlmIngredient to format.
     * @returns String of the formatted ingredient text.
     */
    fun formatIngredientText(
        ingredient: LlmIngredient
    ): String {
        runCatching {
            return recipeActions.formatIngredientText(ingredient)
        }
        return "" // if invalid
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

    // Only sets if the update is not null.
    fun getAllRecipeTypes() {
        viewModelScope.launch() {
            (recipeActions.getAllRecipeTypes())?.let { updatedTypes ->
                setRecipeTypes(updatedTypes) // only sets if not null
            }
        }
    }

    // Updates the selected tag.
    fun updateSelected(tag: Tag, status: Boolean) {
        viewModelScope.launch() {
            setRecipeTags(
                recipeActions.updateSelected(tag, status, mutableUiState.value.tagSheetState.recipeTags)
            )
        }
    }

    // Creates a Tag.
    fun createTag() {
        viewModelScope.launch() {
            runCatching {
                recipeActions.createTag(
                    mutableUiState.value.tagSheetState.createdTagName,
                    mutableUiState.value.tagSheetState.createdTagColour,
                    mutableUiState.value.tagSheetState.recipeTags)
            }
                .onSuccess {
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

    // Deletes a Tag.
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

    // Closes the tag creation area. Purely for updating the UI state and does not use any shared action.
    fun closeTagCreation() {
        setFilterDisplayState(FilterState.APPLY) // go back to apply filter menu
        setCreatedTagColour(null)
        setCreatedTagName("")
        setTagCreationError("")
    }

    // The actions for the tag sheet:
    fun onTagSheetAction(action: TagSheetAction) {
        when (action) {
            is TagSheetAction.SetFilterDisplayState -> setFilterDisplayState(action.displayState)
            is TagSheetAction.CloseTagCreation -> closeTagCreation()
            is TagSheetAction.CreateTag -> createTag()
            is TagSheetAction.DeleteTag -> deleteTag()
            is TagSheetAction.SetCreatedTagColour -> setCreatedTagColour(action.colour)
            is TagSheetAction.SetCreatedTagName -> setCreatedTagName(action.name)
            is TagSheetAction.SetTagToDelete -> setTagToDelete(action.tag)
            is TagSheetAction.ShowTagConfirmationDialog -> setShowTagConfirmationDialog(action.show)
            is TagSheetAction.UpdateSelected -> updateSelected(action.tag, action.selected)
        }
    }

}