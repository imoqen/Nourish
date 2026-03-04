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
import uk.co.imoqen.nourish.common.FilterActions
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import javax.inject.Inject
import uk.co.imoqen.nourish.common.saveImage
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection
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
    private val filterActions: FilterActions,
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

    fun setUserRequest(userRequest: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                userRequest = userRequest
            )
        }
    }

    fun setAwaitingResponse(awaitingResponse: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                awaitingResponse = awaitingResponse
            )
        }
    }

    fun setSavedImageFilename(savedImageFilename: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                savedImageFilename = savedImageFilename
            )
        }
    }

    fun setUserChoice(userChoice: LlmMode) {
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

    fun setDishes(dishes: LlmDishesProposed) {
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
            currentState.copy(
                filterDisplayState = filterDisplayState
            )
        }
    }

    fun setRecipeTags(recipeTags: List<TagSelection>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeTags = recipeTags
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
            currentState.copy(
                createdTagName = createdTagName
            )
        }
    }

    fun setCreatedTagColour(createdTagColour: NourishColour?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                createdTagColour = createdTagColour
            )
        }
    }

    fun setTagCreationError(tagCreationError: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                tagCreationError = tagCreationError
            )
        }
    }

    fun setShowConfirmationDialog(showConfirmationDialog: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showConfirmationDialog = showConfirmationDialog
            )
        }
    }

    fun setTagToDelete(tagToDelete: Tag?) {
        Log.i("tag delete", "tag to delete: ${tagToDelete}")
        mutableUiState.update { currentState ->
            currentState.copy(
                tagToDelete = tagToDelete
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

    fun setAllowUserInput(allowUserInput: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                allowUserInput = allowUserInput
            )
        }
    }

    fun setUserSentRequest(userSentRequest: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                userSentRequest = userSentRequest
            )
        }
    }

    fun createTag(
        description: String,
        colour: NourishColour?
    ) {
        viewModelScope.launch() {
            runCatching {
                if (description == "") {
                    setTagCreationError("Please enter a description.")
                    return@launch
                }
                if (description.length > 27) {
                    setTagCreationError("The description is too long. It must be less than 27 characters.")
                    return@launch
                }
                if (colour == null) {
                    setTagCreationError("Please select a colour.")
                    return@launch
                }
                // check that tag doesnt already exist
                if (mutableUiState.value.recipeTags != null &&
                    mutableUiState.value.recipeTags!!.map{
                        it.tag.identifier.uppercase()
                    }.contains(description.uppercase().replace(" ", "-"))) {
                    setTagCreationError("Please ensure that the tag description is unique; it cannot be the same as another tag.")
                    return@launch
                }
                filterActions.createTag(description, colour)
            }
                .onSuccess {
                    // when the tag has been successfully made,
                    getAllTagsForUser() // update the tags as a new one now exists
                    closeTagCreation()
                }
                .onFailure {
                    setTagCreationError("An error occurred: the tag could not be created.")
                }
        }
    }

    fun deleteTag() {
        viewModelScope.launch() {
            runCatching {
                val tagToDelete = mutableUiState.value.tagToDelete
                if (tagToDelete == null) {
                    Log.i("tag delete", "null")
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Tag could not be deleted."))
                    return@launch
                }
                Log.i("tag delete", "not null")
                filterActions.deleteTag(tagToDelete)
            }
                .onSuccess {
                    // after tag is deleted, refresh the available tags:
                    getAllTagsForUser()
                }

        }
    }

    // closes the tag creation area
    fun closeTagCreation() {
        setFilterDisplayState(FilterState.APPLY) // go back to apply filter menu
        setCreatedTagColour(null)
        setCreatedTagName("")
        setTagCreationError("")
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
        setAwaitingResponse(true)
        viewModelScope.launch() {
            runCatching {
                // get the user prompt ("what would you like to cook?" field)
                val userPrompt = mutableUiState.value.userRequest

                // Get filters selected and the meal type:
                var chosenNutritionalFilters = mutableUiState.value.recipeTags?.filter { it.selected }?.joinToString(", ") // only gets the selected filters and puts them into a string
                if (chosenNutritionalFilters == null) {
                    chosenNutritionalFilters = ""
                }

                var allNutritionalFilters = mutableUiState.value.recipeTags?.joinToString(", ") { it.tag.identifier }
                if (allNutritionalFilters == null) {
                    allNutritionalFilters = ""
                }

                val dishType = mutableUiState.value.selectedRecipeType?.title?: "Breakfast, Lunch, Dinner, Dessert, Side, Drink" // if none provided, all.


                // if the user submitted empty string as prompt, and this string is NOT optional for this call type, return error:
                if ((type == LlmRequestType.RECIPE || type == LlmRequestType.DISHES_FROM_INGREDIENTS || type == LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH) && userPrompt == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_BLANK_PROMPT))
                    setAwaitingResponse(false)
                    return@launch
                }

                if ((type == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE || type == LlmRequestType.DISHES_FROM_IMAGE || type == LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH) && imageFilename == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("A photo must be uploaded to proceed."))
                    setAwaitingResponse(false)
                    return@launch
                }

                // make it into a request:
                val request = LlmRequest(userPrompt, chosenNutritionalFilters, allNutritionalFilters, dishType)

                // validate the user's token:
                val tokenResult = authenticationManager.getUserToken()
                if (tokenResult == null) {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("1" + ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    setAwaitingResponse(false)
                    return@launch
                }

                apiManager.makeRetrofitCall(
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
                                    setAwaitingResponse(false)
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
                                setAwaitingResponse(false)
                            }
                        }
                    },
                ) { response ->

                    viewModelScope.launch() {
                        if (response == null || response.llmRecipe == null) {
                            // Error message in Snackbar:
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("2" + ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                            setAwaitingResponse(false)
                            return@launch
                        }
                        if (response.string.startsWith("An error occurred.") || response.string.startsWith("Error:")) {
                            // Response error in Snackbar:
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(response.string))
                            setAwaitingResponse(false)
                            return@launch
                        }
                        if (response.llmRecipe.recipeNotPossible) {
                            // user inputted invalid prompt / image; recipe cannot be generated.
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("A valid recipe could not be generated. Please try another dish."))
                            setAwaitingResponse(false)
                            return@launch
                        }
                        // Response is valid - output in a recipe card to the user
                        setApiResponse(response.string)
                        addRecipe(response.llmRecipe)
                        setAwaitingResponse(false)
                    }
                }
            }
                .onFailure {
                    // error occurred
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("3" + ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    setAwaitingResponse(false)
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
                LlmRequestType.RECIPE_QUERY, LlmRequestType.RECIPE_ALTERATION -> "" // do nothing - TODO in future.
            }
            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(errorMessage))
            setAwaitingResponse(false)
        }
    }

    fun handleImage(
        uri: Uri?,
        context: Context
    ) {
        viewModelScope.launch() {
            runCatching {
                saveImage(
                    uri,
                    context
                ) { filename -> setSavedImageFilename(filename) }
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
            try {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == null) { throw NullPointerException() } // user is null; throw to be handled appropriately
                // Add the recipe to the database:
                recipeRepository.createRecipe(
                    uid,
                    llmRecipe,
                    mutableUiState.value.savedImageFilename
                )
                // If an error occurs, it is caught below:
            } catch (e: Exception) {
                Log.i("RECIPE SAVE", "ERROR: $e")
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "The recipe could not be saved. Please try again, or generate a new recipe."
                ))
            }
        }
    }

    fun getAllTagsForUser() {
        viewModelScope.launch() {
            (filterActions.getAllTagsForUser(
                mutableUiState.value.recipeTags
            ))?.let { updatedTags ->
                setRecipeTags(updatedTags) // only sets if not null
            }
        }
    }

    // only sets if the update is not null.
    fun getAllRecipeTypes() {
        viewModelScope.launch() {
            (filterActions.getAllRecipeTypes())?.let { updatedTypes ->
                Log.i("setting types", "not null: ${updatedTypes}")
                setRecipeTypes(updatedTypes) // only sets if not null
            }
        }
    }

    fun updateSelected(tag: Tag, status: Boolean) {
        viewModelScope.launch() {
            setRecipeTags(
                filterActions.updateSelected(
                    tag,
                    status,
                    mutableUiState.value.recipeTags
                )
            )
        }
    }

}