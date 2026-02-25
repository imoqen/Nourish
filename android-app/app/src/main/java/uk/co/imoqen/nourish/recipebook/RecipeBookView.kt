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

                // determine pluralities
                val ingredientAmount = ingredient.amount
                val ingredientUnit = ingredient.ingredientUnit
                val unit = if (determineUnitPlurality(ingredientAmount)) {ingredientUnit.pluralName} else {ingredientUnit.singularName}
                val ingredientName = recipeBookViewModel.getIngredientById(ingredient.ingredientId).collectAsState(initial = null).value


                if (ingredientName != null) {

                    val ingredientPluralisedName = if (determineIngredientPlurality(ingredientAmount, ingredientUnit)) {ingredientName.plural} else {ingredientName.singular}

                    if (unit != "") {
                        Text("${formatIngredientAmount(ingredientAmount)} $unit $ingredientPluralisedName")
                    }
                    else {
                        Text("${formatIngredientAmount(ingredientAmount)} $ingredientPluralisedName")
                    }
                }
            }
        } else {
            Text("None")
        }
        Text("Method: ${recipe.method}")
        Text("Serves: ${recipe.numberOfServings}")

        val recipeTypeId = recipe.recipeTypeId
        val recipeType = recipeBookViewModel.getRecipeTypeById(recipeTypeId).collectAsState(initial = null)
        Text("Recipe type: ${recipeType.value?.title}")

        val tags = recipeBookViewModel.getRecipeTagsForRecipe(recipe.id).collectAsState(initial = null).value
        if (tags != null) {
            Text("Tags:")
            tags.recipeTags.forEach{ tag ->
                val tagValue = recipeBookViewModel.getTagByTagId(tag.tagId).collectAsState(initial = null)
                Text("${tagValue.value?.identifier}")
            }
        }

        Text("Storage guidance: ${recipe.storageInstructions}")

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

// determine if plural form should be used or not
// true if plural, false if singular
fun determineUnitPlurality(
    ingredientAmount: Double,
): Boolean {
    // plural form should be used in most cases; just a few cases where it shouldn't.
    return ingredientAmount != 1.0 // if only 1, should always be singular.
}

fun determineIngredientPlurality(
    ingredientAmount: Double,
    ingredientUnit: IngredientUnit
): Boolean {
    if (ingredientAmount == 1.0) {
        // e.g. 1 strawberry = singular
        // but 1 cup strawberry should be plural: 1 cup strawberries
        if (ingredientUnit.useSingularForOne) {
            return false // use singular
        }
    }
    return true
}

