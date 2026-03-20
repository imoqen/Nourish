package uk.co.imoqen.nourish.recipecreation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishConfirmationDialog
import uk.co.imoqen.nourish.common.NourishDivider
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.NourishH3
import uk.co.imoqen.nourish.common.NourishH4
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
        modifier = Modifier.fillMaxSize().padding(top=70.dp).padding(bottom=12.dp).verticalScroll(rememberScrollState()), // room from top for the title area
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Contents, * means it is mandatory:

        // Details (title, type, tags)
        NourishTextDivider("Details")

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Recipe Title
            // Title of recipe *
            NourishTextField(
                value = uiState.title,
                onValueChange = { newRecipeTitle ->
                    viewModel.setTitle(newRecipeTitle)
                },
                placeholder = "Enter Title*",
                onDone = { keyboardFocusManager.clearFocus() },
                leadingIcon = ImageVector.vectorResource(R.drawable.text),
            )

            // Recipe Type
            RecipeDetailSection(
                onClick = { viewModel.setDisplayRecipeTypeSelector(true) },
                roundedCornerAmount = 16
            ) {
                Row(
                    modifier = Modifier.padding(4.dp).padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Recipe Type* ")
                    if (uiState.recipeType == null) {
                        Box(
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(8.dp)
                            )
                        ) {
                            NourishH4(text = "None")
                        }
                    } else {
                        Box(
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(8.dp)
                            )
                        ) {
                            NourishH4(text = uiState.recipeType.title)
                        }
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
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nutritional Tags")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (uiState.tagSheetState.recipeTags != null &&
                            uiState.tagSheetState.recipeTags.any { it.selected }
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
                            Box(
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            ) {
                                NourishH4("None")
                            }
                        }
                    }
                }
            }
        }

        NourishTextDivider(text = "Ingredients*", modifier = Modifier.padding(vertical=8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.95f).padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                    if (uiState.ingredients == null || uiState.ingredients.isEmpty()) {
                        Text(
                            text = "No ingredients added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.95f).padding(top=8.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.background(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.background
                            )
                        ) {
                        uiState.ingredients.forEachIndexed { i, ingredient ->

                            Row(
                                modifier = Modifier.fillMaxWidth(0.95f).padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "- ${ingredient.ingredientPlural}",
                                    modifier = Modifier.weight(1f)
                                )

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
                                    contentDescription = "Delete Ingredient",
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
                )
            }
        }

        NourishTextDivider("Number of Servings*", modifier = Modifier.padding(vertical=8.dp))
        ServingSizeUpdater(uiState.numberOfServings, viewModel::alterServingSize)

        // Method *
        NourishTextDivider("Method*", modifier = Modifier.padding(vertical=8.dp))
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().animateContentSize().padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DraggableStepsList(viewModel, uiState, keyboardFocusManager)

                // modal bottom sheet for adding method step
                AnimatedContent(
                    targetState = uiState.showAddMethodSheet,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                ) { state ->
                    if (state) {
                        Column(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            NourishTextField(
                                value = uiState.stepToAdd,
                                onValueChange = { viewModel.setStepToAdd(it) },
                                placeholder = "Enter the step",
                                onDone = { keyboardFocusManager.clearFocus() },
                                trailingIcon = ImageVector.vectorResource(R.drawable.cross),
                                alwaysDisplayTrailingIcon = true,
                                onTrailingIconPress = { viewModel.resetRecipeStepAddition() }
                            )
                            NourishButton(
                                onClick = { viewModel.addRecipeStep() },
                                text = "Save Step"
                            )
                        }
                    } else {
                        NourishIconTextButton(
                            description = "Add Step",
                            painter = painterResource(R.drawable.chef),
                            onClick = { viewModel.setShowAddMethodSheet(true) },
                        )
                    }
                }
            }

        }

        // Storage
        NourishTextDivider("Storage Guidance", modifier = Modifier.padding(vertical=8.dp))
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
            value = uiState.storageGuidance,
            onValueChange = { viewModel.setStorageGuidance(it) },
            placeholder = "Enter Storage Guidance",
            onDone = { keyboardFocusManager.clearFocus() },
            leadingIcon = ImageVector.vectorResource(R.drawable.fridge),
        )
    }


    // Confirmation Dialogs
    // ingredient deletion:
    NourishConfirmationDialog(
        show = uiState.ingredientIndexToDelete != -1,
        title = "Confirm Removal",
        description = "Are you sure you wish to remove this ingredient from the recipe?",
        onConfirmation = { viewModel.deleteIngredient() },
        onDismiss = { viewModel.setIngredientIndexToDelete(-1) }
    )

    // step deletion:
    NourishConfirmationDialog(
        show = uiState.stepIndexToDelete != -1,
        title = "Confirm Removal",
        description = "Are you sure you wish to remove this step from the recipe?",
        onConfirmation = { viewModel.deleteStep() },
        onDismiss = { viewModel.setStepIndexToDelete(-1) }
    )

    // modal bottom sheet for editing ingredient amount
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
            .fillMaxWidth(0.9f)
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

