package uk.co.imoqen.nourish.db.shoppinglistentry

import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient

/**
 * The ShoppingListEntryRepository, providing a contract of methods to interact with the DAO regarding
 * ShoppingListEntry-related logic.
 *
 * @author Imogen Pemberton, 2026
 */

interface ShoppingListEntryRepository {

    fun getAll(uid: String): Flow<List<ShoppingListEntry>>

    fun getById(id: Long): Flow<ShoppingListEntry?>

    suspend fun addShoppingListEntry(
        shoppingListIngredient: ShoppingListIngredient,
        uid: String,
        userAdded: Boolean
    )

    suspend fun update(shoppingListEntry: ShoppingListEntry)

    suspend fun delete(shoppingListEntry: ShoppingListEntry)

    suspend fun deleteAllForUser(uid: String)

    suspend fun deletePurchasedForUser(uid: String)

    suspend fun setOrdinal(id: Long, ordinal: Int)

    suspend fun updatePurchaseStatus(id: Long, purchased: Boolean)
}