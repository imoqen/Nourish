package uk.co.imoqen.nourish.network.request

/**
 * The Api Request sends a single prompt containing the recipe that the user wishes to cook.
 */
data class LlmRequest(
    val prompt: String,
    val chosenNutritionalFilters: String,
    val allNutritionalFilters: String,
    val dishType: String
)