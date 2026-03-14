package uk.co.imoqen.nourish.shoppinglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.common.NourishTextField

/**
 * Shopping list view
 */
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
            // Shopping List
            LazyColumn(state = lazyListState) {
                // Lists the items, using the primary key as its key: stable.
                itemsIndexed(uiState.shoppingListEntries, key = { i, shoppingListItem -> shoppingListItem.id }) { i, shoppingListItem ->
                    if (shoppingListItem.plural.contains(uiState.searchTerm)) { // applies the search
                        // Uses ReorderableItem library: https://github.com/Calvin-LL/Reorderable
                        ReorderableItem(reorderableLazyListState, key = shoppingListItem.id, modifier = Modifier.fillMaxWidth().padding(2.dp)) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                            Surface(
                                shadowElevation = elevation,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row( // min height and can expand if needed, if the ingredient is very long
                                    modifier = Modifier.heightIn(48.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = shoppingListItem.purchased,
                                        onCheckedChange = {
                                            val index = uiState.shoppingListEntries.indexOfFirst { it.id == shoppingListItem.id }
                                            if (index != -1) {
                                                // update the UI and DB with the purchase state
                                                viewModel.updatePurchasedState(
                                                    index,
                                                    shoppingListItem.copy(purchased = !shoppingListItem.purchased)
                                                )
                                            }
                                        },
                                    )
                                    Text(
                                        "${shoppingListItem.plural} ${shoppingListItem.amount} ${shoppingListItem.unit.name}",
                                        Modifier.padding(horizontal = 8.dp),
                                        textDecoration = if (shoppingListItem.purchased) {
                                            TextDecoration.LineThrough
                                        } else TextDecoration.None,
                                    )

                                    Spacer(modifier = Modifier.weight(1f))
                                    // buttons, amounts, etc, go to the very end
                                    if (uiState.userInteractionStatus == UserInteractionStatus.EDIT) {
                                        // delete the item
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteShoppingListItem(shoppingListItem.id, i)
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.delete),
                                                contentDescription = "Deleye Item",
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
                    FloatingActionButton(
                        modifier = Modifier.padding(6.dp),
                        onClick = {
                            if (uiState.userInteractionStatus == UserInteractionStatus.BROWSE) {
                                viewModel.setUserInteractionStatus(UserInteractionStatus.EDIT)
                            } else {
                                viewModel.setUserInteractionStatus(UserInteractionStatus.BROWSE)
                            }
                        },
                    ) {
                        if (uiState.userInteractionStatus == UserInteractionStatus.BROWSE) {
                            Icon(
                                painter = painterResource(R.drawable.edit_note),
                                contentDescription = "Edit Items",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                    alpha = 0.8f
                                )
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.tick),
                                contentDescription = "Finish Editing Items",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.userInteractionStatus == UserInteractionStatus.BROWSE || uiState.userInteractionStatus == UserInteractionStatus.ADD
                ) {
                    FloatingActionButton(
                        modifier = Modifier.padding(6.dp),
                        onClick = {
                            viewModel.setUserInteractionStatus(UserInteractionStatus.ADD)
                        },
                    ) {
                        if (uiState.userInteractionStatus == UserInteractionStatus.BROWSE) {
                            Icon(
                                painter = painterResource(R.drawable.add),
                                contentDescription = "Add Item",
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
