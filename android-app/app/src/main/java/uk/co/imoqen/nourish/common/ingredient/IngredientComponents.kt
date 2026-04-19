package uk.co.imoqen.nourish.common.ingredient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishErrorText
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.shoppinglist.UnitSelectionButton

/**
 * The modal bottom sheet Composable for adding ingredients. Used in both the shopping list and
 * recipe creation.
 * @param ingredient - The UiState of the ingredient to add, filled in by the user.
 * @param action - The possible IngredientSheetActions within this composable
 * @param possibleUnits - The possible units of an ingredient to add. These differ for the shopping
 *                        list vs the recipe creation.
 * @param text - The text to display as the title in the bottom sheet
 * @param noneUnit - The unit which should be set when no unit has been selected by the user; the
 *                   "default unit". For the shopping list, this is SINGLE. for the recipe creation,
 *                   this is UNIT.
 *
 * @author Imogen Pemberton, 2026
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientBottomSheet(
    ingredient: AddIngredientSheetState,
    action: (IngredientSheetAction) -> Unit,
    possibleUnits: List<IngredientUnit>,
    text: AddIngredientSheetText,
    noneUnit: IngredientUnit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = {
            action(IngredientSheetAction.OnDismissRequest())
        },
        sheetState = modalBottomSheetState
    ) {
        val keyboardFocusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .padding(bottom = 20.dp), // space from bottom so not cramped
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NourishTextDivider(
                text.title,
                modifier = Modifier.padding(top = 6.dp)
            )
            NourishTextField(
                value = ingredient.name,
                onValueChange = { action(IngredientSheetAction.SetIngredientName(it)) },
                placeholder = text.name,
                onDone = { keyboardFocusManager.clearFocus(force=true) }
            )

            NourishTextDivider(
                text.amount,
                modifier = Modifier.padding(top = 6.dp)
            )
            NourishTextField(
                value = ingredient.amount,
                onValueChange = {
                    // Only allow valid double inputs
                    action(IngredientSheetAction.SetIngredientAmount(it))
                },
                placeholder = "Enter amount",
                onDone = { keyboardFocusManager.clearFocus(force=true) },
                keyboardType = KeyboardType.Number // only allows numeric input
            )

            NourishTextDivider(
                text.unit,
                modifier = Modifier.padding(top = 6.dp)
            )

            LazyRow(
                modifier = Modifier.background(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                // unit selection is done separately, it is always possible. Has a different output "none"
                item {
                    UnitSelectionButton(
                        ingredient.unit,
                        { action(IngredientSheetAction.SetIngredientUnit(it)) },
                        "none",
                        noneUnit
                    ) // dynamically determined: if amount is given, then will use UNIT. otherwise will use SINGLE.
                }
                possibleUnits.forEach{ unit ->
                    item {
                        UnitSelectionButton(
                            ingredient.unit,
                            { action(IngredientSheetAction.SetIngredientUnit(it)) },
                            unit.singularName,
                            unit
                        )
                    }
                }
            }

            // Display error
            if (ingredient.error != "") {
                NourishErrorText(ingredient.error)
            }

            NourishButton(
                onClick = {
                    // add the ingredient
                     action(IngredientSheetAction.OnComplete())
                },
                text = text.complete,
            )
        }
    }
}

/**
 * The modal bottom sheet Composable for editing ingredients.
 * @param onDismiss - to close the modal bottom sheet.
 * @param unitName - the name of the unit to edit.
 * @param amount - the amount of the unit to edit.
 * @param setAmount - function to alter the amount of the unit being edited.
 * @param persistAmountUpdate - function to save changes to the db; update the change in the db.
 * @param keyboardFocusManager - focus manager for the keyboard, to close it when Enter is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredientBottomSheet(
    onDismiss: () -> Unit,
    unitName: String,
    amount: String,
    setAmount: (String) -> Unit,
    persistAmountUpdate: () -> Unit,
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

            NourishTextDivider("Edit the amount")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NourishTextField(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    value = amount,
                    onValueChange = {
                        // Only allow valid double inputs
                        setAmount(it)
                    },
                    placeholder = "Enter new amount",
                    onDone = { keyboardFocusManager.clearFocus(force = true) },
                    keyboardType = KeyboardType.Number // only allows numeric input
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = unitName
                )
            }

            NourishButton(
                onClick = {
                    // add the ingredient
                    persistAmountUpdate()
                },
                text = "Update Amount",
            )
        }
    }
}