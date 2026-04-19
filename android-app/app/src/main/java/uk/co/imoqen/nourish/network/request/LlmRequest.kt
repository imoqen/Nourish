package uk.co.imoqen.nourish.network.request

/**
 * Data class outlining the LLM Request to send.
 * The Api Request sends a single prompt containing the recipe that the user wishes to cook,
 * along with selected nutritional filters, all nutritional filters authored by that user to select from,
 * and the selected dish type if selected.
 *
 * @author Imogen Pemberton, 2026
 */
data class LlmRequest(
    val prompt: String,
    val chosenNutritionalFilters: String,
    val allNutritionalFilters: String,
    val dishType: String
)