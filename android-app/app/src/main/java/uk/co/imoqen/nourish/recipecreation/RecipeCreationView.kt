package uk.co.imoqen.nourish.recipecreation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.common.NourishConfirmationDialog
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishIconTextButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.SelectedTags
import uk.co.imoqen.nourish.common.TypesDisplayForMutuallyExclusiveFiltering
import uk.co.imoqen.nourish.common.ingredient.AddIngredientBottomSheet
import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetText
import uk.co.imoqen.nourish.common.ingredient.EditIngredientBottomSheet
import uk.co.imoqen.nourish.recipebook.ServingSizeUpdater
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.common.tag.RecipeTagSelection
import uk.co.imoqen.nourish.common.tag.TagSheetAction
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.shoppinglist.IngredientAmount
import uk.co.imoqen.nourish.shoppinglist.UserInteractionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreationScreen(
    viewModel: RecipeCreationViewModel,
    onBack: () -> Unit,
) {
    // Title top area
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                // bottom corners are rounded, top are not:
                shape = RoundedCornerShape(
                    bottomStart = 32.dp,
                    bottomEnd = 32.dp
                )
            )
            .padding(vertical = 16.dp)
    ) {
        NourishH2(
            "Add a Recipe",
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

    val uiState = viewModel.uiState.collectAsState().value
    val keyboardFocusManager = LocalFocusManager.current
    val nutritionalTagSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // makes it fill the screen with the required amount rather than stuck at half the screen size
    )
    LaunchedEffect("GET_RECIPE_TYPES_AND_TAGS") {
        viewModel.getAllRecipeTypes()
        viewModel.getAllTagsForUser()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(top=70.dp).verticalScroll(rememberScrollState()), // room from top for the title area
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Contents, * means it is mandatory:

        // Title of recipe *
        NourishTextField(
            value = uiState.title,
            onValueChange = { newRecipeTitle ->
                viewModel.setTitle(newRecipeTitle)
            },
            placeholder = "Enter a Title*",
            onDone = { keyboardFocusManager.clearFocus() },
            leadingIcon = ImageVector.vectorResource(R.drawable.text),
        )

        // Type of recipe *
        NourishTextDivider("Details")

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Recipe Type
            RecipeDetailSection(
                onClick = { viewModel.setDisplayRecipeTypeSelector(true) },
                roundedCornerAmount = 16
            ) {
                Row(
                    modifier = Modifier.padding(4.dp).padding(horizontal=6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Recipe Type*")
                    if (uiState.recipeType == null) {
                        NourishH3("None")
                        Icon(
                            painter = painterResource(R.drawable.edit),
                            contentDescription = "Add a Nutritional Tag",
                        )
                    } else {
                        NourishH3(text = uiState.recipeType.title)
                    }
                }
            }
            // Recipe Tags
            RecipeDetailSection(
                onClick = { viewModel.setFilterDisplayState(FilterState.APPLY) },
                roundedCornerAmount = 32
            ) {
                Column(
                    modifier = Modifier.padding(4.dp).clip(RoundedCornerShape(32.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.padding(top=4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nutritional Tags")
                        Icon(
                            modifier = Modifier.padding(horizontal=4.dp),
                            painter = painterResource(R.drawable.add),
                            contentDescription = "Add a Nutritional Tag"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (uiState.tagSheetState.recipeTags != null &&
                            uiState.tagSheetState.recipeTags.any{ it.selected }
                        ) {

                            SelectedTags(
                                tags = uiState.tagSheetState.recipeTags,
                                allowEdit = true,
                                updateSelectedTag = { tag, selected ->
                                    viewModel.onTagSheetAction(
                                        TagSheetAction.UpdateSelected(
                                            tag,
                                            selected
                                        )
                                    )
                                },
                            )

                        } else {
                            NourishH3(
                                text = "None"
                            )
                        }
                    }
                }
            }
        }

        NourishTextDivider("Ingredients")

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.8f).padding(vertical=8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServingSizeUpdater(uiState.numberOfServings, viewModel::alterServingSize)
                // then print ingredients here

                Column(
                    modifier = Modifier.background(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    if (uiState.ingredients == null || uiState.ingredients.isEmpty()) {
                        Text(
                            text = "None",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.95f).padding(8.dp)
                        )
                    } else {
                        uiState.ingredients.forEachIndexed { i, ingredient ->

                            Row(
                                modifier = Modifier.fillMaxWidth(0.95f).padding(start=6.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "- ${ingredient.ingredientPlural}",
                                    modifier = Modifier.weight(1f)
                                )

                                //Spacer(modifier = Modifier.weight(1f)) // amount etc goes to the end

                                IngredientAmount(
                                    unit = IngredientUnit.valueOf(
                                        ingredient.ingredientAmountUnit
                                    ),
                                    amount = ingredient.ingredientAmount,
                                    editModifierFunctionality = {
                                        Modifier
                                            .clickable(
                                                onClick = {
                                                    viewModel.setEditIngredientIndex(
                                                        i
                                                    )
                                                    viewModel.setEditIngredientAmount(
                                                        "${ingredient.ingredientAmount}"
                                                    )
                                                }
                                            )
                                    },
                                    editContent = {
                                        Icon(
                                            painter = painterResource(R.drawable.edit),
                                            contentDescription = "Edit Ingredient Amount",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                        )
                                    },
                                    formatIngredientAmount = viewModel::formatIngredientAmount,
                                    round = false // don't round as its user inputted
                                )
                                Icon(
                                    painter = painterResource(R.drawable.delete),
                                    contentDescription = "Delete Item",
                                    tint = MaterialTheme.colorScheme.error.copy(
                                        alpha = 0.8f
                                    ),
                                    modifier = Modifier.clickable(
                                        onClick = { viewModel.setIngredientIndexToDelete(i) }
                                    )
                                )
                            }
                        }
                    }
                }

                NourishIconTextButton(
                    description = "Add Ingredient",
                    painter = painterResource(R.drawable.ingredients),
                    onClick = { viewModel.setShowAddIngredientSheet(true) },
                    buttonColour = MaterialTheme.colorScheme.background
                )
            }
        }

        // Method *
        NourishTextDivider("Storage Guidance")
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
        ) {

        }

        // Storage guidance
        NourishTextField(
            value = uiState.title,
            onValueChange = { newStorageGuidance ->
                viewModel.setStorageGuidance(newStorageGuidance)
            },
            placeholder = "Enter Storage Guidance",
            onDone = { keyboardFocusManager.clearFocus() },
            leadingIcon = ImageVector.vectorResource(R.drawable.fridge),
        )
    }

    // Confirmation Dialogs
    NourishConfirmationDialog(
        show = uiState.showDeleteIngredientDialog,
        title = "Confirm Ingredient Removal",
        description = "Are you sure you wish to remove this ingredient?\n\n This action cannot be undone.",
        onConfirmation = { viewModel.deleteIngredient(); viewModel.showDeleteIngredientDialog(false) },
        onDismiss = { viewModel.showDeleteIngredientDialog(false) }
    )

    // model bottom sheet for editing ingredient amount
    if (
        uiState.ingredientToEdit.index != -1
    ) {
        EditIngredientBottomSheet(
            onDismiss = { viewModel.setEditIngredientIndex(-1) },
            // gets the unit from the unit name, then its singular form
            unitName = IngredientUnit.valueOf(uiState.ingredients?.getOrNull(uiState.ingredientToEdit.index)?.ingredientAmountUnit ?: "UNIT").singularName,
            amount = uiState.ingredientToEdit.amount,
            setAmount = viewModel::setEditIngredientAmount,
            persistAmountUpdate = viewModel::updateIngredientAmount,
            keyboardFocusManager = keyboardFocusManager
        )
    }

    // modal bottom sheet for adding ingredients
    if (uiState.showAddIngredientSheet) {
        AddIngredientBottomSheet(
            ingredient = uiState.ingredientToAdd,
            action = viewModel::onIngredientSheetAction,
            possibleUnits = listOf(
                // more options than in the shopping list. but not overcomplicating it, not overwhelming user.
                IngredientUnit.ML, IngredientUnit.L, IngredientUnit.G, IngredientUnit.KG,
                IngredientUnit.TSP, IngredientUnit.TBSP, IngredientUnit.PINCH, IngredientUnit.PINT
            ),
            text = AddIngredientSheetText(
                title = "Add an ingredient to your Recipe",
                name = "Enter an ingredient to add",
                amount = "Enter amount",
                unit = "Select unit",
                complete = "Add to Recipe"
            ),
            noneUnit = IngredientUnit.UNIT
        )
    }

    // modal bottom sheet for selecting recipe filter tags
    if (uiState.tagSheetState.filterDisplayState != FilterState.NONE) {
        RecipeTagSelection(
            sheetState = nutritionalTagSheetState,
            tagSheetState = uiState.tagSheetState,
            tagSheetAction = viewModel::onTagSheetAction,
            title = "Choose Nutritional Tag(s)"
        )
    }

    // modal bottom sheet for selecting recipe type:
    if (uiState.displayRecipeTypeSelector) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.setDisplayRecipeTypeSelector(false)
            },
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
                TypesDisplayForMutuallyExclusiveFiltering(
                    uiState.recipeTypes,
                    uiState.recipeType,
                    viewModel::setRecipeType,
                    allowNone = false
                )
            }
        }
    }
}

@Composable
fun RecipeDetailSection(
    onClick: () -> Unit,
    roundedCornerAmount: Int,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(roundedCornerAmount.dp))
            .clickable { onClick() }
            .background(
                shape = RoundedCornerShape(roundedCornerAmount.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            )
            .animateContentSize()
    ) {
        content.invoke()
    }
}