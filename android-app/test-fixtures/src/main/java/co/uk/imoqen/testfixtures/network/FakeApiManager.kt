package co.uk.imoqen.testfixtures.network

import co.uk.imoqen.testfixtures.common.TestData
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.network.response.LlmNutrition
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipe
import java.io.File

class FakeApiManager(): ApiManager {

    // Mock values for LlmRecipe:

    val saladIngredientSection: LlmIngredientSection = LlmIngredientSection(
        section = "For the salad",
        ingredients = listOf( // for one serving:
            LlmIngredient(
                ingredientSingular = "cucumber",
                ingredientPlural = "cucumbers",
                ingredientAmount = 0.25,
                ingredientAmountUnit = "UNIT"
            ),
            LlmIngredient(
                ingredientSingular = "tomato",
                ingredientPlural = "tomatoes",
                ingredientAmount = 100.0,
                ingredientAmountUnit = "G"
            ),
            LlmIngredient(
                ingredientSingular = "lettuce",
                ingredientPlural = "lettuce",
                ingredientAmount = 4.0,
                ingredientAmountUnit = "LEAF"
            )
        )
    )

    // illustrates many of the possible ingredient units being used
    val dressingIngredientSection: LlmIngredientSection = LlmIngredientSection(
        section = "For the dressing",
        ingredients = listOf(
            LlmIngredient(
                ingredientSingular = "honey",
                ingredientPlural = "honey",
                ingredientAmount = 5.0,
                ingredientAmountUnit = "ML"
            ),
            LlmIngredient(
                ingredientSingular = "vinegar",
                ingredientPlural = "vinegar",
                ingredientAmount = 1.0,
                ingredientAmountUnit = "DASH"
            ),
            LlmIngredient(
                ingredientSingular = "lemon juice",
                ingredientPlural = "lemon juice",
                ingredientAmount = 1.0,
                ingredientAmountUnit = "DROP"
            ),
            LlmIngredient(
                ingredientSingular = "salt",
                ingredientPlural = "salt",
                ingredientAmount = 1.0,
                ingredientAmountUnit = "SPRINKLE"
            ),
            LlmIngredient(
                ingredientSingular = "pepper",
                ingredientPlural = "pepper",
                ingredientAmount = 1.0,
                ingredientAmountUnit = "DASH"
            )
        )
    )

    var result = LlmRecipe(
        "Healthy Salad",
        sectionIngredients = listOf( saladIngredientSection, dressingIngredientSection ),
        servings = 1,
        mealType = "Lunch",
        method = listOf(
            "First, wash the vegetables.", "Cut the vegetables.",
            "Make the dressing.", "Assemble and mix everything together in a big bowl."
        ),
        tags = listOf(
            "VEGAN", "LOW-CALORIE"
        ),
        storage = "Eat immediately.",
        recipeNotPossible = false,
        emoji = "\uD83E\uDD57",
        colour = "GREEN"
    )

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
                    onDishesProposed(TestData.llmDishesProposed_dishNotPossible)
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