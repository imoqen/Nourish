package co.uk.imoqen.testfixtures.network

import co.uk.imoqen.testfixtures.common.TestData
import uk.co.imoqen.nourish.network.apimanager.ApiManager
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
 * Mock ApiManager for tests.
 */
class FakeApiManager(): ApiManager {

    override fun makeRetrofitLlmRequestCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String,
        filesDir: File?,
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onError: (String) -> Unit,
        onResponse: (LlmRecipe?) -> Unit
    ) {
        if (bearerToken == "triggerError") {
            onError("An error occurred.")
        }
        else {
            when (type) {
                LlmRequestType.RECIPE -> {
                    onResponse(TestData.llmRecipe_salad)
                }

                LlmRequestType.DISHES_FROM_INGREDIENTS -> {
                    if (imageFilename == "") {
                        onDishesProposed(TestData.llmDishesProposed_pasta)
                    } else {
                        onDishesProposed(TestData.llmDishesProposed_dishNotPossible)
                    }
                }

                LlmRequestType.DISHES_FROM_IMAGE -> {
                    onDishesProposed(TestData.llmDishesProposed_pasta)
                }

                LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE -> {
                    onDishesProposed(TestData.llmDishesProposed_dishNotPossible)
                }

                LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH -> {
                    onResponse(TestData.llmRecipe_recipeNotPossible)
                }
            }
        }
    }

    override fun makeRetrofitLlmActionCall(
        type: LlmActionType,
        request: LlmActionRequest,
        bearerToken: String,
        onQuestionAnswered: (LlmQuestionResponse?) -> Unit,
        onRecipeAltered: (LlmRecipe?) -> Unit,
        onNutritionGenerated: (LlmNutrition?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (bearerToken == "triggerError") {
            onError("An error occurred.")
        }
        else {
            when (type) {
                LlmActionType.NUTRITIONAL_VALUES -> {
                    onNutritionGenerated(TestData.llmNutrition_salad)
                }
                LlmActionType.NONE -> {
                    onError("No type selected.")
                }
                LlmActionType.ALTERATION -> {
                    if (bearerToken == "triggerRecipeNotPossible") {
                        onRecipeAltered(TestData.llmRecipe_recipeNotPossible)
                    } else {
                        onRecipeAltered(TestData.llmRecipe_salad)
                    }
                }
                LlmActionType.QUESTION -> {
                    onQuestionAnswered(TestData.llmQuestionResponse)
                }
            }
        }
    }

}