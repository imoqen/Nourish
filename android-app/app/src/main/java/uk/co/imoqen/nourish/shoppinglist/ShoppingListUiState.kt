package uk.co.imoqen.nourish.shoppinglist

import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

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