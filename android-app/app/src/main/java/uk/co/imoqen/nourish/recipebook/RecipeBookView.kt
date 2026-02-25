package uk.co.imoqen.nourish.recipebook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

        //allRecipes.value.forEach{ recipe ->
        //    RecipeCard(recipe, recipeBookViewModel)
        //}

        // currently only outputs the first recipe; only one recipe will be shown at once.
        if (allRecipes.value.isNotEmpty()) {
            RecipeCard(allRecipes.value.last(), recipeBookViewModel)
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    recipeBookViewModel: RecipeBookViewModel
) {
    recipeBookViewModel.buildRecipeCard(recipe)
    val recipeCard = recipeBookViewModel.uiState.collectAsState().value.selectedRecipeCard

    if (recipeCard != null) {
    Column(
        modifier = Modifier.padding(vertical=8.dp)
    ) {
        Text("Title: ${recipeCard.title}")
        Text("Ingredients:")
        // then output the ingredients by section
        recipeCard.sectionsWithIngredients.forEach { (section, ingredients) ->
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // only output name of section if there is more than 1 section, otherwise it is irrelevant
                if (recipeCard.sectionsWithIngredients.size > 1) {
                    Text(section)
                }

                // id can be used if user wishes to add to shopping list
                ingredients.forEach { (ingredient, amount, id) ->
                    Text("${formatIngredientAmount(amount)} $ingredient")
                }
            }
        }

        Text("Method: ${recipeCard.method}")
        Text("Serves: ${recipeCard.serves}")
        Text("Recipe type: ${recipeCard.recipeType}")
        if (recipeCard.recipeTags.isNotEmpty()) {
            Text("Tags:")
            recipeCard.recipeTags.forEach { tag ->
                Text(tag)
            }
        }

        Text("Storage guidance: ${recipeCard.storageInstructions}")

        }
    }
}

// shaves off the decimal if it is .0, otherwise keeps it.
// e.g. 5.0 -> 5 but 5.5 remains -> 5.5
fun formatIngredientAmount(
    ingredientAmount: Double
): String {
    if (ingredientAmount % 1.0 == 0.0) {
        return ingredientAmount.toInt().toString()
    }
    return ingredientAmount.toString()
}
