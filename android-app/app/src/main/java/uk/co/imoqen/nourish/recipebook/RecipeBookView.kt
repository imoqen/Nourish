package uk.co.imoqen.nourish.recipebook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import uk.co.imoqen.nourish.db.recipe.Recipe

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

        val allrecipes = recipeBookViewModel.getAllRecipes().collectAsState(initial = emptyList())
        Text("Recipe Book Screen")
        Text("all recipes: ${allrecipes.value}")
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe
) {
    Column() {
        Text()
    }
}

