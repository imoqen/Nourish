package uk.co.imoqen.nourish.network.response

/**
 * The JSON, ensured by LangChain, of the recipe query LLM response, so the LLM's answer.
 * Obtained in the application as such, and handled accordingly.
 */
data class LlmQuestionResponse (
    val answer: String
)