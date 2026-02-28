package uk.co.imoqen.nourish.recipebook

import uk.co.imoqen.nourish.R
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.backgroundColour
import uk.co.imoqen.nourish.common.dimmedBackgroundColour
import uk.co.imoqen.nourish.common.onBackgroundColour
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.tag.Tag
import java.io.File
import kotlin.enumValueOf


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
                    icon = Icons.Filled.Search,
                    onDone = { keyboardFocusManager.clearFocus() },
                    trailingIcon = Icons.Filled.Clear,
                    onValueReset = { recipeBookViewModel.setSearchTerm("") }
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


                        TagsDisplayForFiltering(recipeBookViewModel)
                        NourishTextDivider("Filter by type(s)")
                        TypesDisplayForFiltering(recipeBookViewModel)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
        ) {
            // set all recipes
            recipeBookViewModel.getRecipeBookMap()
            // set all tags for the dropdown
            recipeBookViewModel.getAllTagsForUser()

            recipeBookViewModel.setAllRecipeTypes()

            // then filter if a search has been made
            /*if (uiState.searchTerm != "") {
                recipeBookViewModel.filterRecipeBookMapByTitle()
            }*/
            RecipeList(recipeBookViewModel, onViewRecipeCard)
        }
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
            //contentPadding = PaddingValues(rhorizontal = 8.dp),
            //horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list: MutableList<Pair<String, NourishColour>> = mutableListOf()
                //uiState.allTags!!.map { tag -> Pair(tag.tag.displayName, enumValueOf<NourishColour>(tag.tag.colour))}.toMutableList()
            uiState.allTags!!.forEach { tagSelection ->
                if (tagSelection.selected) {
                    list.add(Pair(tagSelection.tag.displayName, enumValueOf<NourishColour>(tagSelection.tag.colour)))
                }
            }
            items(recipeCardsUnexpanded, key = {it.recipeId} ) { recipeCardUnexpanded ->
                AnimatedVisibility(
                    visible = (recipeCardUnexpanded.title.contains(uiState.searchTerm, true) && recipeCardUnexpanded.recipeTags.containsAll(list)),
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
                                        onViewRecipeCard(recipeCardUnexpanded.recipeId)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(240.dp) // matches the card width
                                            .height(
                                                if (recipeCardUnexpanded.imagePath != "") {
                                                    115.dp
                                                } else {
                                                    100.dp
                                                }
                                            )
                                    ) {
                                        // Image:
                                        val imageFile =
                                            File(context.filesDir, recipeCardUnexpanded.imagePath)
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
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = recipeCardUnexpanded.title,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(4.dp),
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FlowRow(
                                            modifier = Modifier.padding(2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {

                                            val toTake = determineSpaceAvailable(recipeCardUnexpanded)
                                            val tags = recipeCardUnexpanded.recipeTags.take(toTake)

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

@Composable
fun TypesDisplayForFiltering(
    recipeBookViewModel: RecipeBookViewModel,
) {
    val types = recipeBookViewModel.uiState.collectAsState().value.recipeTypes
    FlowRow(
        modifier = Modifier.padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        types?.forEach { (type, selected) ->
            Box(
                modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = type.title,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        .clickable {
                            recipeBookViewModel.updateSelectedType(
                                type,
                                !selected
                            ) // do the selection
                        }
                        .then(
                            if (selected) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .then(
                            Modifier.padding(4.dp).padding(horizontal = 2.dp)
                        ),
                    color = MaterialTheme.colorScheme.background

                )

            }
        }
    }
}

@Composable
fun TagsDisplayForFiltering(
    recipeBookViewModel: RecipeBookViewModel
) {
    val tags = recipeBookViewModel.uiState.collectAsState().value.allTags
    FlowRow(
        modifier = Modifier.padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        tags?.forEach { (tag, selected) ->
            // alternate colour of tag depending on dark / light mode
            val colour = enumValueOf<NourishColour>(tag.colour)

            Box(
                modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = tag.displayName,
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
                        .clickable {
                            recipeBookViewModel.updateSelectedTag(
                                tag,
                                !selected
                            ) // do the selection
                        }
                        .then(
                            if (selected) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .then(
                            Modifier.padding(4.dp).padding(horizontal=2.dp)
                        ),
                    color = if (isSystemInDarkTheme()) {
                        colour.backgroundColour()
                    } else {
                        colour.onBackgroundColour()
                    }
                )

            }
        }
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
