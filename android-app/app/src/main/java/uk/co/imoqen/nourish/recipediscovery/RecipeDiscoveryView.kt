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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.IndeterminateCircularIndicator
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishConfirmationDialog
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.NourishDivider
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishH4
import uk.co.imoqen.nourish.common.NourishIconButton
import uk.co.imoqen.nourish.common.NourishIconTextButton
import uk.co.imoqen.nourish.common.NourishSurfaceButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.NourishTextMessage
import uk.co.imoqen.nourish.common.SelectedTags
import uk.co.imoqen.nourish.common.TextSender
import uk.co.imoqen.nourish.common.TypesDisplayForMutuallyExclusiveFiltering
import uk.co.imoqen.nourish.common.recipeColour
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipebook.ImageCardHeader
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.common.tag.RecipeTagSelection

import java.io.File

/**
 * View for RecipeDiscovery, where LLM calls can be made and recipes thus generated.
 * @param viewModel - the view model holding methods and state for use in the view.
 */
@Composable
fun RecipeDiscoveryScreen(
    viewModel: RecipeDiscoveryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardFocusManager = LocalFocusManager.current // To close the keyboard once user submits input

    // for scrolling to bottom of the chat
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // set all nutritional tags and recipe types for the dropdown, only change when those change.
    LaunchedEffect(uiState.recipeTypes?.size) {
        Log.i("size:", "${uiState.recipeTypes?.size}")
        viewModel.getAllRecipeTypes()
    }
    LaunchedEffect(uiState.tagSheetState.recipeTags?.size) {
        Log.i("size:", "${uiState.tagSheetState.recipeTags?.size}")
        viewModel.getAllTagsForUser()
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
                            InitialMessage(viewModel, uiState)
                        }
                        item {
                            RecipeGenerationInfo(uiState)
                        }
                        item {
                            UserRequest(viewModel, uiState, context)
                        }
                        item {
                            // proposes possible dishes
                            RecipeDishProposalGenerationResult(viewModel, uiState)
                        }
                        item {
                            // user chosen recipe if dishes were proposed
                            UserDishChoice(uiState)
                        }
                        item {
                            // the recipe result
                            RecipeGenerationResult(viewModel, uiState)
                        }
                        item {
                            // any time after user has sent their request, they can choose to restart
                            // just not if awaiting response as would then be messed up as response would be received afterwards
                            // so check that the ordinal of the current stage is AFTER the ordinal of the awaiting response stage.
                            if (uiState.llmChatStage.ordinal > LlmChatStage.AWAITING_LLM_RESPONSE.ordinal) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    NourishIconTextButton(
                                        onClick = { viewModel.setShowRestartConfirmationDialog(true) },
                                        painter = painterResource(R.drawable.cross),
                                        description = "Restart",
                                    )
                                }
                            }
                            NourishConfirmationDialog(
                                show = uiState.showRestartConfirmationDialog,
                                title = "Confirm Restart",
                                description = "Are you sure you wish to restart the chat?\n\nAny unsaved recipes in this current instance will be lost.",
                                onConfirmation = { restart(viewModel, scope) },
                                onDismiss = { viewModel.setShowRestartConfirmationDialog(false) }
                            )
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

                // Only allow input if in that stage:
                if (uiState.llmChatStage == LlmChatStage.USER_INPUT) {
                    UserInputSection(viewModel, uiState, keyboardFocusManager)
                }

            }

        }
    }


    // Image expansion dialog overlay:
    if (uiState.displayImage) {
        Dialog(
            onDismissRequest = { viewModel.setDisplayImage(false) },

        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                // display the higher quality image
                if (uiState.savedImageMipmap.highQualityFilename != "") {
                    val context = LocalContext.current
                    val imageFile = File(context.filesDir, uiState.savedImageMipmap.highQualityFilename)
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
                                onClick = { viewModel.setDisplayImage(false) },
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
    viewModel: RecipeDiscoveryViewModel,
    scope: CoroutineScope
) {
    scope.launch {
        viewModel.setVisible(false)
        delay(500)
        viewModel.restart()
        viewModel.setVisible(true)
    }
}

@Composable
fun UserDishChoice(
    uiState: RecipeDiscoveryUiState,
) {
    // only display this bit if they have chosen a dish; that is, any mode which is not FROM DESCRIPTION.
    if (uiState.chosenDish != "" && uiState.userChoice != LlmMode.FROM_DESCRIPTION) {
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
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    context: Context
) {
    // if the current chat stage ordinal is greater than the ordinal of the stage where the user inputs,
    // they must have sent their input, therefore display their sent request.
    if (uiState.llmChatStage.ordinal > LlmChatStage.USER_INPUT.ordinal) {
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
                            UserUploadedImage(viewModel, uiState, context)
                        }
                    }

                    NourishUserRequestComponent(fillMaxWidth = false) {
                        Text(
                            text = "Dish type: ${uiState.selectedRecipeType?.title ?: "Any"}",
                            modifier = Modifier.padding(horizontal=6.dp)
                        )
                    }

                    val selectedTags = uiState.tagSheetState.recipeTags?.filter{ it.selected }
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
    fillMaxWidth: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = if (fillMaxWidth) {
            Modifier
                .padding(4.dp)
                .fillMaxWidth()
        } else {
            Modifier.padding(4.dp)
        }
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

// always displayed
@Composable
fun InitialMessage(
    viewModel: RecipeDiscoveryViewModel,
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
                    enabled = (uiState.llmChatStage.ordinal <= LlmChatStage.USER_INPUT.ordinal), // only enabled if user hasnt yet sent a request  - so if in any state before they have sent it.
                    onClick = {
                        viewModel.updateToInputStageGivenOptions(
                            LlmMode.FROM_DESCRIPTION, "Enter dish or description", LlmRequestType.RECIPE
                        )
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
                    enabled = (uiState.llmChatStage.ordinal <= LlmChatStage.USER_INPUT.ordinal), // only enabled if user hasnt yet sent a request
                    onClick = {
                        viewModel.updateToInputStageGivenOptions(
                            LlmMode.FROM_IMAGE, "Enter optional details", LlmRequestType.DISHES_FROM_IMAGE
                        )
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
                    enabled = (uiState.llmChatStage.ordinal <= LlmChatStage.USER_INPUT.ordinal), // only enabled if user hasnt yet sent a request
                    onClick = {
                        viewModel.updateToInputStageGivenOptions(
                            LlmMode.FROM_INGREDIENTS, "Enter ingredients", LlmRequestType.DISHES_FROM_INGREDIENTS
                        )
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
                        // never reached, this is for before the user clicks the option so nothing will be displayed.
                    }
                }
            }
        //}
    }
}

@Composable
fun RecipeDishProposalGenerationResult(
    viewModel: RecipeDiscoveryViewModel,
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
                                    generateRecipeFromChosenDish(
                                        dish,
                                        viewModel,
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
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState
) {
    Column( // column for the top contents
        modifier = Modifier.fillMaxWidth()
    ) {
        NourishTextMessage(
            textSender = TextSender.LLM,
            modifier = Modifier.align(Alignment.Start)
        ) {

            // Outputs the response, or error if occurred.
            if (uiState.llmGeneratedRecipe != null) {
                GeneratedRecipe(viewModel, uiState, uiState.llmGeneratedRecipe)
            }
        }
    }

    // the loading circle:
    IndeterminateCircularIndicator(uiState.llmChatStage == LlmChatStage.AWAITING_LLM_RESPONSE)

}

@Composable
fun GeneratedRecipe(
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    recipe: LlmRecipe
) {
    Log.i("recipe res", "not null")
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // higher quality image is displayed
        RecipeCardPreview(
            recipe, viewModel::formatIngredientText, uiState.savedImageMipmap.highQualityFilename,
            context, uiState.tagSheetState.recipeTags
        )

        NourishDivider()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom=8.dp)
        ) {
            Text(
                text = "Save the recipe to access it in the future, edit it, view nutritional information, or ask questions about it.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )

            NourishButton(
                onClick = { if (!uiState.recipeSaved) { viewModel.addRecipe(recipe) } },
                text = "Save Recipe",
                enabled = (!uiState.recipeSaved)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(modifier = Modifier.padding(bottom=16.dp))
            Text(
                "Please note that all recipes are produced by Gemini; as such, there may be mistakes.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                "Consult a dietitian or doctor for personalised, accurate nutritional advice.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecipeCardPreview(
    recipe: LlmRecipe,
    formatIngredientText: (LlmIngredient) -> String,
    imageFilename: String,
    context: Context,
    tags: List<TagSelection>? = emptyList(),
    alteredRecipe: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageCardHeader(
            imageFilename,
            recipe.emoji,
            context,
            enumValueOf<NourishColour>(recipe.colour.uppercase()).recipeColour(),
            fillMaxWidth = true // fill the width of the llm response.
        )

        NourishH2(recipe.title)

        NourishTextDivider("Ingredients")

        recipe.sectionIngredients.forEachIndexed { i, ingredientSection ->
            Box(
                modifier = Modifier.padding(6.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // only display section title if there are multiple sections
                    if (recipe.sectionIngredients.size > 1) {
                        Text(
                            text = ingredientSection.section,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Log.i("LLM RECIPE", "ingredients: ${ingredientSection.ingredients}")

                    ingredientSection.ingredients.forEach { ingredient ->
                        val ingredientText = if (alteredRecipe) {
                            // scale the ingredient if it is an alteration, because it is returned for 1 sevring unlike others which return for the actual number of servings.
                            formatIngredientText(ingredient.copy(ingredientAmount = ingredient.ingredientAmount * recipe.servings))
                        } else {
                            formatIngredientText(ingredient)
                        }
                        Text(ingredientText)
                    }

                    if (i != recipe.sectionIngredients.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 64.dp).padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        NourishTextDivider("Method")

        Box(
            modifier = Modifier
                .padding(10.dp)
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .height(200.dp) // constrain height so it doesnt take over so much when the method because method is long
                        .verticalScroll(rememberScrollState())
                ) {
                    recipe.method.forEachIndexed { i, step ->
                        Box(
                            modifier = Modifier.padding(6.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Step ${(i + 1)}",
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = step
                                )

                                if (i != recipe.method.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 64.dp)
                                            .padding(top = 6.dp)
                                    )
                                }

                            }
                        }
                    }
                }
            }
            // shadow to signify that the method box is scrollable
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(0.1f))
                        ),
                    )
            )
        }

        NourishTextDivider("Further information")

        Box(
            modifier = Modifier.padding(2.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Servings: ${recipe.servings}")

                Text("Meal type: ${recipe.mealType}")

                if (tags != null && tags.isNotEmpty()) {
                    SelectedTags(tags)
                }
            }
        }
    }
}

fun generateRecipeFromChosenDish(
    dish: String,
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    context: Context,
    dishes: LlmDishesProposed
) {
    // Set the user request depending on whether an image or ingredients were included
    // and add in the further user detail
    if (dishes.ingredientsIdentified != null) {
        viewModel.setChosenDish(dish + "\n\nUsing ingredients: ${dishes.ingredientsIdentified}")
    } else if (uiState.userRequest != "" ){
        viewModel.setChosenDish(dish + "\n\nFurther details: ${uiState.userRequest}") // if the user added further details
    } else {
        viewModel.setChosenDish(dish) // otherwise, the prompt is just the dish
    }

    // Make the LLM API call depending on whether an image or ingredients were included
    // lower quality image is sent to llm rather than high quality; mipmapping
    if (uiState.savedImageMipmap.lowQualityFilename == "") {
        // generate without image
        viewModel.makeLlmApiCall(LlmRequestType.RECIPE)
    } else {
        // generate with image
        viewModel.makeLlmApiCall(
            LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH,
            uiState.savedImageMipmap.lowQualityFilename,
            context.filesDir
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInputSection(
    viewModel: RecipeDiscoveryViewModel,
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
            viewModel.handleImage(uri, context)
            // update llm to work with dishes from ingredients image:
            if (uiState.userChoice == LlmMode.FROM_INGREDIENTS) {
                viewModel.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE)
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
                        if (uiState.savedImageMipmap.highQualityFilename != "") {
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
                    val filtersSelected = uiState.tagSheetState.recipeTags != null && uiState.tagSheetState.recipeTags.any { it.selected }
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
                                    viewModel.setDisplayRecipeTypeSelector(true)
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
                            tags = uiState.tagSheetState.recipeTags,
                            allowEdit = true,
                            updateSelectedTag = viewModel::updateSelected
                        )
                    }
                }
            }


            val nutritionalTagSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true // makes it fill the screen with the required amount rather than stuck at half the screen size
            )
            if (uiState.tagSheetState.filterDisplayState != FilterState.NONE) {
                RecipeTagSelection(
                    sheetState = nutritionalTagSheetState,
                    tagSheetState = uiState.tagSheetState,
                    tagSheetAction = viewModel::onTagSheetAction
                )
            }

            val recipeTypeSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true // makes it fill the screen with the required amount rather than stuck at half the screen size
            )

            if (uiState.displayRecipeTypeSelector) {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.setDisplayRecipeTypeSelector(false)
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
                            viewModel.uiState.collectAsState().value.recipeTypes
                        TypesDisplayForMutuallyExclusiveFiltering(
                            types,
                            uiState.selectedRecipeType,
                            viewModel::setSelectedRecipeType
                        )
                    }
                }
            }

            // IMAGE SELECTOR:
            // push the image to always be on the right side (end of the row). allow user to edit and del them
            Spacer(modifier = Modifier.weight(1f))
            UserUploadedImage(viewModel, uiState, context, pickMedia,
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
                        viewModel.setUserRequest(request)
                    },
                    placeholder = uiState.placeholderText,
                    onDone = { keyboardFocusManager.clearFocus() },
                    modifier =
                        // make the search bar fill the max width if: in from_description mode (cannot upload image), or an image has been uploaded (so cannot upload another)
                        if (uiState.userChoice == LlmMode.FROM_DESCRIPTION || uiState.savedImageMipmap != Mipmap("","")) {
                            Modifier.fillMaxWidth()
                        } // only leaves space for cam if needed
                        else {
                            Modifier.fillMaxWidth(0.85f)
                        }
                            .then(Modifier.padding(4.dp))
                            .animateBounds(this),
                    leadingIcon = ImageVector.vectorResource(R.drawable.filter_icon), // converts painterresource to imagevector. in future should move to all painterresource approach - recommended by google.
                    onLeadingIconPress = {
                        viewModel.setFilterDisplayState(FilterState.APPLY) // back to apply filter menu
                    },
                    trailingIcon = Icons.Filled.Send,
                    onTrailingIconPress = {
                        //viewModel.setChosenDish(uiState.userRequest)
                        sendToLlm(uiState, viewModel, context)
                    },
                    alwaysDisplayTrailingIcon = true
                )
                // only allow image upload if not exist already
                if (uiState.savedImageMipmap == Mipmap("","") && uiState.userChoice != LlmMode.FROM_DESCRIPTION) {
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
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    context: Context,
    pickMedia: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>? = null,
    allowEdit: Boolean = false,
    allowDelete: Boolean = false
) {
    // image is above if selected:
    val mipmapImageFilenames = uiState.savedImageMipmap
    if (mipmapImageFilenames != Mipmap("","")) {
        // display higher quality image to user
        val imageFile = File(context.filesDir, mipmapImageFilenames.highQualityFilename)
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
                            viewModel.setDisplayImage(true)
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
                                imageFile.delete(); // deletes higher quality image
                                // also delete lower quality image if it exists
                                val lowQualityImageFile = File(context.filesDir, mipmapImageFilenames.lowQualityFilename)
                                if (lowQualityImageFile.exists()) {
                                    lowQualityImageFile.delete()
                                }
                                viewModel.setSavedImageMipmap(Mipmap("",""))

                                // revert back to normal ingredient mode if in ingredient mode:
                                if (uiState.userChoice == LlmMode.FROM_INGREDIENTS) {
                                    viewModel.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS)
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
    viewModel: RecipeDiscoveryViewModel,
    context: Context
) {
    var validRequest = false
    val lowQualityFilename = uiState.savedImageMipmap.lowQualityFilename

    if (lowQualityFilename == "" && uiState.requestType == LlmRequestType.RECIPE && uiState.userRequest != "") {
        // generate recipe from text
        viewModel.makeLlmApiCall(uiState.requestType) // make llm call
        validRequest = true
        // then as the data has been sent and not needed anymore (as recipe generated), can delete the compressed image (low quality image file): as only need high quality after this.
        /*val lowQualityImageFile = File(context.filesDir, lowQualityFilename)
        if (lowQualityImageFile.exists()) {
            lowQualityImageFile.delete()
            // note that the compressed image can only be deleted here, not after dish generation etc as it will be reused when sending to the llm to generate the chosen recipe. so this is the only safe place to delete it.
        }*/
    }

    // generate potential dishes from ingredients list:
    else if (lowQualityFilename == "" && uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS && uiState.userRequest != "") {
        viewModel.makeLlmApiCall(uiState.requestType)
        validRequest = true
    }

    // generate potential dishes from image of ingredients, e.g. image of contents of a fridge:
    else if (lowQualityFilename != "" && uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE) {
        viewModel.makeLlmApiCall(
            uiState.requestType,
            lowQualityFilename,
            context.filesDir
        )
        validRequest = true
    }

    // generate potential dishes from an image of a dish, e.g. image of pasta:
    else if (lowQualityFilename != "" && uiState.requestType == LlmRequestType.DISHES_FROM_IMAGE) {
        // generate recipe from image
        viewModel.makeLlmApiCall(
            uiState.requestType,
            lowQualityFilename,
            context.filesDir
        )
        validRequest = true
    }

    else {
        viewModel.displayBlankInputError(uiState.requestType)
    }

    if (validRequest) {
        // update the chosen dish with the user's request, so that the user request can remain when the dish is chosen and the correct val thus sent to the llm
        //viewModel.setChosenDish(uiState.userRequest)
        // then reset the ui state, as data has been sent and was valid
        viewModel.setLlmChatStage(LlmChatStage.AWAITING_LLM_RESPONSE)
    }

}