package uk.co.imoqen.nourish.network.response

// generic class to be reused across the different response options:
// llmrecipe, llmrecipesproposed
// MAYBE DELETE? MAYBE DONT NEED.
data class LlmResponse<T>(
    val contents: T
)