// uses calvin reorderable
@Composable
fun DraggableStepsList(
    viewModel: RecipeCreationViewModel,
    uiState: RecipeCreationUiState,
    keyboardFocusManager: FocusManager
) {
    // setup for the draggable list:
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (uiState.method != null) {
            // reorder the list accordingly, and persist in the ui state:
        viewModel.setMethod(uiState.method.toMutableList ().apply {
            add(to.index, removeAt(from.index))
        })
    }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    if (uiState.method == null || uiState.method.isEmpty()) {
        Text(
            text = "No steps added yet.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.95f).padding(8.dp)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max=400.dp), //scrollable so just limit its ehight
            state = lazyListState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(uiState.method, key = { _, step -> step.uuid }) { i, step -> // random uuid is assigned when the step is made, so that no collisions between keys. keys must be unique, or the app crashes

                ReorderableItem(reorderableLazyListState, key = step.uuid) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                    Surface(
                        shadowElevation = elevation,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(0.95f),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                        ) {

                            if (uiState.stepIndexToEdit == i) {
                                Box(modifier = Modifier.padding(bottom = 10.dp).align(Alignment.CenterVertically)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        NourishTextField(
                                            value = uiState.stepToEdit,
                                            onValueChange = { viewModel.setStepToEdit(it) },
                                            placeholder = "",
                                            onDone = { keyboardFocusManager.clearFocus() },
                                            trailingIcon = ImageVector.vectorResource(R.drawable.tick),
                                            onTrailingIconPress = { viewModel.updateStep() }
                                        )
                                        Icon(
                                            painter = painterResource(R.drawable.delete),
                                            contentDescription = "Delete Step",
                                            tint = MaterialTheme.colorScheme.error.copy(
                                                alpha = 0.8f
                                            ),
                                            modifier = Modifier.clickable(
                                                onClick = { viewModel.setStepIndexToDelete(i) }
                                            )
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "${i+1}. ${step.step}", modifier = Modifier.weight(1f).padding(start=12.dp).padding(vertical=4.dp)
                                )
                            }

                            if (uiState.stepIndexToEdit == -1) {
                                IconButton(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .draggableHandle(
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        }
                                    ),
                                    onClick = {}
                                ) {
                                    Icon(painterResource(R.drawable.drag_handle), contentDescription = "Reorder")
                                }
                                Icon(
                                    painter = painterResource(R.drawable.edit),
                                    contentDescription = "Edit Step",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clickable(
                                            onClick = {
                                                viewModel.setStepToEdit(
                                                    step.step
                                                )
                                                viewModel.setStepIndexToEdit(
                                                    i
                                                )
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMethodStepBottomSheet(
    onDismiss: () -> Unit,
    step: String,
    setStep: (String) -> Unit,
    keyboardFocusManager: FocusManager
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            NourishTextDivider("Edit the amount")*/