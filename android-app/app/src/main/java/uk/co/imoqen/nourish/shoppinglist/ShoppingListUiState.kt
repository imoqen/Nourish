package uk.co.imoqen.nourish.shoppinglist

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntry

data class ShoppingListUiState(

    val shoppingListEntries: List<ShoppingListUiEntry> = emptyList(),

)

data class ShoppingListUiEntry(
    val id: Long,
    val plural: String,
    val unit: IngredientUnit,
    val amount: Double,
    val purchased: Boolean,
)