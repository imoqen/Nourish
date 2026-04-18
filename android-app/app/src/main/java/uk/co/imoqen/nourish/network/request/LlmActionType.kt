package uk.co.imoqen.nourish.network.request

/**
 * The type of the LLM Action to send.
 */
enum class LlmActionType {
    NONE,
    NUTRITIONAL_VALUES,
    ALTERATION,
    QUESTION
}