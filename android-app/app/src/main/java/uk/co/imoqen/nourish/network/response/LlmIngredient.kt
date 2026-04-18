package uk.co.imoqen.nourish.network.response

/**
 * The JSON, ensured by LangChain, of an ingredient which is used in a recipe.
 * Obtained in the application as such, and handled accordingly.
 */
data class LlmIngredient(
    val ingredientSingular: String,
    val ingredientPlural: String,
    val ingredientAmount: Double,
    val ingredientAmountUnit: String
)