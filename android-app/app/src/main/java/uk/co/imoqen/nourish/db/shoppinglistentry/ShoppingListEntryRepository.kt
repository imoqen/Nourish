package uk.co.imoqen.nourish.db.shoppinglistentry

import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient

interface ShoppingListEntryRepository {

    fun getAll(uid: String): Flow<List<ShoppingListEntry>>

    fun getById(id: Long): Flow<ShoppingListEntry?>

    suspend fun insert(shoppingListEntry: ShoppingListEntry): Long

    suspend fun addShoppingListEntry(shoppingListIngredient: ShoppingListIngredient, uid: String, userAdded: Boolean)

    suspend fun update(shoppingListEntry: ShoppingListEntry)

    suspend fun delete(shoppingListEntry: ShoppingListEntry)

    suspend fun setOrdinal(id: Long, ordinal: Int)
}