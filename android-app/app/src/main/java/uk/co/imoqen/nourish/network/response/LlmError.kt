package uk.co.imoqen.nourish.network.response

/**
 * An error which can occur from calling the API.
 *
 * @author Imogen Pemberton, 2026
 */
data class LlmError(
    val error: String
)