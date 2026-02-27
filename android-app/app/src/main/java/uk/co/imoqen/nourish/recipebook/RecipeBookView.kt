package uk.co.imoqen.nourish.recipebook

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import java.io.File
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

        val recipes = recipeBookViewModel.getRecipeBookMap()
        RecipeList(recipeBookViewModel)

        // currently only outputs the first recipe; only one recipe will be shown at once.
        /*if (allRecipes.value.isNotEmpty()) {
            Text("all recipes: $recipes")
            recipeBookViewModel.setSelectedRecipe(allRecipes.value.last())
            RecipeCard(recipeBookViewModel)
        }*/
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeList(
    recipeBookViewModel: RecipeBookViewModel
) {


    val uiState = recipeBookViewModel.uiState.collectAsState().value
    val recipeBookMap = uiState.recipeBookMap
    val context = LocalContext.current

    recipeBookMap?.forEach { (recipeType, recipeCardsUnexpanded) ->
        Text(recipeType.title)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recipeCardsUnexpanded) { recipeCardUnexpanded ->
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                .width(200.dp) // matches the card width
                                .height(115.dp)
                            ) {
                                // Image:
                                val imageFile = File(context.filesDir, recipeCardUnexpanded.imagePath)
                                var displayImage = false
                                if (imageFile.exists() && recipeCardUnexpanded.imagePath != "") {
                                    displayImage = true
                                    //Use Coil to display the selected image
                                    val painter = rememberAsyncImagePainter(
                                        ImageRequest
                                            .Builder(LocalContext.current)
                                            .data(imageFile)
                                            .build()
                                    )

                                    Image(
                                        painter = painter,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(200.dp) // matches the card width
                                            .height(100.dp), // 2/3 of the card height - maybe use consts for this in future to ensure they match upon changes
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.width(200.dp) // matches the card width
                                            .height(100.dp)
                                            .background(MaterialTheme.colorScheme.error) // put the chosen colour
                                    )
                                }
                                // Emoji:
                                Text(
                                    text = recipeCardUnexpanded.emoji,
                                    fontSize = 32.sp,
                                    // center emoji if no image, otherwise put at bottom center
                                            modifier = if (displayImage) {Modifier.align(Alignment.BottomCenter)}
                                            else {Modifier.align(Alignment.Center).offset(y = (-10).dp)}, // go -15 which is 115 - 100 (total box - image) to ensure centred plus a bit less for looking good
                                    style = TextStyle(
                                                    shadow = Shadow(
                                                        color = Color.Black,
                                                        offset = Offset(4f, 4f),
                                                        blurRadius = 4f
                                                    ))
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = recipeCardUnexpanded.title,
                                    modifier = Modifier.padding(4.dp)
                                )
                                recipeCardUnexpanded.recipeTags.forEach { recipeTag ->
                                    Text(recipeTag)
                                }
                            }
                        }
                    }
                }
            }
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
