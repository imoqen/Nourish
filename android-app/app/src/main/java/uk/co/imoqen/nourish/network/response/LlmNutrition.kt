package uk.co.imoqen.nourish.network.response

/**
 * The JSON, ensured by LangChain, of the dish LLM recipe nutrition response. Obtained in the application
 * as such, and handled accordingly.
 */
data class LlmNutrition (
    val servings: Int, // The nutrition is given for this serving amount, which is always 1.
    val calories: Int,
    val fat: Double,
    val saturates: Double, // "of which saturates" ^
    val carbohydrate: Double,
    val sugars: Double, // "of which sugars" ^
    val protein: Double,
    val salt: Double,
    val fibre: Double
)
