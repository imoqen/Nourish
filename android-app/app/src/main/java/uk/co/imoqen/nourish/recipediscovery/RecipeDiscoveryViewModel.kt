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
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import javax.inject.Inject
import uk.co.imoqen.nourish.common.saveImage
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmRecipe
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
    private val recipeRepository: RecipeRepository
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

                // if the user submitted empty string as prompt, and this string is NOT optional for this call type, return error:
                if ((type == LlmRequestType.RECIPE || type == LlmRequestType.DISHES_FROM_INGREDIENTS || type == LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH) && userPrompt == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_BLANK_PROMPT))
                    setAwaitingResponse(false)
                    return@launch
                }

                // make it into a request:
                val request = LlmRequest(userPrompt)

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
                                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Valid dishes could not be generated. Please enter something differents."))
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
                    llmRecipe
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

}