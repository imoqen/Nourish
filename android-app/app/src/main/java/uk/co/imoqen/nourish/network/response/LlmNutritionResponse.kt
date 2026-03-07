package uk.co.imoqen.nourish.network.response

data class LlmNutritionResponse (
    val servings: Int, // so the nutrition is given for this serving amount
    val calories: Int,
    val fat: Double,
    val saturates: Double, // "of which saturates" ^
    val carbohydrate: Double,
    val sugars: Double, // "of which sugars" ^
    val protein: Double,
    val salt: Double,
)
