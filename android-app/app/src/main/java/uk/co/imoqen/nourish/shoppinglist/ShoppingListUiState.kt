package uk.co.imoqen.nourish.shoppinglist

import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

data class ShoppingListUiState(

    val shoppingListEntries: List<ShoppingListUiEntry> = emptyList(),
    val searchTerm: String = "",
    val userInteractionStatus: UserInteractionStatus = UserInteractionStatus.BROWSE

)

enum class UserInteractionStatus {
    BROWSE,
    EDIT, // edit the order or delete an item
    ADD // add a new item
}

data class ShoppingListUiEntry(
    val id: Long,
    val plural: String,
    val unit: IngredientUnit,
    val amount: Double,
    val purchased: Boolean,
    val ordinal: Int
)