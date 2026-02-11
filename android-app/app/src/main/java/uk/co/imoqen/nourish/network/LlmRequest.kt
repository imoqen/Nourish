package uk.co.imoqen.nourish.network

/**
 * The Api Request sends a single prompt containing the recipe that the user wishes to cook.
 */
data class LlmRequest(
    val prompt: String
)