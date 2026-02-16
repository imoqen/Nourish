package uk.co.imoqen.nourish.network.response

/**
 * The Api Request returns a recipe in this format, using LangChain to structure the output.
 */
data class LlmRecipe(
    val ingredient_amounts: String,
    val meal_type: String,
    val title: String,
    val ingredient_amount_units: String,
    val method: String,
    val ingredients: String,
    val servings: String
)