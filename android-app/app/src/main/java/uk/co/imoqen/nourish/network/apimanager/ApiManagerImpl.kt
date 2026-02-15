package uk.co.imoqen.nourish.network.apimanager

import android.graphics.Bitmap
import com.google.gson.Gson
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.LlmError
import uk.co.imoqen.nourish.network.LlmRequest
import uk.co.imoqen.nourish.network.LlmResponse
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
        var responseString = ""

        call.enqueue(object : Callback<LlmResponse> {
            // OnResponse occurs when a response has been received from the server. could be error.
            override fun onResponse(
                call: Call<LlmResponse>, response: Response<LlmResponse>
            ) {
                if (response.isSuccessful) {
                    val post = response.body()
                    if (post == null) {
                        onResponse(ErrorMessage.API_ERROR_FAILED_TO_GEN)
                    }
                    else {
                        // Extracts the text and updates the UiState
                        responseString = "${post.title}:" +
                                    "\n\nIngredients: \n${post.ingredients} " +
                                    "\n\nIngredient Amounts: \n${post.ingredient_amounts} " +
                                    "\n\nIngredient Units: \n${post.ingredient_amount_units} " +
                                    "\n\nMethod: \n${post.method}" +
                                    "\n\nServing Size: \n${post.servings} " +
                                    "\n\nType of Meal: \n${post.meal_type} "

                        onResponse(responseString)
                    }
                } else {
                    // Extracts the error json from the error string using the LlmError
                    // template, updates the UiState
                    val error = response.errorBody()?.string()
                    val errorJson = Gson().fromJson(error, LlmError::class.java)
                    responseString = "Internal Server Error: ${errorJson.error}"
                    onResponse(responseString)
                }
            }
            // OnFailure occurs when no internet/timeout
            override fun onFailure(call: Call<LlmResponse>, t: Throwable) {
                // handled in the view model
                throw t
            }
        })
    }

    override fun handleFromImage(
        request: LlmRequest,
        bearerToken: String,
        image: Bitmap,
        onResponse: (String?) -> Unit
    ) {
        //val multipartBody = MultipartBody.Part.createFormData("image", image)
       // val call = apiService.generateFromImage(image, request, "Bearer $bearerToken")
    }
}