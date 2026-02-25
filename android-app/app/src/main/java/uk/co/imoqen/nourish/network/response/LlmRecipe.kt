package uk.co.imoqen.nourish.network.response

/**
 * The Api Request returns a recipe in this format, using LangChain to structure the output.
 */
data class LlmRecipe(
    val title: String,
    val sectionIngredients: List<LlmIngredientSection>,
    val servings: String,
    val mealType: String,
    val method: String,
    val tags: List<String>,
    val storage: String,
    val recipeNotPossible: Boolean
)

data class LlmIngredientSection(
    val section: String,
    val ingredients: List<LlmIngredient>
)