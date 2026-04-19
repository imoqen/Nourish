package uk.co.imoqen.nourish.recipediscovery

import android.content.Context
import android.net.Uri
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
import androidx.compose.ui.platform.testTag
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
    // To close the keyboard once user submits input:
    val keyboardFocusManager = LocalFocusManager.current

    // for scrolling to bottom of the chat
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // set all nutritional tags and recipe types for the dropdown, only change when those change.
    LaunchedEffect(uiState.recipeTypes?.size) {
        viewModel.getAllRecipeTypes()
    }
    LaunchedEffect(uiState.tagSheetState.recipeTags?.size) {
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
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier.testTag("recipeDiscoveryLazyColumn") // test identification
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
                            // just not if awaiting response as would then be messed up as response
                            // would be received afterwards so check that the ordinal of the current
                            // stage is AFTER the ordinal of the awaiting response stage.
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

    // Image expansion Dialog overlay:
    if (uiState.displayImage) {
        Dialog(
            onDismissRequest = { viewModel.setDisplayImage(false) },

        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                // Display the higher quality image
                if (uiState.savedImageMipmap.highQualityFilename != "") {
                    val context = LocalContext.current
                    val imageFile = File(context.filesDir, uiState.savedImageMipmap.highQualityFilename)
                    if (imageFile.exists()) {
                        // Use Coil to display the selected image:
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
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = null,
                                // scale down the height of it:
                                contentScale = ContentScale.Fit, // fits rather than scales
                                modifier = Modifier.fillMaxSize()
                            )
                            // Close button:
                            NourishIconButton(
                                onClick = { viewModel.setDisplayImage(false) },
                                painter = painterResource(R.drawable.collapse),
                                modifier = Modifier
                                    .align(
                                        Alignment.CenterEnd
                                    )
                                    .offset(x = (16).dp),
                                contentDescription = "Close"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Function to create a smooth transition between states when restarting: fades out and then back
 * in, via AnimatedVisibility
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param scope - CoroutineScope for 500ms delay.
 */
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

/**
 * The text message to display from the user, with the dish they have chosen.
 * This is used after an image / ingredients have been sent and possible recipes generated.
 * @param uiState - The Ui State displayed in the view.
 */
@Composable
fun UserDishChoice(
    uiState: RecipeDiscoveryUiState,
) {
    // only display this bit if they have chosen a dish; that is, any mode which is not from description.
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

/**
 * The user request text message, depicting the dish they wish to cook, meal type, and chosen
 * nutritional tags.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 * @param context - The Context, required to display an image if uploaded.
 */
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

/**
 * The individual parts to the UserRequest; name, meal type, and tags.
 * @param fillMaxWidth - Boolean reflecting whether the part should fill the max width of the request
 * @param content - Content within the individual part.
 */
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

/**
 * The initial message, to explain to the user how to use the LLM interface. This is always displayed.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 */
@Composable
fun InitialMessage(
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
) {
    Column(
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
                    // only enabled if user hasn't yet sent a request  - so if in any state before
                    // they have sent it.
                    enabled = (uiState.llmChatStage.ordinal <= LlmChatStage.USER_INPUT.ordinal),
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
                    // only enabled if user hasn't yet sent a request.
                    enabled = (uiState.llmChatStage.ordinal <= LlmChatStage.USER_INPUT.ordinal),
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
                    // only enabled if user hasn't yet sent a request.
                    enabled = (uiState.llmChatStage.ordinal <= LlmChatStage.USER_INPUT.ordinal),
                    onClick = {
                        viewModel.updateToInputStageGivenOptions(
                            LlmMode.FROM_INGREDIENTS, "Enter ingredients", LlmRequestType.DISHES_FROM_INGREDIENTS
                        )
                    },
                    painter = painterResource(R.drawable.ingredients),
                    description = "List or Photo of Ingredients",
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

/**
 * Displays the information upon the recipe generation type being selected.
 * @param uiState - The Ui State displayed in the view.
 */
@Composable
fun RecipeGenerationInfo(
    uiState: RecipeDiscoveryUiState
) {
    Column() {
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
                    // Never reached, this is for before the user clicks the option so nothing will
                    // be displayed.
                }
            }
        }
    }
}

/**
 * Outputs the result of the 4x dishes proposed by the LLM, in a list consisting of 4
 * clickable buttons.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 */
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
                                // only enabled if user has not yet chosen the dish:
                                enabled = (uiState.chosenDish == "")
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

/**
 * Outputs the result of the generated recipe.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 */
@Composable
fun RecipeGenerationResult(
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState
) {
    Column( // Column for the top contents
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
    // The loading circle:
    IndeterminateCircularIndicator(uiState.llmChatStage == LlmChatStage.AWAITING_LLM_RESPONSE)
}

/**
 * The generated recipe, outputted in RecipeGenerationResult, with actions to save the Recipe or
 * generate a new one.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 * @param recipe - The generated LlmRecipe.
 */
@Composable
fun GeneratedRecipe(
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    recipe: LlmRecipe
) {
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

/**
 * The preview of the LLM-generated Recipe, before saved to the RecipeCard.
 * @param recipe - The generated LlmRecipe.
 * @param formatIngredientText - Function to format the text of an Ingredient.
 * @param imageFilename - The filename of the image attached to the RecipeCard.
 * @param context - The Context, used to display the image.
 * @param tags - The list of Tags attached to this recipe.
 * @param alteredRecipe - Boolean reflecting whether this is a recipe alteration, or a freshly
 *                        generated recipe.
 */
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
                    ingredientSection.ingredients.forEach { ingredient ->
                        val ingredientText = if (alteredRecipe) {
                            // scale the ingredient if it is an alteration, because it is returned
                            // for 1 serving unlike others which return for the actual number of
                            // servings.
                            formatIngredientText(ingredient.copy(
                                ingredientAmount = ingredient.ingredientAmount * recipe.servings
                            ))
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
                        // constrain height so it doesn't take over so much when the method,
                        // because method is long
                        .height(200.dp)
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
            // Shadow to signify that the method box is scrollable:
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

/**
 * Function to call the view model's API call function, to generate a recipe when a dish is chosen
 * @param dish - The dish which has been selected, to generate a recipe of.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 * @param context - The Context, required to display an image if uploaded.
 * @param dishes - The 4x Dishes which were proposed by the LLM, which the user selected one of.
 */
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
        // if the user added further details
        viewModel.setChosenDish(dish + "\n\nFurther details: ${uiState.userRequest}")
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

/**
 * The user input textbox section, placed at the bottom of the screen, where the user can enter the
 * dish, select nutritional tags, choose a dish type, and upload an image.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 * @param keyboardFocusManager - The keyboard focus manager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInputSection(
    viewModel: RecipeDiscoveryViewModel,
    uiState: RecipeDiscoveryUiState,
    keyboardFocusManager: FocusManager
) {
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
            // push everything to bottom, particularly for nutritional filters button:
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .padding(top = 12.dp) // extra to make distance between text input box and top of box
                .fillMaxWidth()
        ) {
            // Filter Selector: only display if at least one tag has been selected
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
                    val filtersSelected = uiState.tagSheetState.recipeTags != null && uiState.tagSheetState.recipeTags.any {
                        it.selected
                    }

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
                                // Clip stops the clickable element from exceeding the rounded corner shape
                                .clip(RoundedCornerShape(12.dp))
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
                    // only display if at least one has been selected, not if all unselected
                    if (filtersSelected) {
                        SelectedTags(
                            tags = uiState.tagSheetState.recipeTags,
                            allowEdit = true,
                            updateSelectedTag = viewModel::updateSelected
                        )
                    }
                }
            }


            val nutritionalTagSheetState = rememberModalBottomSheetState(
                // fill the screen with the required amount rather than stuck at half screen size
                skipPartiallyExpanded = true
            )
            if (uiState.tagSheetState.filterDisplayState != FilterState.NONE) {
                RecipeTagSelection(
                    sheetState = nutritionalTagSheetState,
                    tagSheetState = uiState.tagSheetState,
                    tagSheetAction = viewModel::onTagSheetAction
                )
            }

            val recipeTypeSheetState = rememberModalBottomSheetState(
                // fill the screen with the required amount rather than stuck at half screen size
                skipPartiallyExpanded = true
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

            // Image selector:
            // push the image to always be on the right side (end of the row).
            // allow user to edit and delete them.
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
                        // make the search bar fill the max width if: in from_description mode
                        // (cannot upload image), or an image has been uploaded
                        // (so cannot upload another)
                        if (uiState.userChoice == LlmMode.FROM_DESCRIPTION ||
                            uiState.savedImageMipmap != Mipmap("","")) {
                            Modifier.fillMaxWidth()
                        } // only leaves space for cam if needed
                        else {
                            Modifier.fillMaxWidth(0.85f)
                        }
                            .then(Modifier.padding(4.dp))
                            .animateBounds(this),
                    // converts painterResource to imageVector for backwards compatibility.
                    leadingIcon = ImageVector.vectorResource(R.drawable.filter_icon),
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
                        modifier = Modifier.padding(end = 4.dp).testTag("selectImage"),
                        contentDescription = "Select image"
                    )
                }
            }
        }
    }

}

/**
 * Displays the image uploaded by a user.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 * @param context - The context, to display the image.
 * @param pickMedia - Used to open the Android Photo Picker.
 * @param allowEdit - Boolean reflecting whether the user is allowed to edit (change) the uploaded image.
 * @param allowDelete - Boolean reflecting whether the user is allowed to delete the uploaded image.
 */
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
                        backgroundColour = MaterialTheme.colorScheme.background,
                        contentDescription = "View image"
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
                            backgroundColour = MaterialTheme.colorScheme.background,
                            contentDescription = "Edit image"
                        )
                    }
                    if (allowDelete) {
                        NourishIconButton(
                            onClick = {
                                imageFile.delete(); // deletes higher quality image
                                // also delete lower quality image if it exists:
                                val lowQualityImageFile = File(
                                    context.filesDir,
                                    mipmapImageFilenames.lowQualityFilename
                                )
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
                            iconColour = MaterialTheme.colorScheme.error,
                            contentDescription = "Delete image"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Function to send the request to the LLM, generating the recipe based on user input.
 * This is done by calling the ViewModel's API call function.
 * @param viewModel - The ViewModel of RecipeDiscovery, holding logic required to alter the view.
 * @param uiState - The Ui State displayed in the view.
 * @param context - The context, to display the image.
 */
fun sendToLlm(
    uiState: RecipeDiscoveryUiState,
    viewModel: RecipeDiscoveryViewModel,
    context: Context
) {
    var validRequest = false
    val lowQualityFilename = uiState.savedImageMipmap.lowQualityFilename

    // Generate recipe from name of dish / description:
    if (lowQualityFilename == "" &&
        uiState.requestType == LlmRequestType.RECIPE &&
        uiState.userRequest != "") {
        // generate recipe from text
        viewModel.makeLlmApiCall(uiState.requestType) // make llm call
        validRequest = true
    }

    // Generate potential dishes from ingredients list:
    else if (lowQualityFilename == "" &&
        uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS &&
        uiState.userRequest != "") {
        viewModel.makeLlmApiCall(uiState.requestType)
        validRequest = true
    }

    // Generate potential dishes from image of ingredients, e.g. image of contents of a fridge:
    else if (lowQualityFilename != "" &&
        uiState.requestType == LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE) {
        viewModel.makeLlmApiCall(
            uiState.requestType,
            lowQualityFilename,
            context.filesDir
        )
        validRequest = true
    }

    // Generate potential dishes from an image of a dish, e.g. image of pasta:
    else if (lowQualityFilename != "" && uiState.requestType == LlmRequestType.DISHES_FROM_IMAGE) {
        viewModel.makeLlmApiCall(
            uiState.requestType,
            lowQualityFilename,
            context.filesDir
        )
        validRequest = true
    }

    // Error: user did not input anything.
    else {
        viewModel.displayBlankInputError(uiState.requestType)
    }

    if (validRequest) {
        // Reset the ui state, as data has been sent and was valid
        viewModel.setLlmChatStage(LlmChatStage.AWAITING_LLM_RESPONSE)
    }
}