package uk.co.imoqen.nourish.recipebook

import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag

data class RecipeBookUiState(
    val recipeBookMap: MutableMap<RecipeType, MutableList<RecipeCardUnexpanded>>? = null,
    val selectedRecipe: Recipe? = null,
    val selectedRecipeCard: RecipeCardExpanded? = null,
    val searchTerm: String = "",
    val displayDropdownFilter: Boolean = false,
    val allTags: List<TagSelection>? = null,
    val recipeTypes: List<TypeSelection>? = null
)

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
    val recipeId: Long // to recover the corresponding recipe card if needed
)

// condensed version of RecipeCardExpanded, so only the data that's necessary to display on the
// all recipes page is displayed.
// potentially could add ingredients list here to enable searching by ingredient.
data class RecipeCardUnexpanded(
    val title: String,
    val recipeTags: MutableList<Pair<String, NourishColour>>,
    val imagePath: String,
    val emoji: String,
    val recipeId: Long
)

data class UiRecipeTag(
    val identifier: String,
    val colour: String
)