package uk.co.imoqen.nourish.recipebook

import uk.co.imoqen.nourish.db.recipe.Recipe

data class RecipeBookUiState(
    val selectedRecipe: Recipe? = null,
    val selectedRecipeCard: RecipeCard? = null
)

data class RecipeCard(
    val title: String,
    val sectionsWithIngredients: MutableMap<String, MutableList<Triple<String, Double, Long>>>, // ingredient name+unit, amount, id
    val method: MutableList<Triple<String, Boolean, Long>>, // step, complete or not, id
    val serves: Int,
    val recipeType: String,
    val recipeTags: MutableList<String>,
    val storageInstructions: String,
    val recipeId: Long // to recover the corresponding recipe card if needed
)