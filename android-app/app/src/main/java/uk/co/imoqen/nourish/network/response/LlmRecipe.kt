package uk.co.imoqen.nourish.network.response

/**
 * The Api Request returns a recipe in this format, using LangChain to structure the output.
 * LlmIngredientSection is a further data class to add sections to ingredients,
 *      e.g. "For the sauce"
 */
data class LlmRecipe(
    val title: String,
    val sectionIngredients: List<LlmIngredientSection>,
    val servings: Int,
    val mealType: String,
    val method: List<String>,
    val tags: List<String>,
    val storage: String,
    val recipeNotPossible: Boolean,
    val emoji: String,
    val colour: String
)

data class LlmIngredientSection(
    val section: String,
    val ingredients: List<LlmIngredient>
)