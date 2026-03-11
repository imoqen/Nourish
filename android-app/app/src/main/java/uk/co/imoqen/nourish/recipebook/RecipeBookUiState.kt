package uk.co.imoqen.nourish.recipebook

import androidx.compose.ui.graphics.Color
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.response.LlmRecipe

data class RecipeBookUiState(
    val recipeBookMap: MutableMap<RecipeType, MutableList<RecipeCardUnexpanded>>? = null,
    val selectedRecipe: Recipe? = null,
    val selectedRecipeCard: RecipeCardExpanded? = null,
    val searchTerm: String = "",
    val displayDropdownFilter: Boolean = false,
    val recipeTags: List<TagSelection>? = null, // all of the recipe tags
    val recipeTypes: List<TypeSelection>? = null, // all of the recipe types

    val llmActionType: LlmActionType = LlmActionType.NONE,

    val userActionRequest: String = "",

    val actionChatStage: ActionChatStage = ActionChatStage.USER_INPUT, // at start it is always user input.

    val alteredRecipe: LlmRecipe? = null, // altered recipe produced by llm
    val visible: Boolean = true
)

enum class ActionChatStage {
    USER_INPUT,
    AWAITING_LLM_RESPONSE,
    RESPONSE_RECEIVED
}

data class TagSelection(
    val tag: Tag,
    val selected: Boolean
)

data class TypeSelection(
    val type: RecipeType,
    val selected: Boolean
)

data class RecipeCardExpanded(
    val title: String,
    val sectionsWithIngredients: MutableMap<String, MutableList<Triple<String, Double, Long>>>, // ingredient name+unit, amount, id
    val method: MutableList<Triple<String, Boolean, Long>>, // step, complete or not, id
    val serves: Int,
    val recipeType: Pair<String, Int>,
    val recipeTags: MutableList<Pair<String, NourishColour>>,
    val storageInstructions: String,
    val imagePath: String,
    val emoji: String,
    val colour: NourishColour
)

// condensed version of RecipeCardExpanded, so only the data that's necessary to display on the
// all recipes page is displayed.
// potentially could add ingredients list here to enable searching by ingredient.
data class RecipeCardUnexpanded(
    val title: String,
    val recipeTags: MutableList<Pair<String, NourishColour>>,
    val imagePath: String,
    val emoji: String,
    val recipeId: Long,
    val colour: NourishColour // from the NourishColour enum then will be read using enum.valueof<nourishcolour>
)

data class UiRecipeTag(
    val identifier: String,
    val colour: String
)