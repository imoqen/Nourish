package uk.co.imoqen.nourish.recipebook

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.backgroundColour
import uk.co.imoqen.nourish.common.onBackgroundColour
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import java.io.File
import kotlin.collections.emptyList


/**
 * Recipe Book view
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBookScreen(
    recipeBookViewModel: RecipeBookViewModel,
    onViewRecipeCard: (Long) -> Unit
) {

    val scrollState = rememberScrollState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val uiState = recipeBookViewModel.uiState.collectAsState().value
    val keyboardFocusManager =
        LocalFocusManager.current // To close the keyboard once user submits input

    Column() {
        // search bar: https://developer.android.com/develop/ui/compose/components/search-bar

        NourishTextField(
            value = uiState.searchTerm,
            onValueChange = { newSearchTerm ->
                recipeBookViewModel.setSearchTerm(newSearchTerm)
            },
            placeholder = "Search for a recipe",
            icon = Icons.Filled.Search,
            onDone = { keyboardFocusManager.clearFocus() },
            trailingIcon = Icons.Filled.Clear,
            onValueReset = { recipeBookViewModel.setSearchTerm("") }
        )

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
        ) {
            val allRecipes = recipeBookViewModel.getAllRecipes().collectAsState(initial = emptyList())

            Text("Recipe Book Screen")

            //allRecipes.value.forEach{ recipe ->
            //    RecipeCard(recipe, recipeBookViewModel)
            //}

            val recipes = recipeBookViewModel.getRecipeBookMap()

            RecipeList(recipeBookViewModel, onViewRecipeCard)
        }

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
    recipeBookViewModel: RecipeBookViewModel,
    onViewRecipeCard: (Long) -> Unit
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
                        .width(240.dp)
                        .height(210.dp)
                ) {
                    Box(
                        Modifier.fillMaxSize()
                            .clickable {
                                onViewRecipeCard(recipeCardUnexpanded.recipeId)
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                .width(240.dp) // matches the card width
                                .height(if (recipeCardUnexpanded.imagePath != "") {115.dp} else {100.dp})
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
                                            .width(240.dp) // matches the card width
                                            .height(100.dp), // 2/3 of the card height - maybe use consts for this in future to ensure they match upon changes
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.width(240.dp) // matches the card width
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
                                            else {Modifier.align(Alignment.Center)},
                                    style = TextStyle(
                                                    shadow = Shadow(
                                                        color = Color.Black,
                                                        offset = Offset(4f, 4f),
                                                        blurRadius = 4f
                                                    ))
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = recipeCardUnexpanded.title,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            Box(
                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                FlowRow(
                                    modifier = Modifier.padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    // only display max 3 then ... if there are more than 3.
                                    // can fit 24px max before going on next line, so need to constrain this.

                                    // IMPORTANT: one tag cannot be more than 27 chars long - need to confine this when user inputs it.
                                    val tags: List<Pair<String, NourishColour>>
                                    val lengthOfThreeTags =  (recipeCardUnexpanded.recipeTags.take(3).sumOf { (tag, colour) ->
                                        tag.length
                                        })
                                    tags = if (lengthOfThreeTags > 27) {
                                        if((recipeCardUnexpanded.recipeTags.take(2).sumOf { (tag, colour) ->
                                                tag.length
                                            }) > 27) {
                                            recipeCardUnexpanded.recipeTags.take(1)
                                        } else {
                                            recipeCardUnexpanded.recipeTags.take(2)
                                        }
                                    } else {
                                        recipeCardUnexpanded.recipeTags.take(3)
                                    }
                                    tags
                                        .forEach { (tag, colour) ->
                                            // alternate colour of tag depending on dark / light mode
                                            Text(
                                                text = tag,
                                                fontSize = 14.sp,
                                                modifier = Modifier
                                                    .background(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = if (isSystemInDarkTheme()) {
                                                            colour.onBackgroundColour()
                                                        } else {
                                                            colour.backgroundColour()
                                                        }
                                                    )
                                                    .padding(horizontal = 4.dp),
                                                color = if (isSystemInDarkTheme()) {
                                                    colour.backgroundColour()
                                                } else {
                                                    colour.onBackgroundColour()
                                                }
                                            )
                                        }
                                    if (recipeCardUnexpanded.recipeTags.size > tags.size) {
                                        Text("...")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
