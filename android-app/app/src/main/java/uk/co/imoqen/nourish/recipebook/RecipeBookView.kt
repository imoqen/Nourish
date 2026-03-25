package uk.co.imoqen.nourish.recipebook

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import uk.co.imoqen.nourish.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.NourishDivider
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.SelectedTags
import uk.co.imoqen.nourish.common.TagsDisplayForFiltering
import uk.co.imoqen.nourish.common.TypesDisplayForFiltering
import uk.co.imoqen.nourish.common.backgroundColour
import uk.co.imoqen.nourish.common.dimmedBackgroundColour
import uk.co.imoqen.nourish.common.onBackgroundColour
import uk.co.imoqen.nourish.common.recipeColour
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.shoppinglist.UserInteractionStatus
import java.io.File
import kotlin.enumValueOf


/**
 * Recipe Book view
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBookScreen(
    recipeBookViewModel: RecipeBookViewModel,
    onViewRecipeCard: (Long) -> Unit,
    onAddRecipe: () -> Unit,
) {

    val scrollState = rememberScrollState()
    val uiState = recipeBookViewModel.uiState.collectAsState().value
    val keyboardFocusManager =
        LocalFocusManager.current // To close the keyboard once user submits input

    Column() {
        // search bar: https://developer.android.com/develop/ui/compose/components/search-bar

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal=8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box (
                contentAlignment = Alignment.Center
            ) {
                NourishTextField(
                    value = uiState.searchTerm,
                    onValueChange = { newSearchTerm ->
                        recipeBookViewModel.setSearchTerm(newSearchTerm)
                    },
                    placeholder = "Search for a recipe",
                    leadingIcon = Icons.Filled.Search,
                    onDone = { keyboardFocusManager.clearFocus() },
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconPress = { recipeBookViewModel.setSearchTerm("") },
                    modifier = Modifier.padding(bottom=8.dp)
                )
            }

            // filter button:
            Box(
                modifier = Modifier
                    .height(48.dp) // height of nourishTextField = 54+8 = 64 (the 8 bits are for padding)
                    .padding(top = 8.dp)
                    .padding(horizontal = 6.dp)
                    .background(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = { recipeBookViewModel.setDisplayDropdownFilter(!uiState.displayDropdownFilter) }) {
                    Icon(
                        painter = painterResource(R.drawable.filter_icon),
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                DropdownMenu(
                    expanded = uiState.displayDropdownFilter,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(0.8f),
                    onDismissRequest = { recipeBookViewModel.setDisplayDropdownFilter(false) }
                ) {
                    // Dropdown menu:
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        NourishTextDivider("Filter by tag(s)")
                        val tags = recipeBookViewModel.uiState.collectAsState().value.recipeTags
                        TagsDisplayForFiltering(
                            tags = tags,
                            onTagPressed = recipeBookViewModel::updateSelected
                        )

                        NourishTextDivider("Filter by type(s)")

                        val types = recipeBookViewModel.uiState.collectAsState().value.recipeTypes
                        TypesDisplayForFiltering(types, recipeBookViewModel::updateSelected)
                    }
                }
            }
        }

        // display selected recipe tags:
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom=4.dp)
        ) {
            SelectedTags(
                tags = uiState.recipeTags,
                allowEdit = true,
                updateSelectedTag = recipeBookViewModel::updateSelected
            )
        }

            // set all recipes
            // maybe should putthis in launched effect in the future
            recipeBookViewModel.getRecipeBookMap()

            // set all tags and recipe types for the dropdown
            // set all nutritional tags and recipe types for the dropdown, only change when those change.

            LaunchedEffect(uiState.recipeTypes?.size) {
                Log.i("size:", "${uiState.recipeTypes?.size}")
                recipeBookViewModel.getAllRecipeTypes()
            }
            LaunchedEffect(uiState.recipeTags?.size) {
                Log.i("size:", "${uiState.recipeTags?.size}")
                recipeBookViewModel.getAllTagsForUser()
            }

            RecipeList(recipeBookViewModel, onViewRecipeCard, scrollState, onAddRecipe)

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeList(
    recipeBookViewModel: RecipeBookViewModel,
    onViewRecipeCard: (Long) -> Unit,
    scrollState: ScrollState,
    onAddRecipe: () -> Unit // to navigate to add recipe view when adding a recipe.
) {
    val uiState = recipeBookViewModel.uiState.collectAsState().value
    val recipeBookMap = uiState.recipeBookMap
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val totalRecipeAmount = recipeBookMap?.values?.size ?: 0

    AnimatedContent(
        targetState = (totalRecipeAmount != 0),
    ) { recipesExist ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
        if (recipesExist && recipeBookMap != null) {

            var loading by remember { mutableStateOf(true) }
            LaunchedEffect("TIME_FOR_LOADING") {
                //delay(150)
                loading = false
            }

            AnimatedVisibility(
                visible = !loading,
                enter = fadeIn(tween(1000)),
                exit = fadeOut(tween(1000))
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
                ) {

                    val recipeTypesSelected: MutableList<RecipeType> = mutableListOf()
                    uiState.recipeTypes!!.forEach { typeSelection ->
                        if (typeSelection.selected) {
                            recipeTypesSelected.add(typeSelection.type)
                        }
                    }

                    recipeBookMap.forEach { (recipeType, recipeCardsUnexpanded) ->

                        if (recipeTypesSelected.isEmpty() || recipeTypesSelected.contains(recipeType)) {
                            if (recipeCardsUnexpanded.isNotEmpty()) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NourishH2(recipeType.title)
                                }

                                val list: MutableList<Pair<String, NourishColour>> = mutableListOf()
                                //uiState.allTags!!.map { tag -> Pair(tag.tag.displayName, enumValueOf<NourishColour>(tag.tag.colour))}.toMutableList()
                                uiState.recipeTags!!.forEach { tagSelection ->
                                    if (tagSelection.selected) {
                                        list.add(
                                            Pair(
                                                tagSelection.tag.displayName,
                                                enumValueOf<NourishColour>(tagSelection.tag.colour)
                                            )
                                        )
                                    }
                                }

                                val numberOfVisibleRecipes =
                                    recipeCardsUnexpanded.count { recipeCardUnexpanded ->
                                        recipeCardUnexpanded.title.contains(
                                            uiState.searchTerm,
                                            true
                                        ) && recipeCardUnexpanded.recipeTags.containsAll(list)
                                    }

                                if (numberOfVisibleRecipes != 0) {

                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp)
                                            .animateContentSize()
                                    ) {
                                        items(
                                            recipeCardsUnexpanded.reversed(), // reversed so most recent recipes first
                                            key = { it.recipeId }) { recipeCardUnexpanded ->
                                            AnimatedVisibility(
                                                visible = (recipeCardUnexpanded.title.contains(
                                                    uiState.searchTerm,
                                                    true
                                                ) && recipeCardUnexpanded.recipeTags.containsAll(
                                                    list
                                                )),
                                                enter = fadeIn(tween(300)),
                                                exit = fadeOut(tween(300))
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(horizontal = 4.dp) // padding goes here instead so it stays fine even when a box is invisibkle
                                                ) {
                                                    Card(
                                                        modifier = Modifier
                                                            .width(240.dp)
                                                            .height(210.dp)
                                                    ) {
                                                        Box(
                                                            Modifier.fillMaxSize()
                                                                .clickable {
                                                                    onViewRecipeCard(
                                                                        recipeCardUnexpanded.recipeId
                                                                    )
                                                                }
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.fillMaxSize()
                                                            ) {

                                                                ImageCardHeader(
                                                                    recipeCardUnexpanded.imagePath,
                                                                    recipeCardUnexpanded.emoji,
                                                                    context,
                                                                    recipeCardUnexpanded.colour.recipeColour()
                                                                )

                                                                Column(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                                    verticalArrangement = Arrangement.Center
                                                                ) {
                                                                    Text(
                                                                        text = recipeCardUnexpanded.title,
                                                                        textAlign = TextAlign.Center,
                                                                        modifier = Modifier.padding(
                                                                            4.dp
                                                                        ),
                                                                        fontWeight = FontWeight.Bold,
                                                                    )
                                                                }
                                                                Box(
                                                                    modifier = Modifier.fillMaxSize()
                                                                        .background(MaterialTheme.colorScheme.surfaceContainer),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    FlowRow(
                                                                        modifier = Modifier.padding(
                                                                            2.dp
                                                                        ),
                                                                        horizontalArrangement = Arrangement.spacedBy(
                                                                            2.dp
                                                                        ),
                                                                        verticalArrangement = Arrangement.spacedBy(
                                                                            2.dp
                                                                        ),
                                                                    ) {
                                                                        if (recipeCardUnexpanded.recipeTags.isEmpty()) {
                                                                            Text(
                                                                                text = "No tags",
                                                                                color = MaterialTheme.colorScheme.onBackground.copy(
                                                                                    0.5f
                                                                                )
                                                                            )
                                                                        } else {

                                                                            val toTake =
                                                                                determineSpaceAvailable(
                                                                                    recipeCardUnexpanded
                                                                                )
                                                                            val tags =
                                                                                recipeCardUnexpanded.recipeTags.take(
                                                                                    toTake
                                                                                )

                                                                            TagsDisplay(tags)
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
                                    }

                                    NourishDivider()

                                } else {
                                    Text("There are no ${recipeType.title} recipes which match the search/filter criteria.")
                                    NourishDivider()
                                }
                            } else {
                                Text("You have not yet saved any ${recipeType.title} recipes.")
                                NourishDivider()
                            }
                        }
                    }

                    // message for if user selected a type but does not have any of those recipes saved:

                    if (!recipeBookMap.keys.containsAll(recipeTypesSelected)) {
                        val selectedTypesWithoutRecipes = (recipeTypesSelected - recipeBookMap.keys)
                        val text = if (selectedTypesWithoutRecipes.size > 1) {
                            "You do not have any saved ${selectedTypesWithoutRecipes.joinToString(" or ") { it.title }} recipes."
                        } else {
                            "You do not have any saved ${selectedTypesWithoutRecipes.joinToString("") { it.title }} recipes."
                        }
                        Text(
                            text = text,
                            modifier = Modifier.padding(6.dp)
                        )

                    }
                }
            }

        } else {
            // display loading for when app first starts then display error msg for when no recipes yet
            RecipeLoadingBar(
                "Loading your recipes...",
                { Text("You have not saved any recipes!") })
        }

        // Add recipe manually button, for creating a recipe
        Row(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(12.dp),
                onClick = {
                    onAddRecipe()
                },
                text = {
                    Text("Add Manually")
                },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.add),
                        contentDescription = "Add Manually",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = 0.8f
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
    }
}

@Composable
fun ImageCardHeader(
    imagePath: String,
    emoji: String,
    context: Context,
    colour: Color,
    fillMaxWidth: Boolean = false
) {
    Box(
        modifier =
            if (fillMaxWidth) { Modifier.fillMaxWidth() } else { Modifier.width(240.dp)  }
                .height(
                    if (imagePath != "") {
                        115.dp
                    } else {
                        100.dp
                    }
                )
    ) {
        // Image:
        val imageFile =
            File(
                context.filesDir,
                imagePath
            )
        var displayImage = false
        if (imageFile.exists() && imagePath != "") {
            displayImage = true
            //Use Coil to display the selected image
            val painter =
                rememberAsyncImagePainter(
                    ImageRequest
                        .Builder(
                            LocalContext.current
                        )
                        .data(imageFile)
                        .build()
                )

            Image(
                painter = painter,
                contentDescription = null,
                modifier =
                    if (fillMaxWidth) { Modifier.fillMaxWidth() } else { Modifier.width(240.dp)  } // matches the card width
                    .height(100.dp), // 2/3 of the card height - maybe use consts for this in future to ensure they match upon changes
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = if (fillMaxWidth) { Modifier.fillMaxWidth() } else { Modifier.width(240.dp)  }// matches the card width
                    .height(100.dp)
                    .background(
                        color = colour
                    ) // put the chosen colour
            )
        }
        // Emoji:
        Text(
            text = emoji,
            fontSize = 32.sp,
            // center emoji if no image, otherwise put at bottom center
            modifier = if (displayImage) {
                Modifier.align(Alignment.BottomCenter)
            } else {
                Modifier.align(Alignment.Center)
            },
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(4f, 4f),
                    blurRadius = 4f
                )
            )
        )
    }
}

// holds the loading bar and a message for if no recipes exist yet after loading
@Composable
fun RecipeLoadingBar(
    loadingMessage: String,
    noContentPresent: @Composable () -> Unit
) {
    var noRecipes by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    // wait a bit for recipes to come
    LaunchedEffect(Unit) {
        delay(5000)
        loading = false
        noRecipes = true
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (loading) {
            Text(loadingMessage)
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(0.6f),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
        if (noRecipes) {
            noContentPresent.invoke()
        }
    }
}

@Composable
fun TagsDisplay(
    tags: List<Pair<String, NourishColour>>
) {
    tags.forEach { (tag, colour) ->
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
                        colour.dimmedBackgroundColour()
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
}

// determines if the length of the Tags belonging to a recipe can fit into one line (27 chars) or two lines (54 chars)
fun determineSpaceAvailable(
    recipeCardUnexpanded: RecipeCardUnexpanded,
): Int {
    // determine the amount of space available:
    var spaceAvailable = 54 // 2 lines of space available
    if (recipeCardUnexpanded.title.length > 27) {
        spaceAvailable = 27 // 1 line of space available
    }
    // then determine which tags to put:
    var fits = false
    var amountOfTagsToDisplay = recipeCardUnexpanded.recipeTags.size // begin with all tags, try to fit them all in
    while (!fits && amountOfTagsToDisplay > 0) {
        fits = ((recipeCardUnexpanded.recipeTags.take(amountOfTagsToDisplay).sumOf { (tag, _) ->
            tag.length
        }) <= spaceAvailable)
        if (!fits) {
            amountOfTagsToDisplay--
        }
    }
    return amountOfTagsToDisplay
}
