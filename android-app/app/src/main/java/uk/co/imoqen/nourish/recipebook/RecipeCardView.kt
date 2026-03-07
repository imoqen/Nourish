package uk.co.imoqen.nourish.recipebook

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import uk.co.imoqen.nourish.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.flow.emptyFlow
import uk.co.imoqen.nourish.common.IndeterminateCircularIndicator
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishDivider
import uk.co.imoqen.nourish.common.NourishH1
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishH4
import uk.co.imoqen.nourish.common.NourishIconButton
import uk.co.imoqen.nourish.common.NourishIconTextButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.NourishTextMessage
import uk.co.imoqen.nourish.common.onBackgroundColour
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQuery
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipediscovery.LlmChatStage
import java.io.File

@Composable
fun RecipeCardScreen(
    recipeBookViewModel: RecipeBookViewModel,
    uiState: RecipeBookUiState,
    recipeId: Long,
    onBack: () -> Unit
) {

    val scrollState = rememberScrollState()
    val keyboardFocusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        val recipe = recipeBookViewModel.getRecipeById(recipeId).collectAsState(initial = null).value
        // should constrain it to only return recipe made by that user, for security. address in a future stepping stone ^
        if (recipe != null) {
            recipeBookViewModel.setSelectedRecipe(recipe)
            RecipeCard(recipeBookViewModel, onBack, uiState, keyboardFocusManager)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCard(
    recipeBookViewModel: RecipeBookViewModel,
    onBack: () -> Unit,
    uiState: RecipeBookUiState,
    keyboardFocusManager: FocusManager
) {
    val recipe = recipeBookViewModel.uiState.collectAsState().value.selectedRecipe
    recipeBookViewModel.buildRecipeCard()
    val recipeCard = recipeBookViewModel.uiState.collectAsState().value.selectedRecipeCard
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    if (recipeCard != null) {
        val imagePath = recipeCard.imagePath
        Card(

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            if (imagePath != "") {
                                320.dp + 32.dp
                            } else {
                                160.dp + 32.dp
                            }
                        )
                ) {

                    // image:
                    val imagePath = recipeCard.imagePath
                    var displayImage = false
                    val context = LocalContext.current

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    ) {
                        // Image:
                        val imageFile =
                            File(
                                context.filesDir,
                                imagePath
                            )
                        if (imageFile.exists() && imagePath != "") {
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
                                    .height(320.dp - 92.dp + 64.dp + 32.dp) // total size of card - size of text bit (200 - 90) + extra for the rounded corner shape (32.dp)
                                    .fillMaxWidth(), // 2/3 of the card height - maybe use consts for this in future to ensure they match upon changes
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth() // matches the card width
                                    .height(160.dp - 92.dp + 64.dp + 32.dp) // takes up much less space because no image
                                    .background(MaterialTheme.colorScheme.error) // put the chosen colour
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                // bottom corners are rounded, top are not:
                                shape = RoundedCornerShape(
                                    bottomStart = 32.dp,
                                    bottomEnd = 32.dp
                                )
                            )
                            .padding(vertical = 16.dp)
                        //contentAlignment = Alignment.Center
                    ) {
                        NourishH2(
                            recipeCard.title,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(0.8f)
                        )

                        // back button:
                        IconButton(
                            onClick = { onBack() },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowLeft,
                                contentDescription = null
                            )
                        }
                    }

                    // simply the bottom bit which overlays the image to add the rounded corner style
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                // bottom corners are rounded, top are not:
                                shape = RoundedCornerShape(
                                    topStart = 32.dp,
                                    topEnd = 32.dp
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        // Emoji:
                        Text(
                            text = recipeCard.emoji,
                            fontSize = 48.sp,
                            // center emoji if no image, otherwise put at bottom center
                            modifier = if (displayImage) {
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = (-2).dp) // offsets are added to account for the img displayed for the rounded corner shape
                            } else {
                                Modifier
                                    .align(Alignment.Center)
                                    .offset(y = (28).dp) // offsets are added to account for the bg displayed for the rounded corner shape
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

                Row(
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .weight(1f) // tags takes up all remaining space, serving size takes up as much as it needs.
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth() // for inner padding of the total item
                        ) {
                            val tags = recipeCard.recipeTags
                            if (tags.isEmpty()) {
                                Text(
                                    text = "No tags",
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                        0.5f
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                FlowRow(
                                    modifier = Modifier.padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    TagsDisplay(tags)
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            //.fillMaxWidth(0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp) // for inner padding of the total item
                                .animateContentSize()
                        ) {
                            NourishIconButton(
                                onClick = { recipeBookViewModel.alterServingSize(false) },
                                painter = painterResource(R.drawable.subtract),
                                modifier = Modifier.size(38.dp)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 3.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.person),
                                    contentDescription = "Number of Servings",
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                                )
                                Text(
                                    text = "${recipeCard.serves}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            NourishIconButton(
                                onClick = { recipeBookViewModel.alterServingSize(true) },
                                painter = painterResource(R.drawable.add),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .fillMaxWidth(0.85f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NourishH2("Ingredients")
                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .animateContentSize()
                    ) {
                        // then output the ingredients by section
                        recipeCard.sectionsWithIngredients.forEach { (section, ingredients) ->
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                // only output name of section if there is more than 1 section, otherwise it is irrelevant
                                if (recipeCard.sectionsWithIngredients.size > 1) {
                                    Text(
                                        text = section,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // id can be used if user wishes to add to shopping list
                                ingredients.forEach { (ingredient, amount, id) ->
                                    Text("${recipeBookViewModel.formatIngredientAmount(amount)} $ingredient")
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .fillMaxWidth(0.95f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    NourishH2("Method")
                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .padding(bottom = 8.dp) // for inner padding of the total item
                    ) {
                        recipeCard.method.forEachIndexed { i, (instruction, complete, id) ->
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    modifier = Modifier.padding(0.dp),
                                    checked = complete,
                                    onCheckedChange = { recipeBookViewModel.markStepAsComplete(id, !complete) }
                                )
                                Text(
                                    fontSize = 18.sp,
                                    text = "Step ${i + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    //modifier = Modifier.padding(top=10.dp)
                                )
                            }
                            Text(
                                text = instruction,
                                textDecoration = if (complete) {
                                    TextDecoration.LineThrough
                                } else TextDecoration.None,
                                textAlign = TextAlign.Center
                            )

                            if (i != recipeCard.method.size-1) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }
            }


            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NourishH2("Storage Guidance")

                HorizontalDivider()

                Text(
                    text = recipeCard.storageInstructions,
                    modifier = Modifier.padding(8.dp)
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                NourishIconTextButton(
                    onClick = {
                        recipeBookViewModel.setLlmActionType(LlmActionType.NUTRITIONAL_VALUES)
                        recipeBookViewModel.setActionChatStage(ActionChatStage.USER_INPUT)
                              },
                    painter = painterResource(R.drawable.forkchart),
                    description = "Nutritional Values",
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                NourishIconTextButton(
                    onClick = {
                        recipeBookViewModel.setUserActionRequest("")
                        recipeBookViewModel.setLlmActionType(LlmActionType.ALTERATION)
                        recipeBookViewModel.setActionChatStage(ActionChatStage.USER_INPUT)
                              },
                    painter = painterResource(R.drawable.adjust),
                    description = "Make an alteration",
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                NourishIconTextButton(
                    onClick = {
                        recipeBookViewModel.setUserActionRequest("")
                        recipeBookViewModel.setLlmActionType(LlmActionType.QUESTION)
                        recipeBookViewModel.setActionChatStage(ActionChatStage.USER_INPUT)
                              },
                    painter = painterResource(R.drawable.live_question),
                    description = "Ask a question",
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }

        }

        if (uiState.llmActionType != LlmActionType.NONE && uiState.selectedRecipe != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    recipeBookViewModel.setLlmActionType(LlmActionType.NONE)
                },
                sheetState = sheetState,
            ) {
                LlmActions(uiState.selectedRecipe, uiState, recipeBookViewModel, keyboardFocusManager)
                // the loading circle:
                IndeterminateCircularIndicator(uiState.actionChatStage == ActionChatStage.AWAITING_LLM_RESPONSE)

            }
        }
    }
}

@Composable
fun NutritionalFact(
    name: String,
    amount: String
) {
    Box(
        modifier = Modifier
            .requiredSize(92.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(text = amount, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun LlmActions(
    recipe: Recipe,
    uiState: RecipeBookUiState,
    viewModel: RecipeBookViewModel,
    keyboardFocusManager: FocusManager
) {
    val llmActionType = uiState.llmActionType
    when (llmActionType) {
        LlmActionType.NUTRITIONAL_VALUES -> {

            Column(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom=12.dp).padding(horizontal=10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                val nutrition =
                    viewModel.getNutritionForRecipe(recipe.id).collectAsState(initial = null).value
                if (nutrition == null) {
                    viewModel.setUserActionRequest("Generate nutritional values for the provided dish in the provided JSON schema, for the given number of servings.")
                    NourishButton(
                        onClick = { viewModel.makeLlmApiCall(llmActionType) },
                        text = "Generate Nutritional Values",
                    )
                } else {
                    NourishH2("Nutritional Values")

                    NourishTextDivider(
                        text = "In ${nutrition.servings} serving${if (nutrition.servings > 1) { "s" } else { "" }}",
                        modifier = Modifier.padding(bottom=8.dp)
                    )

                    NourishH2(
                        text = "${nutrition.calories} kcal",
                        modifier = Modifier.padding(bottom=10.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 92.dp), // size of the circles inside. should add padding inside too though
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 10.dp,
                            alignment = Alignment.CenterHorizontally
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            NutritionalFact("Fat", "${nutrition.fat}g")
                        }
                        item {
                            NutritionalFact("Saturates", "${nutrition.saturates}g")
                        }
                        item {
                            NutritionalFact("Carbohydrate", "${nutrition.carbohydrate}g")
                        }
                        item {
                            NutritionalFact("Sugars", "${nutrition.sugars}g")
                        }
                        item {
                            NutritionalFact("Protein", "${nutrition.protein}g")
                        }
                        item {
                            NutritionalFact("Salt", "${nutrition.salt}g")
                        }
                    }

                    HorizontalDivider(modifier=Modifier.padding(vertical=16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal=16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Please note that these nutritional values are estimates, produced by Gemini.",
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

        }
        LlmActionType.ALTERATION -> {
            NourishTextField(
                value = uiState.userActionRequest,
                onValueChange =  { actionRequest ->
                    viewModel.setUserActionRequest(actionRequest)
                },
                placeholder = "Enter an alteration",
                onDone = { keyboardFocusManager.clearFocus() },
                leadingIcon = ImageVector.vectorResource(R.drawable.edit_note),
                trailingIcon = Icons.Filled.Send,
                onTrailingIconPress = {
                    viewModel.makeLlmApiCall(llmActionType)
                },
                alwaysDisplayTrailingIcon = false
            )

        }
        LlmActionType.QUESTION -> {

            Column(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom=12.dp).padding(horizontal=10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NourishH2("Ask a Question")

                val queries =
                    viewModel.getAllQueriesForRecipe(recipe.id).collectAsState(initial = null).value
                
                if (queries == null || queries.isEmpty()) {
                    
                    Text("Ask the LLM a question about the recipe.")
                    
                } else {
                    queries.forEach { query ->
                        if (query != null) {
                            QueryDisplay(query)
                        }
                    }
                }

                NourishTextField(
                    value = uiState.userActionRequest,
                    onValueChange = { actionRequest ->
                        viewModel.setUserActionRequest(actionRequest)
                    },
                    placeholder = "Enter a question",
                    onDone = { keyboardFocusManager.clearFocus() },
                    leadingIcon = ImageVector.vectorResource(R.drawable.live_question),
                    trailingIcon = Icons.Filled.Send,
                    onTrailingIconPress = {
                        viewModel.makeLlmApiCall(llmActionType)
                    },
                    alwaysDisplayTrailingIcon = false
                )

            }

        }
        LlmActionType.NONE -> {
            Text("An error occurred. Please try again later.")
        }
    }
}

@Composable
fun QueryDisplay(
    query: RecipeQuery
) {
    NourishTextMessage(
        textSender = TODO(),
        modifier = TODO(),
        textMessage = TODO()
    )
}