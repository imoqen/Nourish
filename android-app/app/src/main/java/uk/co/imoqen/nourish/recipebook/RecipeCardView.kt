package uk.co.imoqen.nourish.recipebook

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import uk.co.imoqen.nourish.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import uk.co.imoqen.nourish.common.IndeterminateCircularIndicator
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishIconButton
import uk.co.imoqen.nourish.common.NourishIconTextButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.NourishTextMessage
import uk.co.imoqen.nourish.common.TextSender
import uk.co.imoqen.nourish.common.recipeColour
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQuery
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.recipediscovery.RecipeCardPreview
import uk.co.imoqen.nourish.shoppinglist.IngredientAmount
import java.io.File

@Composable
fun RecipeCardScreen(
    recipeBookViewModel: RecipeBookViewModel,
    uiState: RecipeBookUiState,
    recipeId: Long,
    onBack: () -> Unit,
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

    AnimatedVisibility(
        visible = uiState.visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val recipe = recipeBookViewModel.uiState.collectAsState().value.selectedRecipe
        recipeBookViewModel.buildRecipeCard()
        val recipeCard = recipeBookViewModel.uiState.collectAsState().value.selectedRecipeCard
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        if (recipeCard != null) {

            val imagePath = recipeCard.imagePath
            val colour = recipeCard.colour.recipeColour()

            Card() {
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
                                        .background(colour) // put the chosen colour
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
                        // Serving Size:
                        ServingSizeUpdater(recipeCard.serves, recipeBookViewModel::alterServingSize)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical=16.dp))

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
                                .animateContentSize().fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // then output the ingredients by section
                            recipeCard.sectionsWithIngredients.forEach { (section, ingredients) ->
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // only output name of section if there is more than 1 section, otherwise it is irrelevant
                                    if (recipeCard.sectionsWithIngredients.size > 1) {
                                        Text(
                                            text = section,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    ingredients.forEach { (ingredient, amount, _) ->

                                        val round =
                                            !(ingredient.startsWith("kg") || ingredient.startsWith(
                                                "L"
                                            )) // if kg or L, do not round.
                                        Text(
                                            text = "${
                                                recipeBookViewModel.formatIngredientAmount(
                                                    amount,
                                                    round
                                                )
                                            } $ingredient",
                                        )
                                    }
                                }
                            }
                            NourishIconTextButton(
                                onClick = {
                                    recipeBookViewModel.formatIngredientsForShoppingList() // this sets it not to null, so the dialog will be triggered to display (if successful).
                                },
                                painter = painterResource(R.drawable.shopping_cart_add),
                                description = "Add to Shopping List",
                            )

                            // show shopping list dialog:
                            if (uiState.ingredientsToAddToShoppingList != null) {
                                Dialog(
                                    onDismissRequest = {
                                        recipeBookViewModel.setIngredientsToAddToShoppingList(null)
                                    }
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxHeight(0.7f)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                NourishH3(
                                                    "Add to Shopping List",
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                HorizontalDivider()

                                                val ingredientsToAddToShoppingList =
                                                    uiState.ingredientsToAddToShoppingList

                                                LazyColumn(
                                                    modifier = Modifier.weight(1f) // takes up as much space as possible; all other things have space then this uses up all the rest of the space.
                                                ) {
                                                    itemsIndexed(ingredientsToAddToShoppingList) { i, (ingredient, selected) ->

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth()
                                                                .padding(horizontal = 8.dp),
                                                            horizontalArrangement = Arrangement.Center,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                ingredient.plural
                                                            )

                                                            var amount = ingredient.amount
                                                            var unit = ingredient.unit

                                                            // convert the amounts in the UI if g and kg and >= 1000
                                                            // only in the UI - not in the db so everything is still stored as G / ML so easy to compare and bundle together.
                                                            if (amount >= 1000 && (unit == IngredientUnit.G || unit == IngredientUnit.ML)) {
                                                                when (unit) {
                                                                    IngredientUnit.G -> {
                                                                        unit = IngredientUnit.KG
                                                                    }

                                                                    IngredientUnit.ML -> {
                                                                        unit = IngredientUnit.L
                                                                    }

                                                                    else -> {} // make no change
                                                                }
                                                                amount = amount / 1000
                                                            }

                                                            Spacer(modifier = Modifier.weight(1f))

                                                            IngredientAmount(
                                                                unit = unit,
                                                                amount = amount,
                                                                formatIngredientAmount = recipeBookViewModel::formatIngredientAmount
                                                            )
                                                            Checkbox(
                                                                modifier = Modifier.padding(0.dp),
                                                                checked = selected,
                                                                onCheckedChange = {
                                                                    recipeBookViewModel.alterIngredientShoppingListStatus(
                                                                        ingredient, i, !selected
                                                                    );
                                                                    Log.i(
                                                                        "alter",
                                                                        "${uiState.ingredientsToAddToShoppingList}"
                                                                    )
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                NourishIconTextButton(
                                                    onClick = {
                                                        recipeBookViewModel.addIngredientsToShoppingList() // this sets it not to null, so the dialog will be triggered to display (if successful).
                                                    },
                                                    painter = painterResource(R.drawable.shopping_cart_add),
                                                    description = "Add to Shopping List",
                                                )
                                            }
                                        }

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
                                        onCheckedChange = {
                                            recipeBookViewModel.markStepAsComplete(
                                                id,
                                                !complete
                                            )
                                        }
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

                                if (i != recipeCard.method.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .padding(top = 12.dp)
                                    )
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

                    HorizontalDivider(modifier = Modifier.padding(vertical=16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.background)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RecipeActionButton(
                                onClick = {
                                    recipeBookViewModel.setLlmActionType(LlmActionType.NUTRITIONAL_VALUES)
                                    recipeBookViewModel.setActionChatStage(ActionChatStage.USER_INPUT)
                                },
                                painter = painterResource(R.drawable.forkchart),
                                description = "Nutrition",
                                modifier = Modifier.weight(1f)
                            )
                            RecipeActionButton(
                                onClick = {
                                    recipeBookViewModel.setUserActionRequest("")
                                    recipeBookViewModel.setLlmActionType(LlmActionType.ALTERATION)
                                    recipeBookViewModel.setActionChatStage(ActionChatStage.USER_INPUT)
                                },
                                painter = painterResource(R.drawable.adjust),
                                description = "Make Alteration",
                                modifier = Modifier.weight(1f)
                            )

                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RecipeActionButton(
                                onClick = {
                                    recipeBookViewModel.setUserActionRequest("")
                                    recipeBookViewModel.setLlmActionType(LlmActionType.QUESTION)
                                    recipeBookViewModel.setActionChatStage(ActionChatStage.USER_INPUT)
                                },
                                painter = painterResource(R.drawable.live_question),
                                description = "Ask Question",
                                modifier = Modifier.weight(1f)
                            )

                            RecipeActionButton(
                                onClick = {
                                    // edit
                                },
                                painter = painterResource(R.drawable.edit),
                                description = "Edit Manually",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        /*
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
                        )*/
                    }

                }
            }

            if (uiState.llmActionType == LlmActionType.NUTRITIONAL_VALUES && uiState.selectedRecipe != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        recipeBookViewModel.setLlmActionType(LlmActionType.NONE)
                    },
                    sheetState = sheetState
                ) {
                    if (uiState.llmActionType == LlmActionType.NUTRITIONAL_VALUES) {
                        NutritionLlmAction(
                            uiState.selectedRecipe,
                            uiState,
                            recipeBookViewModel,
                        )
                    }

                }
            } else if ((uiState.llmActionType == LlmActionType.ALTERATION || uiState.llmActionType == LlmActionType.QUESTION)
                && uiState.selectedRecipe != null) {
                // ALTERATION ACTION
                Dialog(
                    onDismissRequest = {
                        recipeBookViewModel.setLlmActionType(LlmActionType.NONE)
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (uiState.llmActionType == LlmActionType.ALTERATION) {
                            AlterationLlmAction(
                                uiState,
                                recipeBookViewModel,
                                uiState.selectedRecipe,
                                keyboardFocusManager
                            )
                        } else if (uiState.llmActionType == LlmActionType.QUESTION) {
                            QueryLlmAction(
                                uiState,
                                recipeBookViewModel,
                                uiState.selectedRecipe,
                                keyboardFocusManager
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeActionButton(
    painter: Painter,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // for assigning equal weight to the buttons so they fill the screen equally
) {
    Box(
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(),
                    contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha=0.90f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    //tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(24.dp)
                )

                if (description != "") {
                    Text(
                        text = description,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QueryLlmAction(
    uiState: RecipeBookUiState,
    viewModel: RecipeBookViewModel,
    recipe: Recipe,
    keyboardFocusManager: FocusManager
) {
    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().fillMaxHeight(0.8f)
            .padding(bottom = 12.dp).padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        NourishH2("Ask a Question", modifier = Modifier.padding(top = 16.dp))

        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))

        val queries =
            viewModel.getAllQueriesForRecipe(recipe.id)
                .collectAsState(initial = null).value

        if (queries == null || queries.isEmpty()) {

            Text(
                "Ask Gemini a question about the recipe.",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )

        } else {

            val lazyListState = rememberLazyListState()

            LaunchedEffect(queries.size) {
                lazyListState.animateScrollToItem(0) // scrolls to the llm message once it is received
            }

            LookaheadScope() {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.85f).animateBounds(this),
                    state = lazyListState,
                    reverseLayout = true // so that start at bottom of the chat
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDivider(modifier = Modifier.padding(bottom=8.dp))
                            Text(
                                "Please note that all answers are produced by Gemini; as such, there may be mistakes.",
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
                    item {
                        // the loading circle:
                        IndeterminateCircularIndicator(uiState.llmAwaitActionType != LlmActionType.NONE) // only show if the await type is not none, as this indicates loading if not none.
                    }

                    items(queries.reversed()) { query -> // queries have to be reversed due to the reverse layout. but still required so that start at bottom of chat.
                        if (query != null) {
                            QueryDisplay(query)
                        }
                    }
                }
            }

        }

        Spacer(modifier= Modifier.weight(1f))

        AnimatedVisibility(
            visible = uiState.llmAwaitActionType == LlmActionType.NONE // only visible if not currently awaiting an LLM response
        ) {
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
                    viewModel.makeLlmApiCall(LlmActionType.QUESTION)
                },
                alwaysDisplayTrailingIcon = false
            )
        }


    }
}

@Composable
fun AlterationLlmAction(
    uiState: RecipeBookUiState,
    viewModel: RecipeBookViewModel,
    recipe: Recipe,
    keyboardFocusManager: FocusManager
) {
    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().fillMaxHeight(0.8f)
            .padding(bottom = 12.dp).padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        LookaheadScope() { // for animation
            Column(
                modifier = Modifier.animateBounds(this),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                NourishH2("Recipe Alteration", modifier = Modifier.padding(top = 16.dp))

                HorizontalDivider(modifier = Modifier.padding(top = 10.dp))

                if (uiState.userActionRequest != "" && uiState.actionChatStage != ActionChatStage.USER_INPUT) {
                    NourishTextMessage(
                        textSender = TextSender.USER,
                        modifier = Modifier.align(Alignment.End),
                        textMessage = {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Text("You", color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.8f))
                                Text(uiState.userActionRequest, modifier = Modifier.padding(8.dp))
                            }
                        }
                    )
                }

                // the loading circle:
                IndeterminateCircularIndicator(uiState.llmAwaitActionType != LlmActionType.NONE)

                if (uiState.alteredRecipe == null) {
                    if (uiState.llmAwaitActionType == LlmActionType.ALTERATION) {
                        Text(
                            "Altering the recipe...",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            "Not happy with the recipe, or wish to change it in some way?",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Simply enter your alteration below, and a new recipe will be generated. You can then choose whether to replace the previous recipe with it, save it as a new recipe, or discard it.",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
                } else {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RecipeCardPreview(
                                    uiState.alteredRecipe,
                                    viewModel::formatIngredientText,
                                    imageFilename = recipe.imagePath,
                                    context = LocalContext.current,
                                    alteredRecipe = true
                                )

                            }
                        }
                        item {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                NourishTextDivider("Select an option")

                                NourishButton(
                                    onClick = {
                                        viewModel.setVisible(false)
                                        viewModel.replaceRecipe(recipe, uiState.alteredRecipe)
                                        viewModel.setVisible(true)
                                        viewModel.setAlteredRecipe(null)
                                        viewModel.setUserActionRequest("")
                                        viewModel.setLlmActionType(LlmActionType.NONE)
                                    },
                                    text = "Replace recipe",
                                )
                                NourishButton(
                                    onClick = {
                                        viewModel.addRecipe(uiState.alteredRecipe, alteration = true) // alteration flag ensures that ingredients get scaled correctly when parsing to the db
                                        viewModel.setAlteredRecipe(null)
                                        viewModel.setLlmActionType(LlmActionType.NONE)
                                    },
                                    text = "Save as new recipe",
                                )
                                NourishButton(
                                    onClick = {
                                        viewModel.setAlteredRecipe(null)
                                        viewModel.setUserActionRequest("")

                                    },
                                    text = "Discard recipe",
                                )
                            }
                        }
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                HorizontalDivider(modifier = Modifier.padding(bottom=8.dp))
                                Text(
                                    "Please note that all answers are produced by Gemini; as such, there may be mistakes.",
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
                    NourishTextDivider("Try another alteration")
                }

                Spacer(modifier= Modifier.weight(1f))

                AnimatedVisibility(
                    visible = uiState.llmAwaitActionType == LlmActionType.NONE
                ) {
                    NourishTextField(
                        value = uiState.userActionRequest,
                        onValueChange = { actionRequest ->
                            viewModel.setUserActionRequest(actionRequest)
                        },
                        placeholder = "Enter an alteration",
                        onDone = { keyboardFocusManager.clearFocus() },
                        leadingIcon = ImageVector.vectorResource(R.drawable.edit_note),
                        trailingIcon = Icons.Filled.Send,
                        onTrailingIconPress = {
                            viewModel.setAlteredRecipe(null)
                            viewModel.makeLlmApiCall(LlmActionType.ALTERATION)
                            //viewModel.setUserActionRequest("")
                        },
                        alwaysDisplayTrailingIcon = false
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionLlmAction(
    recipe: Recipe,
    uiState: RecipeBookUiState,
    viewModel: RecipeBookViewModel,
) {
    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            .padding(bottom = 12.dp).padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // the loading circle:
        IndeterminateCircularIndicator(uiState.llmAwaitActionType != LlmActionType.NONE)

        val nutrition =
            viewModel.getNutritionForRecipe(recipe.id)
                .collectAsState(initial = null).value
        if (uiState.llmAwaitActionType == LlmActionType.NUTRITIONAL_VALUES) {
            Text(
                "Estimating nutritional values...",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        if (nutrition == null && uiState.llmAwaitActionType == LlmActionType.NONE) {
            viewModel.setUserActionRequest("Generate nutritional values for the provided dish in the provided JSON schema, for the given number of servings.")
            NourishButton(
                onClick = { viewModel.makeLlmApiCall(LlmActionType.NUTRITIONAL_VALUES) },
                text = "Generate Nutritional Values",
            )
        } else if (nutrition != null) {
            LookaheadScope() {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateBounds(this),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    NourishH2("Nutritional Values")

                    NourishTextDivider(
                        text = "in ${nutrition.servings} serving${
                            if (nutrition.servings > 1) {
                                "s"
                            } else {
                                ""
                            }
                        }",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    NourishH2(
                        text = "${nutrition.calories} kcal",
                        modifier = Modifier.padding(bottom = 10.dp)
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
                            NutritionalFact(
                                "Carbohydrate",
                                "${nutrition.carbohydrate}g"
                            )
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
    }
}

@Composable
fun QueryDisplay(
    query: RecipeQuery
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        NourishTextMessage(
            textSender = TextSender.USER,
            modifier = Modifier.align(Alignment.End),
            textMessage = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("You", color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.8f))
                    Text(query.userQuestion, modifier = Modifier.padding(8.dp))
                }
            }
        )
        if (query.llmAnswer != "") {
            NourishTextMessage(
                textSender = TextSender.LLM,
                modifier = Modifier.align(Alignment.Start),
                textMessage = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("Gemini", color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.8f))
                        Text(query.llmAnswer, modifier = Modifier.padding(8.dp))
                    }
                }
            )
        }
    }
}

@Composable
fun ServingSizeUpdater(
    servings: Int,
    alterServingSize: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal=8.dp).padding(top=8.dp) // for inner padding of the total item
                .animateContentSize()
        ) {
            NourishIconButton(
                onClick = { alterServingSize(false) },
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
                    text = "$servings",
                    fontWeight = FontWeight.Bold
                )
            }
            NourishIconButton(
                onClick = { alterServingSize(true) },
                painter = painterResource(R.drawable.add),
                modifier = Modifier.size(38.dp)
            )
        }
        Text(
            text = if (servings > 1) "Servings" else "Serving",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}