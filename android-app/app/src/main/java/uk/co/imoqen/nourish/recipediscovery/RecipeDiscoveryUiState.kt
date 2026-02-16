package uk.co.imoqen.nourish.recipediscovery

/**
 * UiState for RecipeDiscovery: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably.
 */
data class RecipeDiscoveryUiState(
    val apiResponse: String = "",
    val userRequest: String = "",
    val awaitingResponse: Boolean = false,
    val awaitingImageResponse: Boolean = false
)