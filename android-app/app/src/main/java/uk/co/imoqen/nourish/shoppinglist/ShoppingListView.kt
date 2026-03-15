package uk.co.imoqen.nourish.shoppinglist

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishConfirmationDialog
import uk.co.imoqen.nourish.common.NourishDivider
import uk.co.imoqen.nourish.common.NourishErrorText
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.recipebook.RecipeLoadingBar
import uk.co.imoqen.nourish.recipediscovery.FilterState
import uk.co.imoqen.nourish.recipediscovery.restart
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

    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // makes it fill the screen with the required amount rather than stuck at half the screen size
    )

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

                    if (shoppingListItem.plural.contains(uiState.searchTerm)) { // applies the search
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
                                    if (shoppingListItem.unit != IngredientUnit.SINGLE) {
                                        Row() {
                                            val round = !(shoppingListItem.unit == IngredientUnit.KG || shoppingListItem.unit == IngredientUnit.L)
                                            Text(
                                                modifier = Modifier
                                                    .padding(vertical = 4.dp)
                                                    .padding(horizontal = 8.dp)
                                                    .background(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.background
                                                    )
                                                    .padding(6.dp),
                                            text = "${
                                                    viewModel.formatIngredientAmount(
                                                        if (round) {
                                                        ceil(shoppingListItem.amount) // gets ceiled because cant buy 0.5 of a lemon e.g.. BUT should still store the decimal because required for calc e.g. ifuser adds 0.5 lemon twice, it shuold still say 1 lemon.
                                                        } else {
                                                            // dont ceil if kg or L - these need to be precise.
                                                            shoppingListItem.amount
                                                        },
                                                        round
                                                    )
                                                } ${
                                                    if (ceil(shoppingListItem.amount) > 1) {
                                                        shoppingListItem.unit.pluralName
                                                    } else {
                                                        shoppingListItem.unit.singularName
                                                    }
                                                }"
                                            )
                                            if (uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.setIngredientIndexToEdit(i)
                                                        viewModel.setEditIngredientAmount("${shoppingListItem.amount}") // so the user can updte the existing
                                                    }
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.edit),
                                                        contentDescription = "Edit Item Amount",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    //Spacer(modifier = Modifier.weight(1f))
                                    // buttons, amounts, etc, go to the very end
                                    if (uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                                        // delete the item
                                        IconButton(
                                            onClick = {
                                                viewModel.setItemIndexToDelete(i)
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.delete),
                                                contentDescription = "Delete Item",
                                                tint = MaterialTheme.colorScheme.error.copy(
                                                    alpha = 0.8f
                                                )
                                            )
                                        }

                                        // move the item
                                        IconButton(
                                            modifier = Modifier.draggableHandle(
                                                onDragStarted = {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.GestureThresholdActivate
                                                    )
                                                },
                                                onDragStopped = {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.GestureEnd
                                                    )
                                                },
                                            ),
                                            onClick = {},
                                        ) {
                                            Icon(
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
                                modifier = Modifier.padding(6.dp),
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
                                containerColor = MaterialTheme.colorScheme.background
                            )
                            ExtendedFloatingActionButton(
                                modifier = Modifier.padding(6.dp),
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
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        }
                        // Edit button:
                        if (uiState.userInteractionStatus == UserInteractionStatus.BROWSE || uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                            if (uiState.userInteractionStatus == UserInteractionStatus.BROWSE) {
                                ExtendedFloatingActionButton(
                                    modifier = Modifier.padding(6.dp),
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
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            }
                            if (uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                                FloatingActionButton(
                                    modifier = Modifier.padding(6.dp),
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
                                    containerColor = MaterialTheme.colorScheme.background
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
                            modifier = Modifier.padding(6.dp),
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
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }

            if (uiState.userInteractionStatus == UserInteractionStatus.ADD) {

                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.setUserInteractionStatus(UserInteractionStatus.BROWSE)
                    },
                    sheetState = modalBottomSheetState
                ) {
                    val keyboardFocusManager = LocalFocusManager.current // keyboard focus manager which is local to the modal bottom sheet. doesnt work otherwise.

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .padding(bottom = 20.dp), // space from bottom so not cramped
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        NourishTextDivider(
                            "Add an item to your Shopping List",
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        NourishTextField(
                            value = uiState.addIngredientName,
                            onValueChange = { viewModel.setAddIngredientName(it) },
                            placeholder = "Enter item to add", // user language consideration: although the app is built for ingredients, users will likely add other things. hence, "item" is used.
                            onDone = { keyboardFocusManager.clearFocus(force=true) }
                        )
                        // Display item name error
                        if (uiState.addIngredientError != "") {
                            NourishErrorText(
                                uiState.addIngredientError
                            )
                        }

                        NourishTextDivider(
                            "Enter amount (optional)",
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        NourishTextField(
                            value = uiState.addIngredientAmount,
                            onValueChange = {
                                // Only allow valid double inputs
                                viewModel.setAddIngredientAmount(it)
                                            },
                            placeholder = "Enter amount", // user language consideration: although the app is built for ingredients, users will likely add other things. hence, "item" is used.
                            onDone = { keyboardFocusManager.clearFocus(force=true) },
                            keyboardType = KeyboardType.Number // only allows numeric input
                        )

                        NourishTextDivider(
                            "Select unit (optional)",
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        LazyRow(
                            modifier = Modifier.background(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            item {
                                UnitSelectionButton(
                                    viewModel,
                                    uiState,
                                    "none",
                                    IngredientUnit.SINGLE
                                ) // dynamically determined: if amount is given, then will use UNIT. otherwise will use SINGLE.
                            }
                            item {
                                UnitSelectionButton(viewModel, uiState, "ml", IngredientUnit.ML)
                            }
                            item {
                                UnitSelectionButton(viewModel, uiState, "l", IngredientUnit.L)
                            }
                            item {
                                UnitSelectionButton(viewModel, uiState, "g", IngredientUnit.G)
                            }
                            item {
                                UnitSelectionButton(viewModel, uiState, "kg", IngredientUnit.KG)
                            }
                            item {
                                UnitSelectionButton(viewModel, uiState, "pint", IngredientUnit.PINT)
                            }
                        }

                        NourishButton(
                            onClick = {
                                // add the ingredient
                                viewModel.addIngredientToShoppingList()
                            },
                            text = "Add to Shopping List",
                        )
                    }
                }
            }

            if (
                uiState.ingredientIndexToEdit != -1
            ) {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.setIngredientIndexToEdit(-1)
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        NourishTextDivider("Edit the amount")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NourishTextField(
                                modifier = Modifier.fillMaxWidth(0.6f),
                                value = uiState.editIngredientAmount,
                                onValueChange = {
                                    // Only allow valid double inputs
                                    viewModel.setEditIngredientAmount(it)
                                },
                                placeholder = "Enter new amount", // user language consideration: although the app is built for ingredients, users will likely add other things. hence, "item" is used.
                                onDone = { keyboardFocusManager.clearFocus(force = true) },
                                keyboardType = KeyboardType.Number // only allows numeric input
                            )
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = uiState.shoppingListEntries.getOrNull(uiState.ingredientIndexToEdit)?.unit?.singularName
                                    ?: ""
                            )
                        }

                        NourishButton(
                            onClick = {
                                // add the ingredient
                                viewModel.updateIngredientAmount()
                            },
                            text = "Update Amount",
                        )
                    }
                }
            }


        }
    }
}

@Composable
fun UnitSelectionButton(
    viewModel: ShoppingListViewModel,
    uiState: ShoppingListUiState,
    text: String,
    unit: IngredientUnit
) {
    Button(
        onClick = { viewModel.setAddIngredientUnit(unit) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.padding(6.dp),
        border =
            if (uiState.addIngredientUnit == unit) {
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
