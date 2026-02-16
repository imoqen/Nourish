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
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmRecipe
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManagerImpl @Inject constructor(
    private val apiService: ApiService
): ApiManager {

    override fun makeRetrofitCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String, // optional; only for generating from image
        filesDir: File?, // optional; only for generating from image
        onResponse: (String?) -> Unit
    ) {
        // when: the last line of each block is what will be assigned to the call val
        try {
            when (type) {
                // generate recipe
                LlmRequestType.RECIPE -> {
                    handleCall(
                        apiService.generateRecipe(request, "Bearer $bearerToken"),
                        onResponse
                    )
                }

                // generate 4x dishes from ingredients
                LlmRequestType.DISHES_FROM_INGREDIENTS -> {
                    //todo
                }

                // generate 4x dishes from image
                LlmRequestType.DISHES_FROM_IMAGE -> {
                    // Assembling the request with the compressed image jpg present:
                    // if error occurs here, it is handled by the catch block at the end

                    // https://medium.com/@myofficework000/uploading-files-with-multipart-form-data-in-retrofit-android-f9a4fecba360
                    val file = File(filesDir, imageFilename)
                    val requestBody = file.asRequestBody("image/jpg".toMediaType())
                    val multipartBody =
                        MultipartBody.Part.createFormData("image", file.name, requestBody)

                    handleCall(
                        apiService.generateFromImage(multipartBody, request, "Bearer $bearerToken"),
                        onResponse
                    )
                }

                // generate ingredients from image
                LlmRequestType.INGREDIENTS_FROM_IMAGE -> {
                    //todo
                }
                // ask a question about a recipe
                LlmRequestType.RECIPE_QUERY -> {
                    //todo
                }
                // alter a recipe
                LlmRequestType.RECIPE_ALTERATION -> {
                    //todo
                }
            }
        } catch(e: Exception) {
            Log.e("CALL ERROR LLM", "$e")
            onResponse("Internal Server Error. Please try again later.")
        }
    }

    private fun <T> handleCall(
        call: Call<T>,
        onResponse: (String?) -> Unit
    ) {
        call.enqueue(object : Callback<T> {
            // Response received from the server:
            override fun onResponse(
                call: Call<T>, response: Response<T>
            ) {
                if (response.isSuccessful) {
                    handleSuccess(response, onResponse)
                } else {
                    handleError(response, onResponse)
                }
            }
            // Timeout or no internet:
            override fun onFailure(call: Call<T>, t: Throwable) {
                throw t // Handled in ViewModel
            }
        })
    }

    // Extracts the error json from the error string using the LlmError
    // template, to update the UiState when called.
    private fun <T> handleError(
        response: Response<T>,
        onResponse: (String?) -> Unit
    ) {
        try {
            val error = response.errorBody()?.string()
            val errorJson = Gson().fromJson(error, LlmError::class.java)
            onResponse("Error: ${errorJson.error}")
        } catch (e: Exception) {
            // exception thrown if error is already returned as json - in this case,
            // simply return the exception itself.
            Log.e("SERVER ERROR LLM", "$e")
            onResponse("Error: The service is currently starting. Please try again in a few minutes!")
        }
    }

    // handles the error based on the type of response accordingly
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
}