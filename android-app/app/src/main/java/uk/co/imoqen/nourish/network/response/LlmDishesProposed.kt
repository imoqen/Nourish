package uk.co.imoqen.nourish.network.response

/**
 * The JSON, ensured by LangChain, of the dish proposal LLM response. Obtained in the application
 * as such, and handled accordingly.
 */
data class LlmDishesProposed(
    val ingredientsIdentified: String? = "",
    val firstMostLikelyDish: String,
    val secondMostLikelyDish: String,
    val thirdMostLikelyDish: String,
    val fourthMostLikelyDish: String,
    val dishNotPossible: Boolean
)