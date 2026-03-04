package uk.co.imoqen.nourish.recipediscovery

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import uk.co.imoqen.nourish.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishConfirmationDialog
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.NourishErrorText
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishH4
import uk.co.imoqen.nourish.common.NourishIconButton
import uk.co.imoqen.nourish.common.NourishIconTextButton
import uk.co.imoqen.nourish.common.NourishSurfaceButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.NourishTextMessage
import uk.co.imoqen.nourish.common.SelectedTags
import uk.co.imoqen.nourish.common.TagsDisplayForFiltering
import uk.co.imoqen.nourish.common.TextSender
import uk.co.imoqen.nourish.common.TypesDisplayForFiltering
import uk.co.imoqen.nourish.common.TypesDisplayForMutuallyExclusiveFiltering
import uk.co.imoqen.nourish.common.backgroundColour
import uk.co.imoqen.nourish.common.dimmedBackgroundColour
import uk.co.imoqen.nourish.common.onBackgroundColour
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed

import java.io.File
import kotlin.text.lowercase

/**
 * View for RecipeDiscovery, where LLM calls can be made and recipes thus generated.
 * @param recipeDiscoveryViewModel - the view model holding methods and state for use in the view.
 */
@Composable
fun RecipeDiscoveryScreen(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel
) {
    val uiState by recipeDiscoveryViewModel.uiState.collectAsState()
    val keyboardFocusManager = LocalFocusManager.current // To close the keyboard once user submits input

    // for scrolling to bottom of the chat
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // set all nutritional tags and recipe types for the dropdown, only change when those change.
    LaunchedEffect(uiState.recipeTypes?.size) {
        Log.i("size:", "${uiState.recipeTypes?.size}")
        recipeDiscoveryViewModel.getAllRecipeTypes()
    }
    LaunchedEffect(uiState.recipeTags?.size) {
        Log.i("size:", "${uiState.recipeTags?.size}")
        recipeDiscoveryViewModel.getAllTagsForUser()
    }

    Box( // for displaying the overlay if it is to be displayed
        modifier = Modifier.fillMaxSize()
    ) {

        AnimatedVisibility(
            visible = uiState.visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        item {
                            InitialMessage(recipeDiscoveryViewModel, uiState)
                        }
                        item {
                            RecipeGenerationInfo(uiState)
                        }
                        item {
                            UserRequest(recipeDiscoveryViewModel, uiState, context)
                        }
                        item {
                            // proposes possible dishes
                            RecipeDishProposalGenerationResult(recipeDiscoveryViewModel, uiState)
                        }
                        item {
                            // user chosen recipe if dishes were proposed
                            UserDishChoice(uiState)
                        }
                        item {
                            // the recipe result
                            RecipeGenerationResult(recipeDiscoveryViewModel, uiState)
                        }
                        item {
                            // any time after user has sent their request, they can choose to restart
                            // just not if awaiting response as would then be messed up as response would be received afterwards
                            if (uiState.userSentRequest && !uiState.awaitingResponse) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    NourishIconTextButton(
                                        onClick = { restart(recipeDiscoveryViewModel, scope) },
                                        painter = painterResource(R.drawable.cross),
                                        description = "Restart",
                                    )
                                }
                            }
                        }
                    }
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .height(40.dp)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.05f))
                                )
                            )
                    )
                }

                if (uiState.allowUserInput) {
                    UserInputSection(recipeDiscoveryViewModel, uiState, keyboardFocusManager)
                }

            }

        }
    }


    // Image expansion dialog overlay:
    if (uiState.displayImage) {
        Dialog(
            onDismissRequest = { recipeDiscoveryViewModel.setDisplayImage(false) },

        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                if (uiState.savedImageFilename != "") {
                    val context = LocalContext.current
                    val imageFile = File(context.filesDir, uiState.savedImageFilename)
                    if (imageFile.exists()) {
                        //Use Coil to display the selected image
                        val painter = rememberAsyncImagePainter(
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(imageFile)
                                .build()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.9f)
                                .align(Alignment.Center)
                                //.clipToBounds()
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = null,

                                // scale down the height of it
                                contentScale = ContentScale.Fit, // fits rather than scaels
                                modifier = Modifier.fillMaxSize()
                            )
                            // close button
                            NourishIconButton(
                                onClick = { recipeDiscoveryViewModel.setDisplayImage(false) },
                                painter = painterResource(R.drawable.collapse),
                                modifier = Modifier
                                    .align(
                                        Alignment.CenterEnd
                                    )
                                    .offset(x = (16).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// smooth transition between states when restarting: fades out and then back in via animatedvisibility
fun restart(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    scope: CoroutineScope
) {
    scope.launch {
        recipeDiscoveryViewModel.setVisible(false)
        delay(500)
        recipeDiscoveryViewModel.restart()
        recipeDiscoveryViewModel.setVisible(true)
    }
}

@Composable
fun UserDishChoice(
    uiState: RecipeDiscoveryUiState,
) {
    if (uiState.chosenDish != "") {
        Column( // column for the top contents
            modifier = Modifier.fillMaxWidth()
        ) {
            NourishTextMessage(
                textSender = TextSender.USER,
                modifier = Modifier.align(Alignment.End)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Generate a recipe for",
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = uiState.chosenDish,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun UserRequest(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    context: Context
) {
    if (uiState.userSentRequest) {
        Column( // column for the top contents
            modifier = Modifier.fillMaxWidth()
        ) {
            NourishTextMessage(
                textSender = TextSender.USER,
                modifier = Modifier.align(Alignment.End)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NourishUserRequestComponent {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (uiState.userRequest != "") {
                                Text(
                                    text = uiState.userRequest,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            UserUploadedImage(recipeDiscoveryViewModel, uiState, context)
                        }
                    }

                    NourishUserRequestComponent {
                        Text("Dish type: ${uiState.selectedRecipeType?.title ?: "Any"}")
                    }

                    val selectedTags = uiState.recipeTags?.filter{ it.selected }
                    if (selectedTags != null  && selectedTags.isNotEmpty()) {
                        NourishUserRequestComponent {
                            SelectedTags(
                                tags = selectedTags,
                                allowEdit = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NourishUserRequestComponent(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}


@Composable
fun InitialMessage(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
) {
    Column( // column for the top contents
        modifier = Modifier.fillMaxWidth()
    ) {
        NourishTextMessage(
            textSender = TextSender.LLM,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Box(
                modifier = Modifier.padding(8.dp)
            ) {
                NourishH3(
                    text = "Let's find a recipe for you.",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        NourishTextMessage(
            textSender = TextSender.USER,
            modifier = Modifier.align(Alignment.End)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NourishH4(
                    text = "I would like to discover a recipe, by entering a...",
                    modifier = Modifier.padding(horizontal=8.dp)
                )

                NourishIconTextButton(
                    enabled = (!uiState.userSentRequest), // only enabled if user hasnt yet sent a request
                    onClick = {
                        recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_DESCRIPTION)
                        recipeDiscoveryViewModel.setPlaceholderText("Enter dish or description")
                        recipeDiscoveryViewModel.setRequestType(LlmRequestType.RECIPE)
                        // ensure no image can be uploaded:
                        recipeDiscoveryViewModel.setSavedImageFilename("")
                        // allow user input:
                        recipeDiscoveryViewModel.setAllowUserInput(true)
                    },
                    painter = painterResource(R.drawable.cook),
                    description = "Dish or Description",
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(0.9f),
                    selected = uiState.userChoice == LlmMode.FROM_DESCRIPTION,
                    fillMaxWidth = true
                )

                NourishIconTextButton(
                    enabled = (!uiState.userSentRequest), // only enabled if user hasnt yet sent a request
                    onClick = {
                        recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_IMAGE)
                        recipeDiscoveryViewModel.setPlaceholderText("Enter optional details")
                        recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_IMAGE)
                        // allow user input:
                        recipeDiscoveryViewModel.setAllowUserInput(true)
                    },
                    painter = painterResource(R.drawable.camera),
                    description = "Photo of a Dish",
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(0.9f),
                    selected = uiState.userChoice == LlmMode.FROM_IMAGE,
                    fillMaxWidth = true
                )

                NourishIconTextButton(
                    enabled = (!uiState.userSentRequest), // only enabled if user hasnt yet sent a request
                    onClick = {
                        recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_INGREDIENTS)
                        recipeDiscoveryViewModel.setPlaceholderText("Enter ingredients")
                        recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS)
                        // allow user input:
                        recipeDiscoveryViewModel.setAllowUserInput(true)
                    },
                    painter = painterResource(R.drawable.ingredients),
                    description = "List or Photo of ingredients",
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(0.9f),
                    selected = uiState.userChoice == LlmMode.FROM_INGREDIENTS,
                    fillMaxWidth = true
                )

            }
        }
    }
}

@Composable
fun RecipeGenerationInfo(
    uiState: RecipeDiscoveryUiState
) {
    Column() {
        //if (uiState.userChoice != null) {
            AnimatedContent(
                targetState = uiState.userChoice
            ) { state ->
                when (state) {
                    LlmMode.FROM_DESCRIPTION -> {
                        NourishTextMessage(
                            textSender = TextSender.LLM,
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                NourishH4(
                                    text = "Enter the name or description of a dish.",
                                    modifier = Modifier.padding(8.dp)
                                )
                                NourishTextDivider("Tip")

                                Text(
                                    text = "The dish does not need to be precise; for example, you can enter 'something sweet but healthy ...'",
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    LlmMode.FROM_IMAGE -> {
                        NourishTextMessage(
                            textSender = TextSender.LLM,
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                NourishH4(
                                    text = "Upload the image of the dish.",
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                )

                                NourishTextDivider("Tip")

                                Text(
                                    text = "You can also provide further information as text, if you wish.",
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    LlmMode.FROM_INGREDIENTS -> {
                        NourishTextMessage(
                            textSender = TextSender.LLM,
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                NourishH4(
                                    text = "List your ingredients below, or upload an image of them.",
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                )

                                NourishTextDivider("Tip")

                                Text(
                                    text = "Try uploading an image of the contents of your fridge!",
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                        }
                    }

                    null -> {
                        // never reached
                    }
                }
            }
        //}
    }
}

@Composable
fun RecipeDishProposalGenerationResult(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState
) {
    // result from LLM:
    val context = LocalContext.current
    val dishes = uiState.dishes
    if (dishes != null) {
        val dishes = uiState.dishes
        val arrayOfDishes = listOf(
            dishes.firstMostLikelyDish,
            dishes.secondMostLikelyDish,
            dishes.thirdMostLikelyDish,
            dishes.fourthMostLikelyDish
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            NourishTextMessage(
                textSender = TextSender.LLM,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom=8.dp)
                ) {
                    NourishH4(
                        text = "The following dishes have been identified.",
                        modifier= Modifier.padding(top=8.dp)
                    )
                    NourishTextDivider("Select one to generate a recipe")

                    arrayOfDishes.forEach { dish ->
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            NourishSurfaceButton(
                                onClick = {
                                    recipeDiscoveryViewModel.setChosenDish(dish) // set the chosen dish
                                    generateRecipe(
                                        dish,
                                        recipeDiscoveryViewModel,
                                        uiState,
                                        context,
                                        dishes
                                    )
                                },
                                text = dish,
                                enabled = (uiState.chosenDish == "") // only enabled if user has not yet chosen the dish.
                            )
                        }
                    }

                    if (dishes.ingredientsIdentified != null) {
                        NourishTextDivider("Ingredients identified")
                        Text(
                            text = dishes.ingredientsIdentified,
                            modifier = Modifier.padding(horizontal=8.dp).padding(top=8.dp)
                        )
                    }
                }

            }
        }

    }
}

@Composable
fun RecipeGenerationResult(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState
) {
    // Outputs the response, or error if occurred.
    if (uiState.apiResponse != "") {
        Log.i("recipe res", "not null")
        Text(
            uiState.apiResponse,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // the loading circle:
    IndeterminateCircularIndicator()
}

fun generateRecipe(
    dish: String,
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    context: Context,
    dishes: LlmDishesProposed
) {
    // Set the user request depending on whether an image or ingredients were included
    if (dishes.ingredientsIdentified != null) {
        recipeDiscoveryViewModel.setChosenDish(dish + ", using only ingredients: ${dishes.ingredientsIdentified}")
    } else {
        recipeDiscoveryViewModel.setChosenDish(dish)
    }

    // Make the LLM API call depending on whether an image or ingredients were included
    if (uiState.savedImageFilename == "") {
        // generate without image
        recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.RECIPE)
    } else {
        // generate with image
        recipeDiscoveryViewModel.makeLlmApiCall(
            LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH,
            uiState.savedImageFilename,
            context.filesDir
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInputSection(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    keyboardFocusManager: FocusManager
) {
    // BOTTOM OF SCREEN: box for textbox user input
    // if image is uploaded, change request type for the ingredient case

    val context = LocalContext.current
    // Photo picker: https://developer.android.com/training/data-storage/shared/photo-picker#compose
    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            recipeDiscoveryViewModel.handleImage(uri, context)
            // update llm to work with dishes from ingredients image:
            if (uiState.userChoice == LlmMode.FROM_INGREDIENTS) {
                recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE)
            }
        }

    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainer
            )
    ) {

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom, // push everything to bottom, particularly for nutriitonal filters button
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .padding(top = 12.dp) // extra to make distance between text input box and top of box
                .fillMaxWidth()
        ) {
            // FILTER SELECTOR: only display if at least one tag has been selected
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        // if image, only take up half the screen to allow the image to have space.
                        if (uiState.savedImageFilename != "") {
                            0.6f
                        } else {
                            1.0f
                        }
                    )
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(6.dp)
                    .animateContentSize()
            ) {
                Column() {
                    val filtersSelected = uiState.recipeTags != null && uiState.recipeTags.any { it.selected }
                    // Selected dish type:
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = if (filtersSelected) { Modifier.padding(bottom=4.dp) } else { Modifier }
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                )
                                .clip(RoundedCornerShape(12.dp)) // clip stops the clickable element from exceeding the rounded corner shape
                                .clickable {
                                    recipeDiscoveryViewModel.setDisplayRecipeTypeSelector(true)
                                }
                                .padding(4.dp)
                                .padding(horizontal = 2.dp)

                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dish Type: ${uiState.selectedRecipeType?.title ?: "Any"}",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end=4.dp)
                                )
                                Icon(
                                    painter = painterResource(R.drawable.edit),
                                    contentDescription = "Edit dish type",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Selected tags:
                    if (filtersSelected) { // only display if at least one has been selected, not if all unselected
                        SelectedTags(
                            tags = uiState.recipeTags,
                            allowEdit = true,
                            updateSelectedTag = recipeDiscoveryViewModel::updateSelected
                        )
                    }
                }
            }


            val nutritionalTagSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true // makes it fill the screen with the required amount rather than stuck at half the screen size
            )
            if (uiState.filterDisplayState != FilterState.NONE) {
                ModalBottomSheet(
                    onDismissRequest = {
                        recipeDiscoveryViewModel.setFilterDisplayState(FilterState.NONE)
                    },
                    sheetState = nutritionalTagSheetState
                ) {

                    val keyboardFocusManager = LocalFocusManager.current

                    val tags = recipeDiscoveryViewModel.uiState.collectAsState().value.recipeTags

                    AnimatedContent(
                        targetState = uiState.filterDisplayState,
                    ) { filterDisplayState ->
                        when (filterDisplayState) {
                            FilterState.APPLY -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(bottom = 20.dp)
                                ) {
                                    NourishTextDivider("Filter by Nutritional Tag(s)")

                                    TagsDisplayForFiltering(
                                        tags = tags,
                                        onTagPressed = recipeDiscoveryViewModel::updateSelected,
                                        displayAddButton = true,
                                        onAddButtonPressed = { recipeDiscoveryViewModel.setFilterDisplayState(FilterState.CREATE) },
                                        displayEditButton = true,
                                        onEditButtonPressed = { recipeDiscoveryViewModel.setFilterDisplayState(FilterState.EDIT) }
                                    )
                                }
                            }

                            FilterState.CREATE -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 20.dp) // to add padding to bottom to ensure not too close to bottom of screen
                                        .animateContentSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {

                                    NourishTextDivider(
                                        "Create a Nutritional Filter",
                                        modifier = Modifier.padding(top = 6.dp)
                                    )

                                    NourishTextField(
                                        value = uiState.createdTagName,
                                        onValueChange = { recipeDiscoveryViewModel.setCreatedTagName(it) },
                                        placeholder = "Enter a description, e.g. 'Vegan'",
                                        onDone = { keyboardFocusManager.clearFocus() }
                                    )

                                    // identifier is created by making the display name all caps and replacing spaces with -. only alphanumerics can be added. so need to vberify this.

                                    NourishTextDivider(
                                        "Choose a colour",
                                        modifier = Modifier.padding(top = 6.dp)
                                    )

                                    // colour picker too from predefined colours.
                                    // 3x3 as there are 9 colours
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        items(NourishColour.entries) { colour ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(3.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) { // simply for padding between tags
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            color =
                                                                if (isSystemInDarkTheme()) {
                                                                    colour.onBackgroundColour()
                                                                } else {
                                                                    colour.dimmedBackgroundColour()
                                                                },
                                                            shape = RoundedCornerShape(16.dp)
                                                        )
                                                        .clickable {
                                                            recipeDiscoveryViewModel.setCreatedTagColour(
                                                                colour
                                                            )
                                                        }
                                                        .then(
                                                            // highlight if this is the selected colour for the tag:
                                                            if (uiState.createdTagColour == colour) {
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
                                                            Modifier.padding(4.dp)
                                                        )
                                                ) {
                                                    Text(
                                                        text = colour.name.lowercase()
                                                            .replaceFirstChar { it.uppercase() },
                                                        color =
                                                            if (isSystemInDarkTheme()) {
                                                                colour.backgroundColour()
                                                            } else {
                                                                colour.onBackgroundColour()
                                                            },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        NourishIconTextButton(
                                            onClick = { recipeDiscoveryViewModel.createTag(uiState.createdTagName, uiState.createdTagColour) },
                                            painter = painterResource(R.drawable.tick),
                                            description = "Create tag"
                                        )
                                        NourishIconTextButton(
                                            onClick = {
                                                recipeDiscoveryViewModel.closeTagCreation()
                                            },
                                            painter = painterResource(R.drawable.cross),
                                            description = "Discard tag"
                                        )
                                    }

                                    if (uiState.tagCreationError != "") {
                                        NourishErrorText(uiState.tagCreationError)
                                    }
                                }
                            }

                            FilterState.EDIT -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    NourishTextDivider("Delete a Nutritional Filter")

                                    TagsDisplayForFiltering(
                                        tags = tags,
                                        trailingIcon = painterResource(R.drawable.delete),
                                        onTrailingIconPress = {
                                            recipeDiscoveryViewModel.setShowConfirmationDialog(true)
                                        },
                                        setTagOnTrailingIconPress = recipeDiscoveryViewModel::setTagToDelete
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                    NourishIconTextButton(
                                        onClick = { recipeDiscoveryViewModel.setFilterDisplayState(FilterState.APPLY) },
                                        painter = painterResource(R.drawable.arrow_left),
                                        description = "Back",
                                    )
                                }

                                NourishConfirmationDialog(
                                    show = uiState.showConfirmationDialog,
                                    title = "Confirm Tag Deletion",
                                    description = "Are you sure you wish to delete this nutritional tag?\n\nThis action cannot be undone.",
                                    onConfirmation = { recipeDiscoveryViewModel.deleteTag() },
                                    onDismiss = { recipeDiscoveryViewModel.setShowConfirmationDialog(false) }
                                )
                            }

                            FilterState.NONE -> {
                                // do nothing - unreachable. only to satisfy compiler.
                            }
                        }
                    }
                }
            }

            val recipeTypeSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true // makes it fill the screen with the required amount rather than stuck at half the screen size
            )

            if (uiState.displayRecipeTypeSelector) {
                ModalBottomSheet(
                    onDismissRequest = {
                        recipeDiscoveryViewModel.setDisplayRecipeTypeSelector(false)
                    },
                    sheetState = recipeTypeSheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NourishTextDivider(
                            "Choose a type for the dish",
                        )
                        Text(
                            "If no type is provided, the resulting recipe will be a dish of the most appropriate type.",
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(vertical = 10.dp)
                        )

                        val types =
                            recipeDiscoveryViewModel.uiState.collectAsState().value.recipeTypes
                        TypesDisplayForMutuallyExclusiveFiltering(
                            types,
                            uiState.selectedRecipeType,
                            recipeDiscoveryViewModel::setSelectedRecipeType
                        )
                    }
                }
            }

            // IMAGE SELECTOR:
            // push the image to always be on the right side (end of the row). allow user to edit and del them
            Spacer(modifier = Modifier.weight(1f))
            UserUploadedImage(recipeDiscoveryViewModel, uiState, context, pickMedia,
                allowEdit = true,
                allowDelete = true
            )

        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LookaheadScope {
                // text box for user text input:
                NourishTextField(
                    value = uiState.userRequest,
                    onValueChange = { request: String ->
                        recipeDiscoveryViewModel.setUserRequest(request)
                    },
                    placeholder = uiState.placeholderText,
                    onDone = { keyboardFocusManager.clearFocus() },
                    modifier =
                        // make the search bar fill the max width if: in from_description mode (cannot upload image), or an image has been uploaded (so cannot upload another)
                        if (uiState.userChoice == LlmMode.FROM_DESCRIPTION || uiState.savedImageFilename != "") {
                            Modifier.fillMaxWidth()
                        } // only leaves space for cam if needed
                        else {
                            Modifier.fillMaxWidth(0.85f)
                        }
                            .then(Modifier.padding(4.dp))
                            .animateBounds(this),
                    leadingIcon = ImageVector.vectorResource(R.drawable.filter_icon), // converts painterresource to imagevector. in future should move to all painterresource approach - recommended by google.
                    onLeadingIconPress = {
                        recipeDiscoveryViewModel.setFilterDisplayState(FilterState.APPLY) // back to apply filter menu
                    },
                    trailingIcon = Icons.Filled.Send,
                    onTrailingIconPress = {
                        recipeDiscoveryViewModel.setChosenDish(uiState.userRequest)
                        sendToLlm(uiState, recipeDiscoveryViewModel, context)
                    },
                    alwaysDisplayTrailingIcon = true
                )
                // only allow image upload if not exist already
                if (uiState.savedImageFilename == "" && uiState.userChoice != LlmMode.FROM_DESCRIPTION) {
                    NourishIconButton(
                        onClick = {
                            pickMedia.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        painter = painterResource(R.drawable.camera),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
    }

}

@Composable
fun UserUploadedImage(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    context: Context,
    pickMedia: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>? = null,
    allowEdit: Boolean = false,
    allowDelete: Boolean = false
) {
    // image is above if selected:
    if (uiState.savedImageFilename != "") {
        val imageFile = File(context.filesDir, uiState.savedImageFilename)
        if (imageFile.exists()) {
            //Use Coil to display the selected image
            val painter = rememberAsyncImagePainter(
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(imageFile)
                    .build()
            )

            LookaheadScope {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(140.dp)
                        .width(140.dp)
                        .animateBounds(this) // allows it to animate
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        // scale down the height of it
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()

                    )

                    NourishIconButton(
                        onClick = {
                            recipeDiscoveryViewModel.setDisplayImage(true)
                        },
                        painter = painterResource(R.drawable.expand),
                        modifier = Modifier
                            .align(
                                Alignment.TopEnd
                            )
                            .offset(x = 16.dp),
                        backgroundColour = MaterialTheme.colorScheme.background
                    )

                    if (allowEdit && pickMedia != null) {
                        NourishIconButton(
                            onClick = {
                                pickMedia.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            painter = painterResource(R.drawable.edit),
                            modifier = Modifier
                                .align(
                                    Alignment.CenterEnd
                                )
                                .offset(x = 16.dp),
                            backgroundColour = MaterialTheme.colorScheme.background
                        )
                    }
                    if (allowDelete) {
                        NourishIconButton(
                            onClick = {
                                imageFile.delete(); recipeDiscoveryViewModel.setSavedImageFilename(
                                ""
                            )
                                // revert back to normal ingredient mode if in ingredient mode:
                                if (uiState.userChoice == LlmMode.FROM_INGREDIENTS) {
                                    recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS)
                                }
                            },
                            painter = painterResource(R.drawable.delete),
                            modifier = Modifier
                                .align(
                                    Alignment.BottomEnd
                                )
                                .offset(x = 16.dp),
                            backgroundColour = MaterialTheme.colorScheme.background,
                            iconColour = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// generates the recipe based on user input: makes the llm api call
fun sendToLlm(
    uiState: RecipeDiscoveryUiState,
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel,
    context: Context
) {
    var validRequest = false

    if (uiState.savedImageFilename == "" && uiState.requestType == LlmRequestType.RECIPE && uiState.userRequest != "") {
        // generate recipe from text
        recipeDiscoveryViewModel.makeLlmApiCall(uiState.requestType) // make llm call
        validRequest = true
    }

    // generate potential dishes from ingredients list:
    else if (uiState.savedImageFilename == "" && uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS && uiState.userRequest != "") {
        recipeDiscoveryViewModel.makeLlmApiCall(uiState.requestType)
        validRequest = true
    }

    // generate potential dishes from image of ingredients, e.g. image of contents of a fridge:
    else if (uiState.savedImageFilename != "" && uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE) {
        recipeDiscoveryViewModel.makeLlmApiCall(
            uiState.requestType,
            uiState.savedImageFilename,
            context.filesDir
        )
        validRequest = true
    }

    // generate potential dishes from an image of a dish, e.g. image of pasta:
    else if (uiState.savedImageFilename != "" && uiState.requestType == LlmRequestType.DISHES_FROM_IMAGE) {
        // generate recipe from image
        recipeDiscoveryViewModel.makeLlmApiCall(
            uiState.requestType,
            uiState.savedImageFilename,
            context.filesDir
        )
        validRequest = true
    }

    else {
        recipeDiscoveryViewModel.displayBlankInputError(uiState.requestType)
    }

    if (validRequest) {
        // update the chosen dish with the user's request, so that the user request can remain when the dish is chosen and the correct val thus sent to the llm
        recipeDiscoveryViewModel.setChosenDish(uiState.userRequest)
        // then reset the ui state, as data has been sent and was valid
        recipeDiscoveryViewModel.setAllowUserInput(false)
        recipeDiscoveryViewModel.setUserSentRequest(true)
    }

}

// Progress bar:
// https://developer.android.com/develop/ui/compose/components/progress
@Composable
fun IndeterminateCircularIndicator(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel = hiltViewModel()
) {
    val uiState by recipeDiscoveryViewModel.uiState.collectAsState()

    // if awaiting anything, need to show the loading screen
    if (!uiState.awaitingResponse) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}