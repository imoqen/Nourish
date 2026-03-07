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
import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.response.LlmError
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.ApiResponse
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmNutrition
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipe
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManagerImpl @Inject constructor(
    private val apiService: ApiService
): ApiManager {

    override fun makeRetrofitLlmRequestCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String, // optional; only for generating from image
        filesDir: File?, // optional; only for generating from image
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onResponse: (ApiResponse?) -> Unit
    ) {
        // when: the last line of each block is what will be assigned to the call val
        try {
            when (type) {
                // generate recipe
                LlmRequestType.RECIPE -> {
                    Log.i("calling llm", "pompt: $request")
                    handleRequestCall(
                        apiService.generateRecipe(request, "Bearer $bearerToken"),
                        onDishesProposed,
                        onResponse
                    )
                }

                // generate recipe from image and the chosen dish
                LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH -> {
                    Log.i("calling llm", "pompt: $request")
                    val file = File(filesDir, imageFilename)
                    val requestBody = file.asRequestBody("image/jpg".toMediaType())
                    val multipartBody =
                        MultipartBody.Part.createFormData("image", file.name, requestBody)

                    handleRequestCall(
                        apiService.generateRecipeFromDishImage(multipartBody, request, "Bearer $bearerToken"),
                        onDishesProposed,
                        onResponse
                    )
                }

                // generate 4x dishes from ingredients
                LlmRequestType.DISHES_FROM_INGREDIENTS -> {
                    Log.i("calling llm", "pompt: $request")
                    handleRequestCall(
                        apiService.generateDishesFromIngredients(request, "Bearer $bearerToken"),
                        onDishesProposed,
                        { } // do nothing because we are proposing dishes, not getting recipe response

                    )
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

                    handleRequestCall(
                        apiService.generateDishesFromImage(multipartBody, request, "Bearer $bearerToken"),
                        onDishesProposed,
                        { } // do nothing because we are proposing dishes, not getting recipe response

                    )
                }

                // generate potential dishes from image of ingredients
                LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE -> {
                    val file = File(filesDir, imageFilename)
                    val requestBody = file.asRequestBody("image/jpg".toMediaType())
                    val multipartBody =
                        MultipartBody.Part.createFormData("image", file.name, requestBody)

                    handleRequestCall(
                        apiService.generateDishesFromIngredientsImage(multipartBody, request, "Bearer $bearerToken"),
                        onDishesProposed,
                        { } // do nothing because we are proposing dishes, not getting recipe response

                    )
                }
            }
        } catch(e: Exception) {
            Log.e("CALL ERROR LLM", "$e")
            onResponse(ApiResponse(null, "Internal Server Error. Please try again later."))
        }
    }

    override fun makeRetrofitLlmActionCall(
        type: LlmActionType,
        request: LlmActionRequest,
        bearerToken: String,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit
    ) {
        // when: the last line of each block is what will be assigned to the call val
        try {
            when (type) {
                // generate recipe
                LlmActionType.ALTERATION -> {
                    handleActionCall(
                        apiService.generateAlteredRecipe(request, "Bearer $bearerToken"),
                        {}, onRecipeAltered, {}
                    )
                }

                // generate recipe from image and the chosen dish
                LlmActionType.QUESTION -> {
                    handleActionCall(
                        apiService.generateQueryAnswer(request, "Bearer $bearerToken"),
                        onQuestionAnswered, {}, {}
                    )
                }

                // generate 4x dishes from ingredients
                LlmActionType.NUTRITIONAL_VALUES -> {
                    handleActionCall(
                        apiService.generateNutrition(request, "Bearer $bearerToken"),
                        {}, {}, onNutritionGenerated
                    )
                }

                LlmActionType.NONE -> {
                    Log.e("Llm Call", "Call not made - the action was not specified.")
                }
            }
        } catch(e: Exception) {
            Log.e("CALL ERROR LLM", "$e")
        }
    }

    private fun <T> handleRequestCall(
        call: Call<T>,
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onResponse: (ApiResponse?) -> Unit
    ) {
        call.enqueue(object : Callback<T> {
            // Response received from the server:
            override fun onResponse(
                call: Call<T>, response: Response<T>
            ) {
                if (response.isSuccessful) {
                    handleRequestSuccess(response, onDishesProposed, onResponse)
                } else {
                    handleRequestError(response, onResponse)
                }
            }
            // Timeout or no internet:
            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.e("CALL ERROR LLM", "${t.message}")
                onResponse(ApiResponse(null,"Internal Server Error. Please try again later."))
            }
        })
    }

    // Extracts the error json from the error string using the LlmError
    // template, to update the UiState when called.
    private fun <T> handleRequestError(
        response: Response<T>,
        onResponse: (ApiResponse?) -> Unit
    ) {
        try {
            val error = response.errorBody()?.string()
            val errorJson = Gson().fromJson(error, LlmError::class.java)
            onResponse(ApiResponse(null, "Error: ${errorJson.error}"))
        } catch (e: Exception) {
            // exception thrown if error is already returned as json - in this case,
            // simply return the exception itself.
            Log.e("SERVER ERROR LLM", "$e")
            onResponse(ApiResponse(null, "Error: The service is currently starting. Please try again in a few minutes!"))
        }
    }

    // handles the error based on the type of response accordingly
    private fun <T> handleRequestSuccess(
        response: Response<T>,
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onResponse: (ApiResponse?) -> Unit
    ) {
        val post = response.body()
        when(post) {
            is LlmRecipe -> {
                onResponse(ApiResponse(post,
                    "${post.title}:" +
                            //"\n\nIngredients: \n${post.ingredients} " +
                            "\n\nMethod: \n${post.method}" +
                            "\n\nServing Size: \n${post.servings} " +
                            "\n\nType of Meal: \n${post.mealType} ")
                )
            }
            is LlmDishesProposed -> {
                onDishesProposed(post)
                onResponse(ApiResponse(null, "")) // no recipe generated - only dishes. onresponse should still be called for cleanup and validation.
            }
            else -> {
                onResponse(ApiResponse(null, ErrorMessage.API_ERROR_FAILED_TO_GEN))
            }
        }
    }



    private fun <T> handleActionCall(
        call: Call<T>,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit
    ) {
        call.enqueue(object : Callback<T> {
            // Response received from the server:
            override fun onResponse(
                call: Call<T>, response: Response<T>
            ) {
                if (response.isSuccessful) {
                    handleActionSuccess(response, onQuestionAnswered, onRecipeAltered, onNutritionGenerated)
                } else {
                    handleActionError(response)
                }
            }
            // Timeout or no internet:
            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.i("Llm Action", "${t.message}")
            }
        })
    }

    // simply logs the error for debugging
    private fun <T> handleActionError(
        response: Response<T>,
    ) {
        try {
            val error = response.errorBody()?.string()
            Log.i("Server error llm", "$error")
            val errorJson = Gson().fromJson(error, LlmError::class.java)
            Log.i("Server Error LLM", errorJson.error)
        } catch (e: Exception) {
            Log.e("SERVER ERROR LLM", "$e")
        }
    }

    private fun <T> handleActionSuccess(
        response: Response<T>,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit
    ) {
        val post = response.body()
        when(post) {
            is LlmQuestionResponse -> {
                onQuestionAnswered(post)
            }
            is LlmRecipe -> {
                onRecipeAltered(post)
            }
            is LlmNutrition -> {
                onNutritionGenerated(post)
            }
            else -> {
                Log.i("Llm Action", ErrorMessage.API_ERROR_FAILED_TO_GEN)
            }
        }
    }
}