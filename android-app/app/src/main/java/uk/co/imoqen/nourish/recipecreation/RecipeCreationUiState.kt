package uk.co.imoqen.nourish.recipecreation

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

    // list of ingredient? todo
    val ingredients: List<LlmIngredientSection>? = null
    // list of method? todo
    // recipe tag list? todo

)