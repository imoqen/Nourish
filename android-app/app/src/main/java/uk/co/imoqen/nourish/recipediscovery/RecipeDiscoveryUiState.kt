package uk.co.imoqen.nourish.recipediscovery

import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection

/**
 * UiState for RecipeDiscovery: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably.
 */
data class RecipeDiscoveryUiState(
    val apiResponse: String = "",
    val userRequest: String = "",
    val awaitingResponse: Boolean = false,

    val userChoice: LlmMode? = null,

    val dishes: LlmDishesProposed? = null,

    val displayImage: Boolean = false,
    val displayFilters: Boolean = false,
    val displayTagCreation: Boolean = false,

    val createdTagName: String = "",

    val recipeTags: List<TagSelection>? = null,
    val recipeTypes: List<TypeSelection>? = null,


    // the currently uploaded / saved image, most recent one; name of file. can find the file via File(context.filesDir, savedImageFilename):
    val savedImageFilename: String = "",

    val requestType: LlmRequestType = LlmRequestType.RECIPE, // gen recipe by default

    val placeholderText: String = ""
)

enum class LlmMode {
    FROM_DESCRIPTION,
    FROM_IMAGE,
    FROM_INGREDIENTS
}