package uk.co.imoqen.nourish.recipebook

data class RecipeBookUiState(
    val selectedRecipeCard: RecipeCard? = null
)

data class RecipeCard(
    val title: String,
    val sectionsWithIngredients: MutableMap<String, MutableList<Triple<String, Double, Long>>>,
    val method: String,
    val serves: Int,
    val recipeType: String,
    val recipeTags: MutableList<String>,
    val storageInstructions: String
)