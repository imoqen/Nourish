package uk.co.imoqen.nourish.recipecreation

import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetState
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.common.tag.TagSheetState

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
    val ingredients: List<LlmIngredientSection>? = null,
    val showAddIngredientSheet: Boolean = false,
    val ingredientToAdd: AddIngredientSheetState = AddIngredientSheetState()

    // list of method? todo

)