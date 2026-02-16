package uk.co.imoqen.nourish.recipediscovery

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.round
import androidx.core.graphics.scale
import uk.co.imoqen.nourish.common.compressImage
import uk.co.imoqen.nourish.common.saveImage
import uk.co.imoqen.nourish.network.request.LlmRequestType
import java.io.File
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
    private val uiEventBus: UiEventBus
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

    /**
     * Makes the API call by forming the request, getting the user token and ensuring it is valid.
     * Then, placing this token in the Bearer Authorization header and the request in the body.
     * Enqueues the call and when a response is received, stores it in the UiState to display in
     * the view.
     * If an error occurs, the error is stored as above and displayed in the view.
     */
    /*fun makeLlmApiCall() {
        // The request simply sends the name of the meal, and the Cloud Run service deals with it appropriately.
        setAwaitingResponse(true)
        viewModelScope.launch() {
            // User submitted empty string as prompt; send error.
            if (mutableUiState.value.userRequest == "") {
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_BLANK_PROMPT))
                setAwaitingResponse(false)
                return@launch
            }
            // Otherwise proceed
            val request = LlmRequest(mutableUiState.value.userRequest)

            runCatching {
                val tokenResult = authenticationManager.getUserToken()
                if (tokenResult == null) {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    setAwaitingResponse(false)
                    return@launch
                }

                apiManager.makeRetrofitCall(
                    LlmRequestType.RECIPE,
                    request,
                    tokenResult
                ) { response ->
                        viewModelScope.launch() {
                            if (response == null) {
                                // Error message in Snackbar:
                                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                                setAwaitingResponse(false)
                                return@launch
                            }
                            if (response.startsWith("An error occurred.") || response.startsWith("Internal Server Error:")) {
                                // Response error in Snackbar:
                                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(response))
                                setAwaitingResponse(false)
                                return@launch
                            }
                            // Response is valid - output in a recipe card to the user
                            setApiResponse(response)
                            setAwaitingResponse(false)
                        }
                    }

            }
                .onFailure {
                    // error occurred
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    setAwaitingResponse(false)
                }
        }
    }*/

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
                if (type == LlmRequestType.RECIPE && userPrompt == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_BLANK_PROMPT))
                    setAwaitingResponse(false)
                    return@launch
                }

                // make it into a request:
                val request = LlmRequest(userPrompt)

                // validate the user's token:
                val tokenResult = authenticationManager.getUserToken()
                if (tokenResult == null) {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    setAwaitingResponse(false)
                    return@launch
                }

                apiManager.makeRetrofitCall(
                    type,
                    request,
                    tokenResult,
                    imageFilename, // will just be "" and null if not used, so optional in that sense. handled appropriately.
                    filesDir
                ) { response ->

                    viewModelScope.launch() {
                        if (response == null) {
                            // Error message in Snackbar:
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                            setAwaitingResponse(false)
                            return@launch
                        }
                        if (response.startsWith("An error occurred.") || response.startsWith("Error:")) {
                            // Response error in Snackbar:
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(response))
                            setAwaitingResponse(false)
                            return@launch
                        }
                        // Response is valid - output in a recipe card to the user
                        setApiResponse(response)
                        setAwaitingResponse(false)
                    }
                }
            }
                .onFailure {
                    // error occurred
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    setAwaitingResponse(false)
                }
        }
    }

    /*fun makeLlmImageApiCall(
        imageFilename: String,
        filesDir: File
    ) {
        // file name to use: compressed-recipe-image-$time.$extension, will be passed in from the view
        viewModelScope.launch() {

            runCatching {
                val request = LlmRequest(mutableUiState.value.userRequest) // currently the "what would you like to cook" field - change this in the future maybe

                val tokenResult = authenticationManager.getUserToken()
            if (tokenResult == null) {
                Log.i("VIEWMODELCALL", "invalid token")
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                setAwaitingResponse(false)
                return@launch
            }
                Log.i("viewmodelcall", "before")

            apiManager.makeRetrofitCall(
                LlmRequestType.DISHES_FROM_IMAGE,
                request,
                tokenResult,
                imageFilename,
                filesDir
            ) { response ->
                Log.i("here", "in here right now")
                viewModelScope.launch() {
                    if (response == null) {
                        Log.i("VIEWMODELCALL", "null response")
                        // Error message in Snackbar:
                        uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                        setAwaitingResponse(false)
                        return@launch
                    }
                    if (response.startsWith("An error occurred.") || response.startsWith("Error:")) {
                        // Response error in Snackbar:
                        uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(response))
                        setAwaitingResponse(false)
                        return@launch
                    }
                    // Response is valid - output in a recipe card to the user
                    setApiResponse(response)
                    setAwaitingResponse(false)
                }
            }
            }
            .onFailure {
                // error occurred
                Log.i("VIEWMODELCALL", "an error occurred - on failure")
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                setAwaitingResponse(false)
            }
        }
    }*/

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
                    makeLlmApiCall(LlmRequestType.DISHES_FROM_IMAGE, uiState.value.savedImageFilename, context.filesDir)
                }
                .onFailure { e ->
                    Log.i("PhotoPicker", "image not saved: $e")
                }
        }

    }

}