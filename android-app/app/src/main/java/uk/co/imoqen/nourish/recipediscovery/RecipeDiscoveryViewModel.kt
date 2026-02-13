package uk.co.imoqen.nourish.recipediscovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.LlmRequest
import javax.inject.Inject

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

    /**
     * Makes the API call by forming the request, getting the user token and ensuring it is valid.
     * Then, placing this token in the Bearer Authorization header and the request in the body.
     * Enqueues the call and when a response is received, stores it in the UiState to display in
     * the view.
     * If an error occurs, the error is stored as above and displayed in the view.
     */
    fun makeLlmApiCall() {
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

                apiManager.handleRecipeGeneration(request, tokenResult) { response ->
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
    }

}