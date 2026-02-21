package uk.co.imoqen.nourish.network.response

data class ApiResponse(
    val llmRecipe: LlmRecipe?,
    val string: String
)