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

    LaunchedEffect("POPULATE SHOPPING LIST") {
        viewModel.populateShoppingList()
        val mymap = uiState.shoppingListEntries.associateWith { SlideState.NONE }.toMutableMap()
        viewModel.setShoppingListSliderState(mymap)
    }

    val isPlaced = remember { mutableStateOf(false) }
    val currentIndex = remember { mutableIntStateOf(-1) }
    val destinationIndex = remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    var itemHeightPx = 0
    val itemHeightDp = remember { mutableStateOf(50.dp) }

    LaunchedEffect(isPlaced.value) {
        if (isPlaced.value) {
            launch {
                if (currentIndex.intValue != destinationIndex.intValue) {
                    // that makes the swap, will need to replace it with our db logic so it persists
                    Collections.swap(uiState.shoppingListEntries, currentIndex.intValue, destinationIndex.intValue)
                    uiState.slideStates?.putAll(uiState.shoppingListEntries.associateWith { SlideState.NONE }.toMutableMap())
                }
                isPlaced.value = false
            }
        }
    }
    with(LocalDensity.current) {
        itemHeightPx = itemHeightDp.value.toPx().toInt()
    }

    val hapticFeedback = LocalHapticFeedback.current
    

    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
        Log.i("old list:", "${uiState.shoppingListEntries}")
        val list = uiState.shoppingListEntries.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        Log.i("new list:", "${list}")
        viewModel.setShoppingListEntries(list)

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LazyColumn(state = listState) {
        items(uiState.shoppingListEntries, key = { it.id }) { // the primary key of the item is its key, this is stable as it will never change
            ReorderableItem(reorderableLazyListState, key = it) { isDragging ->
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


    /*LazyColumn(state = listState
    ) {

        itemsIndexed(uiState.shoppingListEntries) { i, shoppingListEntry -> // maybe the index in the list will come in handy in the future. if not, delete.
            key(i) {
                val slideState = uiState.slideStates!![shoppingListEntry] ?: SlideState.NONE

                val verticalTranslation by animateIntAsState(
                    targetValue = when (slideState) {
                        SlideState.UP -> -50
                        SlideState.DOWN -> 50
                        else -> 0
                    },
                    label = "drag_to_reorder_vertical_translation"
                )

                Row(
                    modifier = Modifier.dragToReorder(
                        item = uiState.shoppingListEntries[i],
                        itemList = uiState.shoppingListEntries,
                        itemHeight = 50,
                        updateSlideState = {param, state -> uiState.slideStates!![param] = state},
                        onStartDrag = { index -> currentIndex.intValue = index },
                        onStopDrag = { currIndex: Int, destIndex: Int ->
                            isPlaced.value = true
                            currentIndex.intValue = currIndex
                            destinationIndex.intValue = destIndex
                        }),
                    )
                 {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f).height(50.dp)
                    ) {
                        Text(shoppingListEntry.plural)

                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                        ) {
                            if (shoppingListEntry.unit != IngredientUnit.SINGLE) { // single has no unit or amount, e.g. salt
                                Text(viewModel.formatIngredientAmount(ceil(shoppingListEntry.amount))) // amount is always rounded up e.g. if need 0.5 of a banana, then need to buy 1 whole banana. but the unrounded value is stored so you dont buy more than you need.
                                Text(shoppingListEntry.unit.pluralName)
                            }
                        }
                        // then button to move, edit the amount or delete. edit and delete will only be usable if in edit mode
                    }
                }
            }
        }
    }*/
}

enum class SlideState { NONE, UP, DOWN }


fun <T> Modifier.dragToReorder(
    item: T, // The current item being dragged.
    itemList: List<T>, // The entire list of the items onscreen.
    itemHeight: Int, // The height of an item (in pixels).
    updateSlideState: (item: T, slideState: SlideState) -> Unit, // Callback to update the slide state of an item.
    onStartDrag: (currIndex: Int) -> Unit = {}, // Callback invoked when dragging begins and exposes the index of the item being dragged.
    onStopDrag: (currIndex: Int, destIndex: Int) -> Unit // Call invoked when drag is finished.
): Modifier = composed {
    // Keep track of the of the animated vertical drag offset to allow for a smooth UI.
    val offsetY = remember { Animatable(0f) }

    // Handle pointer input for detecting gestures (in this case, drag).
    pointerInput(Unit) {
        // Wrap in a coroutine scope to use suspend functions for touch events and animation.
        coroutineScope {
            // The index of the current item in the list.
            val itemIndex = itemList.indexOf(item)
            // Threshold for when an item should be considered as moved to a new position in the list.
            // Needs to be at least a half of the height of the item, but this can be modified as needed.
            val offsetToSlide = itemHeight / 2

            // Variables to track the number of items slid over and the total offset.
            var numberOfSlidItems = 0
            var previousNumberOfItems: Int
            var listOffset = 0

            // Invoked when the dragging operation has started.
            val onDragStart = {
                // Interrupt any ongoing animation of other items.
                launch {
                    offsetY.stop()
                }
                // Invoke the start drag callback with the current item's index.
                onStartDrag(itemIndex)
            }

            // Define the action to perform while dragging.
            val onDragging = { change: PointerInputChange ->
                // Calculate the new vertical drag offset.
                val verticalDragOffset = offsetY.value + change.position.y

                launch {
                    // Snap the offset to the calculated value.
                    offsetY.snapTo(verticalDragOffset)

                    // Determine the direction of the drag.
                    val offsetSign = offsetY.value.sign.toInt()

                    // Update the number of items slid over based on the drag offset.
                    previousNumberOfItems = numberOfSlidItems
                    numberOfSlidItems = calculateNumberOfSlidItems(
                        offsetY.value * offsetSign,
                        itemHeight,
                        offsetToSlide,
                        previousNumberOfItems
                    )

                    // Update the visual state of the items being slid over.
                    if (previousNumberOfItems > numberOfSlidItems) {
                        updateSlideState(
                            itemList[itemIndex + previousNumberOfItems * offsetSign],
                            SlideState.NONE
                        )
                    } else if (numberOfSlidItems != 0) {
                        try {
                            updateSlideState(
                                itemList[itemIndex + numberOfSlidItems * offsetSign],
                                if (offsetSign == 1) SlideState.UP else SlideState.DOWN
                            )
                        } catch (e: IndexOutOfBoundsException) {
                            numberOfSlidItems = previousNumberOfItems
                        }
                    }
                    // Update the total offset based on the number of items slid over.
                    listOffset = numberOfSlidItems * offsetSign
                }
                // Consume the gesture event, not passed to external.
                if (change.positionChange() != Offset.Zero) change.consume()
            }

            // Define the action to perform when dragging ends.
            val onDragEnd = {
                launch {
                    // Animate the vertical offset of the dragged item to its final position.
                    // The final position is calculated based on the number of items slid over (`numberOfSlidItems`),
                    // the height of each item (`itemHeight`), and the direction of the drag (`offsetY.value.sign`).
                    offsetY.animateTo(itemHeight * numberOfSlidItems * offsetY.value.sign)
                    // Invoke the stop drag callback with the current item's index and its final index.
                    onStopDrag(itemIndex, itemIndex + listOffset)
                }
            }

            // Detect drag gestures after a long press, invoking the defined actions.
            detectDragGesturesAfterLongPress(
                onDragStart = { onDragStart() },
                onDrag = { change, _ -> onDragging(change) },
                onDragEnd = { onDragEnd() }
            )
        }
    }.offset {
        // Apply the calculated vertical offset using 0 for `x` because we are only interested in the `y` drag.
        IntOffset(0, offsetY.value.roundToInt())
    }
}

private const val NO_ITEMS_MOVED = 0

// Helper function to calculate the number of items that have been slid over based on the drag offset.
private fun calculateNumberOfSlidItems(
    offsetY: Float, // The current vertical offset of the dragged item.
    itemHeight: Int, // The height of each item in the list.
    offsetToSlide: Int, // The minimum vertical distance needed to consider an item as moved.
    previousNumberOfItems: Int // The previously calculated number of items moved past.
): Int {
    // Calculate the number of items that would fit in the current vertical offset.
    val numberOfItemsInOffset = (offsetY / itemHeight).toInt()

    // Calculate the number of items considering the threshold for moving (`offsetToSlide`).
    // Adding `offsetToSlide` ensures we're looking slightly beyond the current offset to see if another item has been moved past.
    val numberOfItemsPlusOffset = ((offsetY + offsetToSlide) / itemHeight).toInt()

    // Calculate the number of items considering the threshold for moving (`offsetToSlide`).
    // Subtracting `offsetToSlide` - 1 ensures we're looking slightly before the current offset to account for potential rounding errors.
    // The subtraction of 1 is to ensure that we don't prematurely count an item as moved when it's very close but hasn't quite reached the threshold.
    // This number can be tweaked to achieve different behaviors depending on the need.
    val numberOfItemsMinusOffset = ((offsetY - offsetToSlide - 1) / itemHeight).toInt()

    // Determine the number of items moved past based on the calculated values.
    return when {
        offsetY - offsetToSlide - 1 < 0 -> NO_ITEMS_MOVED
        numberOfItemsPlusOffset > numberOfItemsInOffset -> numberOfItemsPlusOffset
        numberOfItemsMinusOffset < numberOfItemsInOffset -> numberOfItemsInOffset
        else -> previousNumberOfItems
    }
}