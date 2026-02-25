package uk.co.imoqen.nourish.network.response

data class LlmDishesProposed(
    val ingredientsIdentified: String? = "",
    val firstMostLikelyDish: String,
    val secondMostLikelyDish: String,
    val thirdMostLikelyDish: String,
    val fourthMostLikelyDish: String,
    val dishNotPossible: Boolean
)