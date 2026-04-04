package uk.co.imoqen.nourish.shoppinglist

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.common.NourishConfirmationDialog
import uk.co.imoqen.nourish.common.NourishDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.common.ingredient.AddIngredientBottomSheet
import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetText
import uk.co.imoqen.nourish.common.ingredient.EditIngredientBottomSheet
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.recipebook.RecipeLoadingBar
import kotlin.math.ceil

/**
 * Shopping list view
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current // to provide haptic feedback when reordering list items
    val keyboardFocusManager = LocalFocusManager.current

    LaunchedEffect("POPULATE_SHOPPING_LIST") {
        viewModel.populateShoppingList()
    }

    // whenever the entries change (e.g. from checking off items as purchased), need to resort them in the UI.
    LaunchedEffect(uiState.shoppingListEntries) {
        viewModel.setShoppingListEntries(uiState.shoppingListEntries.sortedWith(
            compareBy<ShoppingListUiEntry> { it.purchased }
                .thenBy {
            it.ordinal
        }))
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->

        // Update the UI:
        val list = uiState.shoppingListEntries.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        list.sortBy { it.purchased } // sort to ensure purchased ones are always at bottom of the list
        viewModel.setShoppingListEntries(list)

        // Persist the change locally by updating the DB:
        val fromKey = from.key as? Long
        val toKey = to.key as? Long
        viewModel.reorderShoppingList(fromKey, toKey)

        // Provide haptic feedback:
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    // for deleting all (reset)
    NourishConfirmationDialog(
        show = uiState.showDeleteAllDialog,
        title = "Confirm Clear",
        description = "Are you sure you wish to clear the shopping list?\n\nAll items in the shopping list will be deleted.",
        onConfirmation = { viewModel.clearShoppingList(); viewModel.setShowDeleteAllDialog(false); viewModel.setUserInteractionStatus(UserInteractionStatus.BROWSE) },
        onDismiss = { viewModel.setShowDeleteAllDialog(false) }
    )
    // to delete only the purchased ones
    NourishConfirmationDialog(
        show = uiState.showDeletePurchasedDialog,
        title = "Confirm Clear",
        description = "Are you sure you wish to clear all purchased items from the shopping list?",
        onConfirmation = { viewModel.clearShoppingList(onlyDeletePurchased = true); viewModel.setShowDeletePurchasedDialog(false); viewModel.setUserInteractionStatus(UserInteractionStatus.BROWSE) },
        onDismiss = { viewModel.setShowDeletePurchasedDialog(false) }
    )
    // for deleting single item
    NourishConfirmationDialog(
        show = (uiState.itemIndexToDelete != -1),
        title = "Confirm Delete",
        description = "Are you sure you wish to delete this item from the shopping list?",
        onConfirmation = { viewModel.deleteShoppingListItem(); viewModel.setItemIndexToDelete(-1) },
        onDismiss = { viewModel.setItemIndexToDelete(-1) }
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Search Bar to search for ingredients
        Box (
            contentAlignment = Alignment.Center
        ) {
            NourishTextField(
                value = uiState.searchTerm,
                onValueChange = { newSearchTerm ->
                    viewModel.setSearchTerm(newSearchTerm)
                },
                placeholder = "Search for an item",
                leadingIcon = Icons.Filled.Search,
                onDone = { keyboardFocusManager.clearFocus() },
                trailingIcon = Icons.Filled.Clear,
                onTrailingIconPress = { viewModel.setSearchTerm("") },
                modifier = Modifier.padding(bottom=8.dp)
            )
        }

        // Placed in box so that the FABs can be placed on top of the shopping list
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.shoppingListEntries.isEmpty()) {
                RecipeLoadingBar("Loading your shopping list...") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "There are no items in your shopping list.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .padding(4.dp),
                            textAlign = TextAlign.Center,
                        )
                        NourishDivider()
                        Text(
                            "Items can be added from the ingredients of a recipe, or manually by pressing 'Add Item' below.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

                // Shopping List
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 72.dp) // padding at bottom to accommodate for the space that the FAB takes up, so that all elements of the page are 100% visible by scrolling
                ) {

                // Lists the items, using the primary key as its key: stable.
                itemsIndexed(
                    uiState.shoppingListEntries,
                    key = { i, shoppingListItem -> shoppingListItem.id }) { i, shoppingListItem ->

                    // checks whether another item under the same name exists but a small amount
                    // e.g. in recipes when only a small amount is required (30ml or so). don't add this as irs own item, but
                    // add it as a disclaimer under an existing item.
                    // removed it - think i was obvercomplicating it

                    if (shoppingListItem.plural.lowercase().contains(uiState.searchTerm.lowercase())) { // applies the search
                        // Uses ReorderableItem library: https://github.com/Calvin-LL/Reorderable
                        ReorderableItem(
                            reorderableLazyListState,
                            key = shoppingListItem.id,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
                                .padding(horizontal = 6.dp)
                        ) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                            Surface(
                                shadowElevation = elevation,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row( // min height and can expand if needed, if the ingredient is very long
                                    modifier = Modifier
                                        .heightIn(48.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = shoppingListItem.purchased,
                                        onCheckedChange = {
                                            val index =
                                                uiState.shoppingListEntries.indexOfFirst { it.id == shoppingListItem.id }
                                            if (index != -1) {
                                                // update the UI and DB with the purchase state
                                                viewModel.updatePurchasedState(
                                                    index,
                                                    shoppingListItem.copy(purchased = !shoppingListItem.purchased)
                                                )
                                            }
                                        },
                                        modifier = Modifier.testTag("checkOffIngredient")
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth() // will take all remaining space and wrap
                                    ) {
                                        Text(
                                            text = shoppingListItem.plural,
                                            textDecoration = if (shoppingListItem.purchased) {
                                                TextDecoration.LineThrough
                                            } else TextDecoration.None,
                                        )
                                    }

                                    // if the unit type is SINGLE, dont display the amount. only display if UNIT or others which have a unit and amount.

                                    IngredientAmount(
                                        unit = shoppingListItem.unit,
                                        amount = shoppingListItem.amount,
                                        editModifierFunctionality = {
                                            if (uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                                                Modifier
                                                    .clickable(
                                                        onClick = {
                                                            viewModel.setIngredientIndexToEdit(
                                                                i
                                                            )
                                                            viewModel.setEditIngredientAmount(
                                                                "${shoppingListItem.amount}"
                                                            ) // so the user can updte the existing
                                                        }
                                                    )
                                            } else {
                                                Modifier
                                            }
                                        },
                                        editContent = {
                                            if (uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                                                Icon(
                                                    painter = painterResource(R.drawable.edit),
                                                    contentDescription = "Edit Item Amount",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier
                                                        .padding(end=6.dp)
                                                )
                                            }
                                        },
                                        formatIngredientAmount = viewModel::formatIngredientAmount
                                    )

                                    //Spacer(modifier = Modifier.weight(1f))
                                    // buttons, amounts, etc, go to the very end
                                    AnimatedVisibility(
                                        visible = uiState.userInteractionStatus == UserInteractionStatus.EDIT
                                    ) {
                                        Row() {
                                            // delete the item
                                            Icon(
                                                painter = painterResource(R.drawable.delete),
                                                contentDescription = "Delete Item",
                                                tint = MaterialTheme.colorScheme.error.copy(
                                                    alpha = 0.8f
                                                ),
                                                modifier = Modifier.clickable(
                                                    onClick = { viewModel.setItemIndexToDelete(i) }
                                                )
                                            )

                                            // move the item
                                            Icon(
                                                modifier = Modifier
                                                    .draggableHandle(
                                                        onDragStarted = {
                                                            hapticFeedback.performHapticFeedback(
                                                                HapticFeedbackType.GestureThresholdActivate
                                                            )
                                                        },
                                                        onDragStopped = {
                                                            hapticFeedback.performHapticFeedback(
                                                                HapticFeedbackType.GestureEnd
                                                            )
                                                        }
                                                    )
                                                    .padding(
                                                        end = 6.dp
                                                    ),
                                                painter = painterResource(R.drawable.drag_handle),
                                                contentDescription = "Reorder Item",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                    alpha = 0.8f
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


            // FAB for adding and editing
            Row(
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                AnimatedVisibility(
                    visible = uiState.userInteractionStatus == UserInteractionStatus.BROWSE || uiState.userInteractionStatus == UserInteractionStatus.EDIT
                ) {
                    Row() {
                        // Delete All button:
                        if (uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                            ExtendedFloatingActionButton(
                                modifier = Modifier.padding(vertical=12.dp,horizontal=6.dp).testTag("deleteAll"),
                                text = {
                                    Text(
                                        text = "All",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    viewModel.setShowDeleteAllDialog(true)
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.delete),
                                        contentDescription = "Clear All",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            ExtendedFloatingActionButton(
                                modifier = Modifier.padding(vertical=12.dp,horizontal=6.dp).testTag("deletePurchased"),
                                text = {
                                    Text(
                                        text = "Purchased",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    viewModel.setShowDeletePurchasedDialog(true)
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.delete),
                                        contentDescription = "Clear Purchased",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                        // Edit button:
                        if (uiState.userInteractionStatus == UserInteractionStatus.BROWSE || uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                            if (uiState.userInteractionStatus == UserInteractionStatus.BROWSE) {
                                ExtendedFloatingActionButton(
                                    modifier = Modifier.padding(vertical=12.dp,horizontal=6.dp).testTag("editList"),
                                    text = {
                                        Text(
                                            text = "Edit List",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    },
                                    onClick = {
                                        viewModel.setUserInteractionStatus(UserInteractionStatus.EDIT)
                                    },
                                    icon = {
                                        Icon(
                                            painter = painterResource(R.drawable.edit_note),
                                            contentDescription = "Edit Items",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                            if (uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                                FloatingActionButton(
                                    modifier = Modifier.padding(vertical=12.dp,horizontal=6.dp).testTag("finishEditing"),
                                    onClick = {
                                            viewModel.setUserInteractionStatus(UserInteractionStatus.BROWSE)
                                    },
                                    content = {
                                        Icon(
                                            painter = painterResource(R.drawable.tick),
                                            contentDescription = "Finish Editing Items",
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.userInteractionStatus == UserInteractionStatus.BROWSE || uiState.userInteractionStatus == UserInteractionStatus.ADD
                ) {
                    if (uiState.userInteractionStatus == UserInteractionStatus.BROWSE) {
                        ExtendedFloatingActionButton(
                            modifier = Modifier.padding(vertical=12.dp,horizontal=6.dp).testTag("addItem"),
                            onClick = {
                                viewModel.setUserInteractionStatus(UserInteractionStatus.ADD)
                            },
                            text = {
                                Text("Add Item")
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.add),
                                    contentDescription = "Add Item",
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

            if (uiState.userInteractionStatus == UserInteractionStatus.ADD) {
                AddIngredientBottomSheet(
                    ingredient = uiState.ingredientToAdd,
                    action = viewModel::onIngredientSheetAction,
                    possibleUnits = listOf(
                        IngredientUnit.ML, IngredientUnit.L, IngredientUnit.G, IngredientUnit.KG, IngredientUnit.PINT
                    ),
                    text = AddIngredientSheetText(
                        title = "Add an item to your Shopping List",
                        name = "Enter an item to add",
                        amount = "Enter amount (optional)",
                        unit = "Select unit (optional)",
                        complete = "Add to Shopping List"
                    ),
                    noneUnit = IngredientUnit.SINGLE
                )
            }

            if (
                uiState.ingredientToEdit.index != -1
            ) {
                EditIngredientBottomSheet(
                    onDismiss = { viewModel.setIngredientIndexToEdit(-1) },
                    unitName = uiState.shoppingListEntries.getOrNull(uiState.ingredientToEdit.index)?.unit?.singularName ?: "",
                    amount = uiState.ingredientToEdit.amount,
                    setAmount = viewModel::setEditIngredientAmount,
                    persistAmountUpdate = viewModel::updateIngredientAmount,
                    keyboardFocusManager = keyboardFocusManager
                )
            }


        }
    }
}

@Composable
fun UnitSelectionButton(
    selectedUnit: IngredientUnit,
    onUnitChange: (IngredientUnit) -> Unit,
    text: String,
    currentUnit: IngredientUnit
) {
    Button(
        onClick = { onUnitChange(currentUnit) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.padding(6.dp),
        border =
            if (selectedUnit == currentUnit) {
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            } else {
                null
            }
    ) {
        Text(text)
    }
}

@Composable
fun IngredientAmount(
    unit: IngredientUnit,
    amount: Double,
    formatIngredientAmount: (Double, Boolean) -> String,
    editModifierFunctionality: () -> Modifier = { Modifier },
    editContent: @Composable () -> Unit = {},
    round: Boolean = true,
) {
    if (unit != IngredientUnit.SINGLE) {
        Row(
            modifier = Modifier.fillMaxHeight()
                .padding(vertical = 4.dp)
                .padding(horizontal = 8.dp)
                .background(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.background
                )
                .clip(RoundedCornerShape(8.dp))
                .then(
                    editModifierFunctionality()
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val round = round && !(unit == IngredientUnit.KG || unit == IngredientUnit.L)
            Text(
                modifier = Modifier
                    .padding(6.dp),
                text = "${
                    formatIngredientAmount(
                        if (round) {
                            ceil(amount) // gets ceiled because cant buy 0.5 of a lemon e.g.. BUT should still store the decimal because required for calc e.g. ifuser adds 0.5 lemon twice, it shuold still say 1 lemon.
                        } else {
                            // dont ceil if kg or L - these need to be precise. or if in the user created recipe.
                            amount
                        },
                        round
                    )
                }${
                    if (unit == IngredientUnit.UNIT) {
                        ""
                    } else {
                        if (ceil(amount) > 1) {
                            " " + unit.pluralName
                        } else {
                            " " + unit.singularName
                        }
                    }
                }"
            )
            editContent.invoke()
        }
    }

}
