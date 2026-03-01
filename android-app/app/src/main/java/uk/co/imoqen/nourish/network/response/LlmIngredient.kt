package uk.co.imoqen.nourish.network.response

data class LlmIngredient(
    val ingredientSingular: String,
    val ingredientPlural: String,
    val ingredientAmount: Double,
    val ingredientAmountUnit: String
)