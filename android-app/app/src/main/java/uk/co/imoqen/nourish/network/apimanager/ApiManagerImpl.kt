package uk.co.imoqen.nourish.network.apimanager

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
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmNutrition
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipe
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles the LLM calls and their responses. LLM calls are split into two different types:
 *   - Llm Request: To generate recipes and/or dishes.
 *   - Llm Action Request: To perform an action on an existing recipe: nutrition, query, or alteration.
 * @param apiService - the API Service, to send the LLM request to Google Cloud Service and
 *                     return the response, asynchronously.
 *
 * @author Imogen Pemberton, 2026
 */
@Singleton
class ApiManagerImpl @Inject constructor(
    private val apiService: ApiService
): ApiManager {

    /**
     * Makes the API call to generate a recipe / dishes, depending on the user input.
     * Used in RecipeDiscovery.
     * @param type - The type of call to make (Recipe, Dishes from Ingredients, Dishes from Image,
     *               Dishes from Ingredients Image, and Recipe from Image and chosen Dish).
     * @param request - The request to send to the LLM, including the prompt, nutritional filters,
     *                  and meal type.
     * @param bearerToken - The Bearer token of the user, for server-side authentication.
     * @param imageFilename - The filename of the image to send in the request, optionally.
     * @param filesDir - the files directory on the device, to read the image
     * @param onDishesProposed - Function which runs when dishes have been proposed
     * @param onError - Function which runs when an error occurs
     * @param onResponse - Function which runs when recipe has been generated
     */
    override fun makeRetrofitLlmRequestCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String, // optional; only for generating from image
        filesDir: File?, // optional; only for generating from image
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onError: (String) -> Unit,
        onResponse: (LlmRecipe?) -> Unit,
    ) {
        // The last line of each block is what will be assigned to the call val
        try {
            when (type) {
                // Generate recipe from description
                LlmRequestType.RECIPE -> {
                    handleRequestCall(
                        apiService.generateRecipe(request, "Bearer $bearerToken"),
                        onDishesProposed,
                        onResponse,
                        onError
                    )
                }

                // Generate recipe from image and the chosen dish. This occurs after 4x potential
                // dishes are generated, and the user selects one.
                LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH -> {
                    val file = File(filesDir, imageFilename)
                    val requestBody = file.asRequestBody("image/jpg".toMediaType())
                    val multipartBody =
                        MultipartBody.Part.createFormData("image", file.name, requestBody)

                    handleRequestCall(
                        apiService.generateRecipeFromDishImage(
                            multipartBody, request, "Bearer $bearerToken"
                        ),
                        onDishesProposed,
                        onResponse,
                        onError
                    )
                }

                // Generate 4x potential dishes from ingredients
                LlmRequestType.DISHES_FROM_INGREDIENTS -> {
                    handleRequestCall(
                        apiService.generateDishesFromIngredients(
                            request, "Bearer $bearerToken"
                        ),
                        onDishesProposed,
                        // do nothing because we are proposing dishes, not getting recipe response:
                        { },
                        onError
                    )
                }

                // Generate 4x potential dishes from image
                LlmRequestType.DISHES_FROM_IMAGE -> {
                    // Assembling the request with the compressed image jpg present:
                    // if error occurs here, it is handled by the catch block at the end
                    val file = File(filesDir, imageFilename)
                    val requestBody = file.asRequestBody("image/jpg".toMediaType())
                    val multipartBody =
                        MultipartBody.Part.createFormData(
                            "image", file.name, requestBody
                        )
                    handleRequestCall(
                        apiService.generateDishesFromImage(
                            multipartBody, request, "Bearer $bearerToken"
                        ),
                        onDishesProposed,
                        // do nothing because we are proposing dishes, not getting recipe response:
                        { },
                        onError
                    )
                }

                // Generate 4x potential dishes from image of ingredients
                LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE -> {
                    val file = File(filesDir, imageFilename)
                    val requestBody = file.asRequestBody("image/jpg".toMediaType())
                    val multipartBody =
                        MultipartBody.Part.createFormData(
                            "image", file.name, requestBody
                        )
                    handleRequestCall(
                        apiService.generateDishesFromIngredientsImage(
                            multipartBody, request, "Bearer $bearerToken"
                        ),
                        onDishesProposed,
                        // do nothing because we are proposing dishes, not getting recipe response:
                        { },
                        onError
                    )
                }
            }
        } catch(e: Exception) {
            onError("Internal Server Error. Please try again later.")
        }
    }

    /**
     * Makes the API call to perform LLM actions on an existing recipe.
     * Used in RecipeBook.
     * @param type - The type of call to make (None, Nutritional Values, Alteration, or Query).
     * @param request - The request to send to the LLM, including the recipe, query, all nutritional
     *                  filters, and the dish type.
     * @param bearerToken - The Bearer token of the user, for server-side authentication.
     * @param onQuestionAnswered - Function which runs when answer to question is generated.
     * @param onRecipeAltered - Function which runs when a recipe alteration is generated.
     * @param onNutritionGenerated - Function which runs when a recipe's nutrition is generated.
     * @param onError - Function which runs when an error occurs
     */
    override fun makeRetrofitLlmActionCall(
        type: LlmActionType,
        request: LlmActionRequest,
        bearerToken: String,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit,
        onError: (String) -> Unit
    ) {
        // The last line of each block is what will be assigned to the call val
        try {
            when (type) {
                // Generate existing recipe alteration:
                LlmActionType.ALTERATION -> {
                    handleActionCall(
                        apiService.generateAlteredRecipe(request, "Bearer $bearerToken"),
                        {}, onRecipeAltered, {}, onError
                    )
                }

                // Answer question about existing recipe:
                LlmActionType.QUESTION -> {
                    handleActionCall(
                        apiService.generateQueryAnswer(request, "Bearer $bearerToken"),
                        onQuestionAnswered, {}, {}, onError
                    )
                }

                // Generate nutritional values of existing recipe:
                LlmActionType.NUTRITIONAL_VALUES -> {
                    handleActionCall(
                        apiService.generateNutrition(request, "Bearer $bearerToken"),
                        {}, {}, onNutritionGenerated, onError
                    )
                }
                LlmActionType.NONE -> {
                    onError("Error. Action not specified.")
                }
            }
        } catch(e: Exception) {
            onError("Internal Server Error. Please try again later.")
        }
    }

    /**
     * Private auxiliary function to handle the LLM Request call.
     * @param call - The retrofit2 Call object
     * @param onDishesProposed - Function which runs when dishes are proposed
     * @param onResponse - Function which runs when recipe is generated
     * @param onError - Function which runs when error occurs.
     */
    private fun <T> handleRequestCall(
        call: Call<T>,
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onResponse: (LlmRecipe?) -> Unit,
        onError: (String) -> Unit
    ) {
        call.enqueue(object : Callback<T> {
            // Response received from the server:
            override fun onResponse(
                call: Call<T>, response: Response<T>
            ) {
                if (response.isSuccessful) {
                    handleRequestSuccess(response, onDishesProposed, onResponse, onError)
                } else {
                    handleRequestError(response, onError)
                }
            }
            // Timeout or no internet:
            override fun onFailure(call: Call<T>, t: Throwable) {
                onError("Internal Server Error. Please try again later, or check your internet connection.")
            }
        })
    }

    /**
     * Private auxiliary function to handle an error during the LLM Request call,
     * by extracting the error Json.
     * @param response - The erroneous response sent from the Cloud Run Service.
     * @param onError - Function which runs when an error occurs.
     */
    private fun <T> handleRequestError(
        response: Response<T>,
        onError: (String) -> Unit
    ) {
        try {
            val error = response.errorBody()?.string()
            val errorJson = Gson().fromJson(error, LlmError::class.java)
            onError("An error occurred. ${errorJson.error}")
        } catch (e: Exception) {
            // Exception thrown if error is already returned as json - in this case, server must be starting.
            onError("The LLM service is down, or currently starting. Please try again in a few minutes!")
        }
    }

    /**
     * Private auxiliary function to handle a successful response from the LLM Request.
     * Handles the different cases: onResponse for recipe and onDishesProposed for proposed dishes.
     * @param response - the LLM response
     * @param onDishesProposed - Function which runs to store the dishes proposed, to output in the UI
     * @param onResponse - Function which runs to store the generated recipe, to output in the UI
     * @param onError - Function which runs when error occurs.
     */
    private fun <T> handleRequestSuccess(
        response: Response<T>,
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onResponse: (LlmRecipe?) -> Unit,
        onError: (String) -> Unit
    ) {
        val post = response.body()
        when(post) {
            // Recipe generated:
            is LlmRecipe -> {
                onResponse(post)
            }
            // Dishes generated:
            is LlmDishesProposed -> {
                onDishesProposed(post)
                // no recipe generated - only dishes.
                // onResponse should still be called for cleanup and validation.
                onResponse(null)
            }
            else -> {
                onError(ErrorMessage.API_ERROR_FAILED_TO_GEN)
            }
        }
    }

    // Action Call Auxiliary Functions:

    /**
     * Private auxiliary function to handle the LLM Action Request call.
     * @param call - The retrofit2 Call object
     * @param onQuestionAnswered - Function which runs when answer to question is generated.
     * @param onRecipeAltered - Function which runs when a recipe alteration is generated.
     * @param onNutritionGenerated - Function which runs when a recipe's nutrition is generated.
     * @param onError - Function which runs when an error occurs
     */
    private fun <T> handleActionCall(
        call: Call<T>,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit,
        onError: (String) -> Unit
    ) {
        call.enqueue(object : Callback<T> {
            // Response received from the server:
            override fun onResponse(
                call: Call<T>, response: Response<T>
            ) {
                if (response.isSuccessful) {
                    handleActionSuccess(
                        response, onQuestionAnswered, onRecipeAltered, onNutritionGenerated, onError
                    )
                } else {
                    handleActionError(response, onError)
                }
            }
            // Timeout or no internet:
            override fun onFailure(call: Call<T>, t: Throwable) {
                onError("Internal Server Error. Please try again later, or check your internet connection.")
            }
        })
    }

    /**
     * Private auxiliary function to handle an error during the LLM Action Request call,
     * by extracting the error Json.
     * @param response - The erroneous response sent from the Cloud Run Service.
     * @param onError - Function which runs when an error occurs.
     */
    private fun <T> handleActionError(
        response: Response<T>,
        onError: (String) -> Unit
    ) {
        try {
            val error = response.errorBody()?.string()
            val errorJson = Gson().fromJson(error, LlmError::class.java)
            // Likely due to token limit being reached.
            onError("An error occurred. ${errorJson.error}")
        } catch (e: Exception) {
            onError("The LLM service is down, or currently starting. Please try again in a few minutes!")
        }
    }

    /**
     * Private auxiliary function to handle a successful response from the LLM Action Request.
     * Handles the different cases: question answer, recipe alteration, and nutrition generation.
     * @param response - the LLM Action response
     * @param onQuestionAnswered - Function which runs when answer to question is generated.
     * @param onRecipeAltered - Function which runs when a recipe alteration is generated.
     * @param onNutritionGenerated - Function which runs when a recipe's nutrition is generated.
     * @param onError - Function which runs when an error occurs
     */
    private fun <T> handleActionSuccess(
        response: Response<T>,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit,
        onError: (String) -> Unit
    ) {
        val post = response.body()
        when(post) {
            // Question response:
            is LlmQuestionResponse -> {
                onQuestionAnswered(post)
            }
            // Recipe alteration:
            is LlmRecipe -> {
                onRecipeAltered(post)
            }
            // LLM nutrition:
            is LlmNutrition -> {
                onNutritionGenerated(post)
            }
            // Error:
            else -> {
                onError(ErrorMessage.API_ERROR_FAILED_TO_GEN)
            }
        }
    }
}