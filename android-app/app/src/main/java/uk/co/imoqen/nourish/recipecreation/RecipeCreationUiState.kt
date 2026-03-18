package uk.co.imoqen.nourish.recipecreation

import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.recipebook.RecipeCardUnexpanded

class RecipeCreationUiState(
    val recipeBookMap: MutableMap<RecipeType, MutableList<RecipeCardUnexpanded>>? = null
)