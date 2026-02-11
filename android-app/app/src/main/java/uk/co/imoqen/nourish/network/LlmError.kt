package uk.co.imoqen.nourish.network

/**
 * An error which can occur from calling the API
 */
data class LlmError(
    val error: String
)