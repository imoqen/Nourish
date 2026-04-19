package uk.co.imoqen.nourish.recipebook

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.response.LlmRecipe

/**
 * UiState for RecipeBook: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably, adhering to
 * Unidirectional Data Flow UDF.
 *
 * @author Imogen Pemberton, 2026
 */
data class RecipeBookUiState(
    // Displaying and searching recipes:
    val recipeBookMap: MutableMap<RecipeType, MutableList<RecipeCardUnexpanded>>? = null,
    val selectedRecipe: Recipe? = null,
    val selectedRecipeCard: RecipeCardExpanded? = null,
    val searchTerm: String = "",
    val displayDropdownFilter: Boolean = false,
    val recipeTags: List<TagSelection>? = null, // all of the recipe tags
    val recipeTypes: List<TypeSelection>? = null, // all of the recipe types

    // Viewing a selected recipe and performing actions on it:
    val llmActionType: LlmActionType = LlmActionType.NONE,
    // to define which is being awaited for - to display the loading bar and make sure it stays,
    // even if the dialog etc is closed and then returned to.
    val llmAwaitActionType: LlmActionType = LlmActionType.NONE,
    val userActionRequest: String = "",
    // at the start, it is always user input.
    val actionChatStage: ActionChatStage = ActionChatStage.USER_INPUT,
    val alteredRecipe: LlmRecipe? = null, // altered recipe produced by llm
    val visible: Boolean = true,

    // To add ingredients to the shopping list:
    val showShoppingListDialog: Boolean = false,
    // boolean outlines whether it should be added to shopping list or not. all are initially set
    // to true and set to false only if user chooses not to add it.
    val ingredientsToAddToShoppingList: MutableList<Pair<ShoppingListIngredient, Boolean>>? = null,

    // To delete a recipe:
    val deleteConfirmation: Boolean = false
)

/**
 * The stage in the Action Chat, to determine if loading (awaiting response), input, or received.
 */
enum class ActionChatStage {
    USER_INPUT,
    AWAITING_LLM_RESPONSE,
    RESPONSE_RECEIVED
}

/**
 * Tag, and whether it has been selected or not.
 */
data class TagSelection(
    val tag: Tag,
    val selected: Boolean
)

/**
 * Type, and whether it has been selected or not.
 */
data class TypeSelection(
    val type: RecipeType,
    val selected: Boolean
)

/**
 * Holds the Ui state of the recipe card when expanded; contains all details of a given Recipe.
 * This enables the extra detail to only be stored when necessary.
 */
data class RecipeCardExpanded(
    val title: String,
    // ingredient name+unit, amount, id:
    val sectionsWithIngredients: MutableMap<String, MutableList<Triple<String, Double, Long>>>,
    // step, complete or not, id:
    val method: MutableList<Triple<String, Boolean, Long>>,
    val serves: Int,
    val recipeType: Pair<String, Int>,
    val recipeTags: MutableList<Pair<String, NourishColour>>,
    val storageInstructions: String,
    val imagePath: String,
    val emoji: String,
    val colour: NourishColour,
    val llmGenerated: Boolean
)

/**
 * Holds the Ui state of the recipe card when unexpanded (when viewing all recipes);
 * contains a condensed version of RecipeCardExpanded, so only the data that's necessary to display
 * when viewing all recipes.
 * This enables the extra detail to only be stored when necessary.
 */
data class RecipeCardUnexpanded(
    val title: String,
    val recipeTags: MutableList<Pair<String, NourishColour>>,
    val imagePath: String,
    val emoji: String,
    val recipeId: Long,
    val colour: NourishColour // from the NourishColour enum then will be read using enum.valueof<nourishcolour>
)

/**
 * An ingredient to store in the shopping list.
 * Ingredient is stored as deconstructed as it will be used in the UI, so don't want to be calling
 * the repository.getById in the UI.
 */
data class ShoppingListIngredient(
    val ingredientId: Long,
    // the plural form will always be displayed in the shopping list.
    // Singular is not put here as its not needed.
    val plural: String,
    val unit: IngredientUnit,
    val amount: Double
)