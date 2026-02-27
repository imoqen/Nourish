package uk.co.imoqen.nourish.recipebook

import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType

data class RecipeBookUiState(
    val recipeBookMap: MutableMap<RecipeType, MutableList<RecipeCardUnexpanded>>? = null,
    val selectedRecipe: Recipe? = null,
    val selectedRecipeCard: RecipeCardExpanded? = null
)

data class RecipeCardExpanded(
    val title: String,
    val sectionsWithIngredients: MutableMap<String, MutableList<Triple<String, Double, Long>>>, // ingredient name+unit, amount, id
    val method: MutableList<Triple<String, Boolean, Long>>, // step, complete or not, id
    val serves: Int,
    val recipeType: String,
    val recipeTags: MutableList<String>,
    val storageInstructions: String,
    val recipeId: Long // to recover the corresponding recipe card if needed
)

// condensed version of RecipeCardExpanded, so only the data that's necessary to display on the
// all recipes page is displayed.
// potentially could add ingredients list here to enable searching by ingredient.
data class RecipeCardUnexpanded(
    val title: String,
    val recipeTags: MutableList<String>,
    val imagePath: String,
    val emoji: String,
    val recipeId: Long
)

data class UiRecipeTag(
    val identifier: String,
    val colour: String
)