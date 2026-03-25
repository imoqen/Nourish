package uk.co.imoqen.nourish.shoppinglist

import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetState
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

data class ShoppingListUiState(
    val shoppingListEntries: List<ShoppingListUiEntry> = emptyList(), // list must be used,so when it is updated then the whole list is updated to trigger recomposition
    val searchTerm: String = "",
    val userInteractionStatus: UserInteractionStatus = UserInteractionStatus.BROWSE,
    val showDeleteAllDialog: Boolean = false,
    val showDeletePurchasedDialog: Boolean = false,
    val itemIndexToDelete: Int = -1,

    // adding item to shopping list:
    val ingredientToAdd: AddIngredientSheetState = AddIngredientSheetState(),
    // editing ingredient:
    val ingredientToEdit: IngredientToEdit = IngredientToEdit()
)

data class IngredientToEdit(
    val index: Int = -1,
    val amount: String = ""
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