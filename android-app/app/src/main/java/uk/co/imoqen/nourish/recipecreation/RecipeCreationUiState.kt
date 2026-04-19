package uk.co.imoqen.nourish.recipecreation

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetState
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.common.tag.TagSheetState
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.shoppinglist.IngredientToEdit
import java.util.UUID

/**
 * UiState for RecipeCreation: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably, adhering to
 * Unidirectional Data Flow UDF.
 *
 * @author Imogen Pemberton, 2026
 */
data class RecipeCreationUiState(
    // Ui state of recipe creation. nothing should be added to the DB until the user presses save.
    val title: String = "",
    val numberOfServings: Int = 1,
    val recipeType: RecipeType? = null,
    val storageGuidance: String = "",
    val displayRecipeTypeSelector: Boolean = false,
    val recipeTypes: List<RecipeType>? = null,

    // Recipe tags:
    val tagSheetState: TagSheetState = TagSheetState(),

    // List of ingredients. for simplicity (not only programming, but also for the user's sake),
    // ingredients cant be added to sections. just one section.
    val ingredients: List<LlmIngredient>? = null,
    val showAddIngredientSheet: Boolean = false,
    val ingredientToAdd: AddIngredientSheetState = AddIngredientSheetState(
        unit = IngredientUnit.UNIT // default unit is UNIT rather than SINGLE - no SINGLEs in recipes.
    ),
    val ingredientToEdit: IngredientToEdit = IngredientToEdit(),
    val ingredientIndexToDelete: Int = -1,

    // List of method
    // required for the lazy list orderable (draggable) to work. because it relies on each item being unique.
    // If a user entered two duplicate steps, the app would crash.
    val method: List<RecipeStep>? = null,
    val showAddMethodSheet: Boolean = false,
    val stepToAdd: String = "",
    val stepToEdit: String = "",
    val stepIndexToEdit: Int = -1,
    val stepIndexToDelete: Int = -1,

    // Image, colour, and emoji of recipe to create.
    val imageFile: String = "",
    val emoji: String = "\uD83E\uDD57",
    val colour: NourishColour? = null,
    val showColourSelector: Boolean = false,
    val showEmojiSelector: Boolean = false,

    val recipeCreated: Boolean = false,
    val recipeCreationError: String = "",
    val recipeEditId: Long = -1,
    // Default is add, otherwise edit will be used. this is to use different actions if in edit mode.
    val creationMode: CreationMode = CreationMode.ADD,

    // To discard changes / new recipe:
    val showBackConfirmation: Boolean = false
)

/**
 * a Recipe Step. UUID is used as the id in the lazy list, rather than its index as this can change.
 * So that every Recipe Step is unique (the "step" String cannot be used, as would break if two are
 * the same.)
 */
data class RecipeStep(
    val step: String,
    val uuid: UUID
)

/**
 * The mode to use the RecipeCreationView in; creation mode to add a new recipe, or edit mode to edit
 * an existing recipe.
 */
enum class CreationMode {
    ADD,
    EDIT
}