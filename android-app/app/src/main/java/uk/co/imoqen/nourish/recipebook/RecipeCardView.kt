package uk.co.imoqen.nourish.recipebook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.emptyFlow
import uk.co.imoqen.nourish.common.onBackgroundColour
import uk.co.imoqen.nourish.db.recipe.Recipe

@Composable
fun RecipeCardScreen(
    recipeBookViewModel: RecipeBookViewModel,
    recipeId: Long
) {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
    ) {
        val recipe = recipeBookViewModel.getRecipeById(recipeId).collectAsState(initial = null).value
        // should constrain it to only return recipe made by that user, for security. address in a future stepping stone ^
        if (recipe != null) {
            recipeBookViewModel.setSelectedRecipe(recipe)
            RecipeCard(recipeBookViewModel)
        }
    }
}

@Composable
fun RecipeCard(
    recipeBookViewModel: RecipeBookViewModel
) {
    val recipe = recipeBookViewModel.uiState.collectAsState().value.selectedRecipe
    recipeBookViewModel.buildRecipeCard()
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

            // in future, id here can be used to edit the complete status of the recipe.
            Text("Method:")
            recipeCard.method.forEachIndexed{ i, (instruction, complete, id) ->
                Text(
                    text = "Step ${i+1}. $instruction",
                    textDecoration = if (complete) { TextDecoration.LineThrough} else TextDecoration.None
                )
                Button(
                    onClick = {
                        recipeBookViewModel.markStepAsComplete(id, !complete)
                    }
                ) {
                    Text("Mark as complete")
                }
            }

            Text("Serves: ${recipeCard.serves}")
            Text("Recipe type: ${recipeCard.recipeType}")
            if (recipeCard.recipeTags.isNotEmpty()) {
                Text("Tags:")
                recipeCard.recipeTags.forEach { (tag, colour) ->
                    Text(
                        text = tag,
                        color = colour.onBackgroundColour()
                    )
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