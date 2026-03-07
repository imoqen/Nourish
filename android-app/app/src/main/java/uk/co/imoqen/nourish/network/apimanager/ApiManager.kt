package uk.co.imoqen.nourish.network.apimanager

import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.ApiResponse
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmNutritionResponse
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipeResponse
import uk.co.imoqen.nourish.recipebook.LlmAction
import java.io.File

interface ApiManager {

    // for llm recipe generation
    fun makeRetrofitLlmRequestCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String = "", // optional; only for generating from image
        filesDir: File? = null, // optional; only for generating from image
        onDishesProposed: (LlmDishesProposed?) -> Unit, // optional; only for when dishes are proposed
        onResponse: (ApiResponse?) -> Unit,
    )

    // for llm actions, which are things that can be done AFTER the recipe is generated, from the recipe book screen in a recipe card.
    fun makeRetrofitLlmActionCall(
        type: LlmAction,
        request: LlmActionRequest,
        bearerToken: String,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipeResponse?) -> Unit,
        onNutritionGenerated: (LlmNutritionResponse?) -> Unit
    )

}