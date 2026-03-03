package uk.co.imoqen.nourish.recipediscovery

import android.util.Log
import uk.co.imoqen.nourish.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishH4
import uk.co.imoqen.nourish.common.NourishIconButton
import uk.co.imoqen.nourish.common.NourishIconTextButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.NourishTextMessage
import uk.co.imoqen.nourish.common.SelectedTags
import uk.co.imoqen.nourish.common.TagsDisplayForFiltering
import uk.co.imoqen.nourish.common.TextSender
import uk.co.imoqen.nourish.common.TypesDisplayForFiltering
import uk.co.imoqen.nourish.network.request.LlmRequestType

import java.io.File

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    InitialMessage(recipeDiscoveryViewModel)
                }
                item {
                    RecipeGenerationInfo(uiState)
                }
                item {
                    RecipeGenerationResult(recipeDiscoveryViewModel, uiState)
                }
            }

            UserInputSection(recipeDiscoveryViewModel, uiState, keyboardFocusManager)

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
                            modifier = Modifier.fillMaxSize(0.9f).align(Alignment.Center)
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
                                modifier = Modifier.align(
                                    Alignment.CenterEnd
                                ).offset(x = (16).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun InitialMessage(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel
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
                    text = "Discover a recipe, by entering a...",
                    modifier = Modifier.padding(8.dp)
                )

                NourishIconTextButton(
                    onClick = {
                        recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_DESCRIPTION)
                        recipeDiscoveryViewModel.setPlaceholderText("Dish or description")
                        recipeDiscoveryViewModel.setRequestType(LlmRequestType.RECIPE)
                    },
                    painter = painterResource(R.drawable.cook),
                    description = "Dish or Description",
                    modifier = Modifier.padding(top = 8.dp)
                )

                NourishIconTextButton(
                    onClick = {
                        recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_IMAGE)
                        recipeDiscoveryViewModel.setPlaceholderText("Optional details")
                        recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_IMAGE)
                    },
                    painter = painterResource(R.drawable.camera),
                    description = "Photo of a Dish",
                    modifier = Modifier.padding(top = 8.dp)
                )

                NourishIconTextButton(
                    onClick = {
                        recipeDiscoveryViewModel.setUserChoice(LlmMode.FROM_INGREDIENTS)
                        recipeDiscoveryViewModel.setPlaceholderText("Ingredients")
                        recipeDiscoveryViewModel.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS)
                    },
                    painter = painterResource(R.drawable.ingredients),
                    description = "List or Photo of ingredients",
                    modifier = Modifier.padding(8.dp)
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
        if (uiState.userChoice != null) {
            when (uiState.userChoice) {
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
    }
}
@Composable
fun RecipeGenerationResult(
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
        Text("Choose a recipe to generate:")
        arrayOfDishes.forEach { dish ->
            Button(onClick = {
                if (dishes.ingredientsIdentified != null) {
                    recipeDiscoveryViewModel.setUserRequest(dish + ", using only ingredients: ${dishes.ingredientsIdentified}")
                } else {
                    recipeDiscoveryViewModel.setUserRequest(dish)
                }
                if (uiState.savedImageFilename == "") {
                    // generate without image
                    Log.i("GENERATING", "generating without image")
                    recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.RECIPE)
                } else {
                    // generate with image
                    Log.i("GENERATING", "generating with image")
                    recipeDiscoveryViewModel.makeLlmApiCall(
                        LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH,
                        uiState.savedImageFilename,
                        context.filesDir
                    )
                }
            }) {
                Text(dish)
            }
        }
        if (dishes.ingredientsIdentified != null) {
            Text("Ingredients identified: ${dishes.ingredientsIdentified}")
        }
    }

    // Outputs the response, or error if occurred.
    if (uiState.apiResponse != "") {
        Log.i("recipe res", "not null")
        Text(
            uiState.apiResponse,
            modifier = Modifier.fillMaxWidth()
        )
    }
    IndeterminateCircularIndicator()
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
            modifier = Modifier.padding(6.dp)
        ) {
            // FILTER SELECTOR:
            Box(
                modifier = Modifier.fillMaxWidth(
                    // if image, only take up half the screen to allow the image to have space.
                    if (uiState.savedImageFilename != "") { 0.5f } else { 1.0f }
                )
            ) {
                // Selected tags:
                SelectedTags(uiState.recipeTags, recipeDiscoveryViewModel::updateSelected)

                val sheetState = rememberModalBottomSheetState()
                if (uiState.displayFilters) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            recipeDiscoveryViewModel.setDisplayFilters(false)
                        },
                        sheetState = sheetState,
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            NourishTextDivider("Filter by tag(s)")
                            val tags =
                                recipeDiscoveryViewModel.uiState.collectAsState().value.recipeTags
                            TagsDisplayForFiltering(tags, recipeDiscoveryViewModel::updateSelected)

                            NourishTextDivider("Filter by type(s)")

                            val types =
                                recipeDiscoveryViewModel.uiState.collectAsState().value.recipeTypes
                            TypesDisplayForFiltering(
                                types,
                                recipeDiscoveryViewModel::updateSelected
                            )
                        }
                    }
                }
            }

            // IMAGE SELECTOR:

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

                    Box(
                        modifier = Modifier.padding(8.dp)
                            .height(200.dp).width(200.dp)
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
                            modifier = Modifier.align(
                                Alignment.TopEnd
                            ).offset(x = 16.dp),
                            backgroundColour = MaterialTheme.colorScheme.background
                        )
                        NourishIconButton(
                            onClick = {
                                pickMedia.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            painter = painterResource(R.drawable.edit),
                            modifier = Modifier.align(
                                Alignment.CenterEnd
                            ).offset(x = 16.dp),
                            backgroundColour = MaterialTheme.colorScheme.background
                        )
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
                            modifier = Modifier.align(
                                Alignment.BottomEnd
                            ).offset(x = 16.dp),
                            backgroundColour = MaterialTheme.colorScheme.background,
                            iconColour = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                        .then(Modifier.padding(4.dp)),
                leadingIcon = ImageVector.vectorResource(R.drawable.filter_icon), // converts painterresource to imagevector. in future should move to all painterresource approach - recommended by google.
                onLeadingIconPress = {
                    recipeDiscoveryViewModel.setDisplayFilters(true)
                },
                trailingIcon = Icons.Filled.Send,
                onTrailingIconPress = {

                    // generate the recipe:

                    if (uiState.savedImageFilename == "" && uiState.requestType == LlmRequestType.RECIPE) {
                        Log.i("recipe", "making recipe")
                        // generate recipe from text
                        recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.RECIPE) // make llm call
                    }
                    // generate potential dishes from ingredients list:
                    else if (uiState.savedImageFilename == "" && uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS) {
                        recipeDiscoveryViewModel.makeLlmApiCall(LlmRequestType.DISHES_FROM_INGREDIENTS)
                    }
                    // generate potential dishes from image of ingredients, e.g. image of contents of a fridge:
                    else if (uiState.savedImageFilename != "" && uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE) {
                        recipeDiscoveryViewModel.makeLlmApiCall(
                            LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE,
                            uiState.savedImageFilename,
                            context.filesDir
                        )
                    }
                    // generate potential dishes from an image of a dish, e.g. image of pasta:
                    else {
                        Log.i("recipe", "from image")
                        // generate recipe from image
                        recipeDiscoveryViewModel.makeLlmApiCall(
                            LlmRequestType.DISHES_FROM_IMAGE,
                            uiState.savedImageFilename,
                            context.filesDir
                        )
                    }
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

// Progress bar:
// https://developer.android.com/develop/ui/compose/components/progress
@Composable
fun IndeterminateCircularIndicator(
    recipeDiscoveryViewModel: RecipeDiscoveryViewModel = hiltViewModel()
) {
    val uiState by recipeDiscoveryViewModel.uiState.collectAsState()

    // if awaiting anything, need to show the loading screen
    if (!uiState.awaitingResponse) return

    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}