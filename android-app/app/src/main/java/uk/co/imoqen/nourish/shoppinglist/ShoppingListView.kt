package uk.co.imoqen.nourish.shoppinglist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import kotlin.math.ceil

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
    }

    LazyColumn {
        itemsIndexed(uiState.shoppingListEntries) { i, shoppingListEntry -> // maybe the index in the list will come in handy in the future. if not, delete.
            Row(
                modifier = Modifier.fillMaxWidth(0.9f)
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