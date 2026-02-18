package uk.co.imoqen.nourish.network.response

data class LlmRecipesProposed(
    val firstMostLikelyDish: String,
    val secondMostLikelyDish: String,
    val thirdMostLikelyDish: String,
    val fourthMostLikelyDish: String
)