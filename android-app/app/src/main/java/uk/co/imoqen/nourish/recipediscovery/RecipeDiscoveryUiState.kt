package uk.co.imoqen.nourish.recipediscovery

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipebook.TypeSelection

/**
 * UiState for RecipeDiscovery: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably.
 */
data class RecipeDiscoveryUiState(

    val filterDisplayState: FilterState = FilterState.NONE, // determines what part of the filter selection is shown

    val showTagConfirmationDialog: Boolean = false,
    val showRestartConfirmationDialog: Boolean = false,
    val tagToDelete: Tag? = null,

    val apiResponse: String = "",
    val userRequest: String = "",

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

    val recipeSaved: Boolean = false, // to prevent recipes from being saved more than once.

    val chosenDish: String = "",


    // the currently uploaded / saved image, most recent one; name of file. can find the file via File(context.filesDir, savedImageFilename):
    val savedImageMipmap: Mipmap = Mipmap("",""),

    val requestType: LlmRequestType = LlmRequestType.RECIPE, // gen recipe by default

    // to transition smoothly when restart occurs
    val visible: Boolean = true, // in future should refactor the chat dialogs to use a single state then animate this and display with when

    val placeholderText: String = "",

    val llmChatStage: LlmChatStage = LlmChatStage.USER_SELECT_OPTION, // begsina t the first stage

    val llmGeneratedRecipe: LlmRecipe? = null
)

data class Mipmap(
    val lowQualityFilename: String,
    val highQualityFilename: String
)

// controls flow within the llm chat. in the report, can draw this out into a flow diagram to show all the possible flows through the chat
enum class LlmChatStage {
    USER_SELECT_OPTION, // first stage
    USER_INPUT, // second stage - once sent, cant be edited hence user only enters input once.
    AWAITING_LLM_RESPONSE, // third stage
    USER_SELECT_DISH, // optional fourth stage, which causes AWAITING_LLM_RESPONSE again
    USER_VIEW_RECIPE // fourth/fifth stage
}

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