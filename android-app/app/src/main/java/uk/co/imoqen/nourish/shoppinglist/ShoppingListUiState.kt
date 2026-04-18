package uk.co.imoqen.nourish.shoppinglist

import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetState
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

/**
 * UiState for ShoppingList: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably, adhering to
 * Unidirectional Data Flow UDF.
 */
data class ShoppingListUiState(
    // List must be used, so when it is updated then the whole list is updated to trigger recomposition:
    val shoppingListEntries: List<ShoppingListUiEntry> = emptyList(),
    val searchTerm: String = "",
    val userInteractionStatus: UserInteractionStatus = UserInteractionStatus.BROWSE,
    val showDeleteAllDialog: Boolean = false,
    val showDeletePurchasedDialog: Boolean = false,
    val itemIndexToDelete: Int = -1,

    // Adding item to shopping list:
    val ingredientToAdd: AddIngredientSheetState = AddIngredientSheetState(),
    // Editing item in shopping list:
    val ingredientToEdit: IngredientToEdit = IngredientToEdit()
)

/**
 * To edit an ingredient:
 */
data class IngredientToEdit(
    val index: Int = -1,
    val amount: String = ""
)

/**
 * The status of user's interaction: browse, edit, or add.
 */
enum class UserInteractionStatus {
    BROWSE,
    EDIT, // edit the order or delete an item
    ADD // add a new item
}

/**
 * The ShoppingListEntry displayed in the UI, extracted from the database.
 */
data class ShoppingListUiEntry(
    val id: Long,
    val plural: String,
    val unit: IngredientUnit,
    val amount: Double,
    val purchased: Boolean,
    val ordinal: Int
)