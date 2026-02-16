package uk.co.imoqen.nourish.network.apimanager

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.response.LlmError
import uk.co.imoqen.nourish.network.response.LlmRecipesProposed
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.network.response.LlmResponse
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManagerImpl @Inject constructor(
    private val apiService: ApiService
): ApiManager {

    override fun handleRecipeGeneration(
        request: LlmRequest,
        bearerToken: String,
        onResponse: (String?) -> Unit
    ) {
        val call = apiService.generateRecipe(request, "Bearer $bearerToken")
        call.enqueue(object : Callback<LlmRecipe> {
            // OnResponse occurs when a response has been received from the server. could be error.
            override fun onResponse(
                call: Call<LlmRecipe>, response: Response<LlmRecipe>
            ) {
                if (response.isSuccessful) {
                    handleSuccess(response, onResponse)
                } else {
                    handleError(response, onResponse)
                }
            }
            // OnFailure occurs when no internet/timeout
            override fun onFailure(call: Call<LlmRecipe>, t: Throwable) {
                // handled in the view model
                throw t
            }
        })
    }

    // Extracts the error json from the error string using the LlmError
    // template, to update the UiState when called.
    private fun <T> handleError(
        response: Response<T>,
        onResponse: (String?) -> Unit
    ) {
        val error = response.errorBody()?.string()
        val errorJson = Gson().fromJson(error, LlmError::class.java)
        onResponse("Internal Server Error: ${errorJson.error}")
    }

    private fun <T> handleSuccess(
        response: Response<T>,
        onResponse: (String?) -> Unit
    ) {
        val post = response.body()
        when(post) {
            is LlmRecipe -> {
                onResponse(
                    "${post.title}:" +
                            "\n\nIngredients: \n${post.ingredients} " +
                            "\n\nIngredient Amounts: \n${post.ingredient_amounts} " +
                            "\n\nIngredient Units: \n${post.ingredient_amount_units} " +
                            "\n\nMethod: \n${post.method}" +
                            "\n\nServing Size: \n${post.servings} " +
                            "\n\nType of Meal: \n${post.meal_type} "
                )
            }
            is LlmRecipesProposed -> {
                onResponse(post.recipes)
            }
            else -> {
                onResponse(ErrorMessage.API_ERROR_FAILED_TO_GEN)
            }
        }
    }

    /**
     * Sends an image to the LLM and requests 4 potential meals which could be present in the image,
     * most likely being first.
     */
    // with help from https://medium.com/@myofficework000/uploading-files-with-multipart-form-data-in-retrofit-android-f9a4fecba360
    override fun handleFromImage(
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String,
        filesDir: File,
        onResponse: (String?) -> Unit
    ) {
        // Assembling the request with the compressed image jpg present:
        val file = File(filesDir, imageFilename)
        val requestBody = file.asRequestBody("image/jpg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

        // Wrapped in try-catch because multipartBody could be malformed if the user submits
        // an invalid image.
        try {
            val call = apiService.generateFromImage(multipartBody, request, "Bearer $bearerToken")
            call.enqueue(object : Callback<LlmRecipesProposed> {
                // OnResponse occurs when a response has been received from the server. could be error.
                override fun onResponse(
                    call: Call<LlmRecipesProposed>, response: Response<LlmRecipesProposed>
                ) {
                    if (response.isSuccessful) {
                        handleSuccess(response, onResponse)
                    } else {
                        handleError(response, onResponse)
                    }
                }

                // OnFailure occurs when no internet/timeout
                override fun onFailure(call: Call<LlmRecipesProposed>, t: Throwable) {
                    // handled in the view model
                    throw t
                }
            })
        } catch (e: Exception) {
            Log.e("CALL ERROR", "IMAGE INVALID; $e")
            onResponse("Internal Server Error. Please try again later.")
        }
    }
}