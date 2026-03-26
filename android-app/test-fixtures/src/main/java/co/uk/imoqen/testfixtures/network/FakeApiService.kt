package co.uk.imoqen.testfixtures.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.mock.Calls
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.network.response.LlmNutrition
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipe

// I THINK THIS CAN BE DELETED as we use api manager within view models anyway, never directly api service.

class FakeApiService(): ApiService {

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

    var result: Call<LlmRecipe> =
        Calls.response(LlmRecipe(
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
        ))

    override fun generateRecipe(
        request: LlmRequest,
        bearer: String
    ): Call<LlmRecipe> {
        TODO("Not yet implemented")
    }

    override fun generateRecipeFromDishImage(
        image: MultipartBody.Part,
        request: LlmRequest,
        bearer: String
    ): Call<LlmRecipe> {
        TODO("Not yet implemented")
    }

    override fun generateDishesFromIngredients(
        request: LlmRequest,
        bearer: String
    ): Call<LlmDishesProposed> {
        TODO("Not yet implemented")
    }

    override fun generateDishesFromIngredientsImage(
        image: MultipartBody.Part,
        request: LlmRequest,
        bearer: String
    ): Call<LlmDishesProposed> {
        TODO("Not yet implemented")
    }

    override fun generateDishesFromImage(
        image: MultipartBody.Part,
        request: LlmRequest,
        bearer: String
    ): Call<LlmDishesProposed> {
        TODO("Not yet implemented")
    }

    override fun generateNutrition(
        request: LlmActionRequest,
        bearer: String
    ): Call<LlmNutrition> {
        TODO("Not yet implemented")
    }

    override fun generateQueryAnswer(
        request: LlmActionRequest,
        bearer: String
    ): Call<LlmQuestionResponse> {
        TODO("Not yet implemented")
    }

    override fun generateAlteredRecipe(
        request: LlmActionRequest,
        bearer: String
    ): Call<LlmRecipe> {
        TODO("Not yet implemented")
    }


}