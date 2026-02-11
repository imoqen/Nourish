package uk.co.imoqen.nourish.recipediscovery

data class RecipeDiscoveryUiState(
    // for logging in:
    val apiResponse: String = "",
    val apiResponseError: String = "",
    val userRequest: String = ""
)