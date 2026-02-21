package uk.co.imoqen.nourish.network.response

/**
 * The Api Request returns a recipe in this format, using LangChain to structure the output.
 */
data class LlmRecipe(
    val ingredientAmounts: String,
    val mealType: String,
    val title: String,
    val ingredientAmountsUnits: String,
    val method: String,
    val ingredients: String,
    val ingredientPlurals: String,
    val servings: String
)