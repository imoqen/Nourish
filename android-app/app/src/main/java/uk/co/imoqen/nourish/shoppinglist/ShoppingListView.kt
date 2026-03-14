package uk.co.imoqen.nourish.shoppinglist

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntry
import java.util.Collections
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * Shopping list view
 */
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect("POPULATE SHOPPING LIST") {
        viewModel.populateShoppingList()
    }

    var list by remember { mutableStateOf(List(100) { "Item $it" }) }
    val hapticFeedback = LocalHapticFeedback.current

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->

        Log.i("reord", "ering")
        // Update the UI:
        val list = uiState.shoppingListEntries.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        viewModel.setShoppingListEntries(list)

        // Persist the change locally:
        val fromKey = from.key as? Long
        val toKey = to.key as? Long
        viewModel.reorderShoppingList(fromKey, toKey)

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    /*
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        Log.i("old list:", "${uiState.shoppingListEntries}")
        val list = uiState.shoppingListEntries.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        Log.i("new list:", "${list}")
        viewModel.setShoppingListEntries(list)

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }*/

    LazyColumn(state = lazyListState) {
        items(uiState.shoppingListEntries, key = { it.id }) { // the primary key of the item is its key, this is stable as it will never change
            ReorderableItem(reorderableLazyListState, key = it.id) { isDragging ->
                Log.i("is dragging: ", "$isDragging")
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(shadowElevation = elevation) {
                    Row {
                        Text(it.plural, Modifier.padding(horizontal = 8.dp))
                        IconButton(
                            modifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    Log.i("drag started: ", "now")
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                },
                                onDragStopped = {
                                    Log.i("drag stopped: ", "now")
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                },
                            ),
                            onClick = {},
                        ) {
                            Icon(Icons.Rounded.Build, contentDescription = "Reorder")
                        }
                    }
                }
            }
        }
    }

}
