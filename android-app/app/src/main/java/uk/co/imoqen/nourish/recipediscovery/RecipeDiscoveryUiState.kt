package uk.co.imoqen.nourish.recipediscovery

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection

/**
 * UiState for RecipeDiscovery: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably.
 */
data class RecipeDiscoveryUiState(

    val filterDisplayState: FilterState = FilterState.NONE, // determines what part of the filter selection is shown

    val showConfirmationDialog: Boolean = false,
    val tagToDelete: Tag? = null,

    val apiResponse: String = "",
    val userRequest: String = "",
    val awaitingResponse: Boolean = false,

    val userChoice: LlmMode? = null,

    val dishes: LlmDishesProposed? = null,

    val displayImage: Boolean = false,

    val createdTagName: String = "",
    val createdTagColour: NourishColour? = null,
    val tagCreationError: String = "",

    val recipeTags: List<TagSelection>? = null,
    val recipeTypes: List<RecipeType>? = null, // typeselection not needed as only one can be selected anyway
    val selectedRecipeType: RecipeType? = null,
    val displayRecipeTypeSelector: Boolean = false,

    val allowUserInput: Boolean = false,
    val userSentRequest: Boolean = false,

    val chosenDish: String = "",


    // the currently uploaded / saved image, most recent one; name of file. can find the file via File(context.filesDir, savedImageFilename):
    val savedImageFilename: String = "",

    val requestType: LlmRequestType = LlmRequestType.RECIPE, // gen recipe by default

    // to transition smoothly when restart occurs
    val visible: Boolean = true, // in future should refactor the chat dialogs to use a single state then animate this and display with when

    val placeholderText: String = ""
)

enum class LlmMode {
    FROM_DESCRIPTION,
    FROM_IMAGE,
    FROM_INGREDIENTS
}

enum class FilterState {
    APPLY, // to apply filters to the query
    CREATE, // to add a new filter
    EDIT, // delete filters
    NONE // when the modal bottom sheet for filtering should not be displayed
}