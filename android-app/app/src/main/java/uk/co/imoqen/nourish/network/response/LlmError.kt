package uk.co.imoqen.nourish.network.response

/**
 * An error which can occur from calling the API
 */
data class LlmError(
    val error: String
)