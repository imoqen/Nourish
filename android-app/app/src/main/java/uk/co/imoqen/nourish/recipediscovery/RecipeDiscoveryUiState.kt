package uk.co.imoqen.nourish.recipediscovery

import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.common.tag.TagSheetState

/**
 * UiState for RecipeDiscovery: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably, adhering to
 * Unidirectional Data Flow UDF.
 */
data class RecipeDiscoveryUiState(

    val showRestartConfirmationDialog: Boolean = false,
    val userRequest: String = "",
    val userChoice: LlmMode? = null,
    val dishes: LlmDishesProposed? = null,
    val displayImage: Boolean = false,
    // TypeSelection not needed, as only one can be selected anyway: mutually exclusive.
    val recipeTypes: List<RecipeType>? = null,
    val selectedRecipeType: RecipeType? = null,
    val displayRecipeTypeSelector: Boolean = false,

    val recipeSaved: Boolean = false, // to prevent recipes from being saved more than once.

    val chosenDish: String = "",


    // The currently uploaded / saved image, most recent one; name of file, as a MipMap.
    // Can find the file via File(context.filesDir, savedImageFilename):
    val savedImageMipmap: Mipmap = Mipmap("",""),

    // The type of request:
    val requestType: LlmRequestType = LlmRequestType.RECIPE,

    // To transition smoothly when restart occurs
    val visible: Boolean = true,
    val placeholderText: String = "",
    val llmChatStage: LlmChatStage = LlmChatStage.USER_SELECT_OPTION, // Begins at the first stage
    val llmGeneratedRecipe: LlmRecipe? = null,

    // Tag sheet state: instantiated using the default tag sheet state values.
    val tagSheetState: TagSheetState = TagSheetState()
)

/**
 * MipMap approach to store high quality for displaying to the user, and low quality for sending
 * to the LLM.
 */
data class Mipmap(
    val lowQualityFilename: String,
    val highQualityFilename: String
)

/**
 * Controls flow within the llm chat. In the report, could draw this out into a flow diagram to show
 * all the possible flows through the chat.
 */
enum class LlmChatStage {
    USER_SELECT_OPTION, // first stage
    USER_INPUT, // second stage - once sent, cant be edited hence user only enters input once.
    AWAITING_LLM_RESPONSE, // third stage
    USER_SELECT_DISH, // optional fourth stage, which causes AWAITING_LLM_RESPONSE again
    USER_VIEW_RECIPE // fourth/fifth stage
}

/**
 * The LlmMode; there are three different modes. From description, image, and ingredients.
 */
enum class LlmMode {
    FROM_DESCRIPTION,
    FROM_IMAGE,
    FROM_INGREDIENTS
}