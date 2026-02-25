package uk.co.imoqen.nourish.recipebook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import kotlin.collections.emptyList

/**
 * Recipe Book view
 */
@Composable
fun RecipeBookScreen(
    recipeBookViewModel: RecipeBookViewModel
) {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
    ) {

        val allRecipes = recipeBookViewModel.getAllRecipes().collectAsState(initial = emptyList())
        Text("Recipe Book Screen")

        allRecipes.value.forEach{ recipe ->
            RecipeCard(recipe, recipeBookViewModel)
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    recipeBookViewModel: RecipeBookViewModel
) {
    val recipeIngredients = recipeBookViewModel.getRecipeIngredientsForRecipe(recipe.id).collectAsState(initial = null).value
    Column(
        modifier = Modifier.padding(vertical=8.dp)
    ) {
        Text("Title: ${recipe.title}")
        Text("Ingredients:")
        if (recipeIngredients != null) {
            recipeIngredients.ingredients.forEach { ingredient ->
                val ingredientName = recipeBookViewModel.getIngredientById(ingredient.ingredientId).collectAsState(initial = null).value
                if (ingredientName != null) {
                    Text("${ingredient.amount} ${ingredient.ingredientUnit.singularName} ${ingredientName.singular}")
                }
            }
        } else {
            Text("None")
        }
        Text("Method: ${recipe.method}")
    }
}

