package uk.co.imoqen.nourish.network.apimanager

import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmNutrition
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipe
import java.io.File

/**
 * APIManager, provides the contract for making Retrofit calls to the Google Cloud Run service,
 * and handle the result appropriately.
 *
 * @author Imogen Pemberton, 2026
 */
interface ApiManager {

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
    fun makeRetrofitLlmRequestCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String = "",
        filesDir: File? = null,
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onError: (String) -> Unit,
        onResponse: (LlmRecipe?) -> Unit
    )

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
    fun makeRetrofitLlmActionCall(
        type: LlmActionType,
        request: LlmActionRequest,
        bearerToken: String,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit,
        onError: (String) -> Unit
    )

}