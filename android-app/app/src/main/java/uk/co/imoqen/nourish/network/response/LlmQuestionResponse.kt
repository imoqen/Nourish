package uk.co.imoqen.nourish.network.response

/**
 * The JSON, ensured by LangChain, of the recipe query LLM response, so the LLM's answer.
 * Obtained in the application as such, and handled accordingly.
 *
 * @author Imogen Pemberton, 2026
 */
data class LlmQuestionResponse (
    val answer: String
)