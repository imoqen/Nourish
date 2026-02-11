package uk.co.imoqen.nourish.recipediscovery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.imoqen.nourish.authentication.AuthenticationManager
import uk.co.imoqen.nourish.constants.ErrorMessage
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.LlmError
import uk.co.imoqen.nourish.network.LlmRequest
import uk.co.imoqen.nourish.network.LlmResponse
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
    private val authenticationManager: AuthenticationManager
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

    fun setApiResponseError(apiResponseError: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                apiResponseError = apiResponseError
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

    /**
     * Makes the API call by forming the request, getting the user token and ensuring it is valid.
     * Then, placing this token in the Bearer Authorization header and the request in the body.
     * Enqueues the call and when a response is received, stores it in the UiState to display in
     * the view.
     * If an error occurs, the error is stored as above and displayed in the view.
     */
    fun makeLlmApiCall() {
        // The request simply sends the name of the meal, and the Cloud Run service deals with it appropriately.
        val request = LlmRequest(mutableUiState.value.userRequest)
        viewModelScope.launch() {
            runCatching {
                val tokenResult = authenticationManager.getUserToken()
                if (tokenResult == null) {
                    setApiResponseError(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE)
                    return@launch
                }
                val call = apiService.generateRecipe(request, "Bearer ${tokenResult.token}")
                call.enqueue(object : Callback<LlmResponse> {
                    // OnResponse occurs when a response has been received from the server. could be error.
                    override fun onResponse(
                        call: Call<LlmResponse>, response: Response<LlmResponse>
                    ) {
                        if (response.isSuccessful) {
                            val post = response.body()
                            if (post != null) {
                                // Extracts the text and updates the UiState
                                setApiResponse(
                                    "${post.title}:" +
                                            "\n\nIngredients: \n${post.ingredients} " +
                                            "\n\nIngredient Amounts: \n${post.ingredient_amounts} " +
                                            "\n\nIngredient Units: \n${post.ingredient_amount_units} " +
                                            "\n\nMethod: \n${post.method}" +
                                            "\n\nServing Size: \n${post.servings} " +
                                            "\n\nType of Meal: \n${post.meal_type} "
                                )
                                setApiResponseError("")
                            }
                        } else {
                            // Extracts the error json from the error string using the LlmError
                            // template, updates the UiState
                            val error = response.errorBody()?.string()
                            val errorJson = Gson().fromJson(error, LlmError::class.java)
                            setApiResponseError(errorJson.error)
                            setApiResponse("")
                        }
                    }
                    // OnFailure occurs when no internet/timeout
                    override fun onFailure(call: Call<LlmResponse>, t: Throwable) {
                        setApiResponseError(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE)
                        setApiResponse("")
                    }
                })
            }
                .onFailure {
                    setApiResponseError(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE)
                    setApiResponse("")
                }
        }
    }

}