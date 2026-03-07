package co.uk.imoqen.testfixtures.network

import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.ApiResponse
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmNutrition
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipe
import java.io.File

class FakeApiManager(): ApiManager {

    override fun makeRetrofitLlmRequestCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String,
        filesDir: File?,
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onResponse: (ApiResponse?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun makeRetrofitLlmActionCall(
        type: LlmActionType,
        request: LlmActionRequest,
        bearerToken: String,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

}