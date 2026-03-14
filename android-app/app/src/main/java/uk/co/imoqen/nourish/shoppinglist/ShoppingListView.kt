package uk.co.imoqen.nourish.shoppinglist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import uk.co.imoqen.nourish.R

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

    LaunchedEffect("POPULATE_SHOPPING_LIST") {
        viewModel.populateShoppingList()
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Update the UI:
        val list = uiState.shoppingListEntries.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        viewModel.setShoppingListEntries(list)

        // Persist the change locally by updating the DB:
        val fromKey = from.key as? Long
        val toKey = to.key as? Long
        viewModel.reorderShoppingList(fromKey, toKey)

        // Provide haptic feedback:
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LazyColumn(state = lazyListState) {
        // Lists the items, using the primary key as its key: stable.
        items(uiState.shoppingListEntries, key = { it.id }) {

            // Uses ReorderableItem library: https://github.com/Calvin-LL/Reorderable
            ReorderableItem(reorderableLazyListState, key = it.id) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(shadowElevation = elevation) {
                    Row {
                        Text(it.plural, Modifier.padding(horizontal = 8.dp))
                        IconButton(
                            modifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                },
                            ),
                            onClick = {},
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.drag_handle),
                                contentDescription = "Reorder Item",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }

}
