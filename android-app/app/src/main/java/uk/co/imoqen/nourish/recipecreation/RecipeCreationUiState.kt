package uk.co.imoqen.nourish.recipecreation

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetState
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.common.tag.TagSheetState
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.shoppinglist.IngredientToEdit
import java.util.UUID

data class RecipeCreationUiState(
    // ui state of recipe creation. nothing should be added to the DB until the user presses save.
    val title: String = "",
    val numberOfServings: Int = 1,
    val recipeType: RecipeType? = null,
    val storageGuidance: String = "",
    val displayRecipeTypeSelector: Boolean = false,
    val recipeTypes: List<RecipeType>? = null,

    // recipe tags:
    val tagSheetState: TagSheetState = TagSheetState(),

    // list of ingredients. for simplicity (not only programming, but also for the user's sake), ingredients cant be added to sections. just one section.
    val ingredients: List<LlmIngredient>? = null, // will set this to List<LlmIngredientSection> when doing createRecipe
    val showAddIngredientSheet: Boolean = false,
    val ingredientToAdd: AddIngredientSheetState = AddIngredientSheetState(unit = IngredientUnit.UNIT), // default unit is UNIT rather than SINGLE - no SINGLEs in recipes.
    val ingredientToEdit: IngredientToEdit = IngredientToEdit(),
    val ingredientIndexToDelete: Int = -1,

    // list of method
    val method: List<RecipeStep>? = null, // required for the lazy list orderable (draggable) to work. because it relies on each item being unique. if a user entered two duplicate steps, the app would crash.
    val showAddMethodSheet: Boolean = false,
    val stepToAdd: String = "",
    val stepToEdit: String = "",
    val stepIndexToEdit: Int = -1,
    val stepIndexToDelete: Int = -1,

    // image, colour, and emoji
    val imageFile: String = "",
    val emoji: String = "\uD83E\uDD57",
    val colour: NourishColour? = null
)

data class RecipeStep(
    val step: String,
    val uuid: UUID
)