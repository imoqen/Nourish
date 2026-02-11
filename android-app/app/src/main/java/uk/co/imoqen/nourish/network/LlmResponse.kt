package uk.co.imoqen.nourish.network

// based on the json structure retrieved from the example, https://jsonplaceholder.typicode.com/
data class LlmResponse(
    val ingredient_amounts: String,
    val meal_type: String,
    val title: String,
    val ingredient_amount_units: String,
    val method: String,
    val ingredients: String,
    val servings: String
)