package uk.co.imoqen.nourish.db.shoppinglistentry

import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.Recipe

interface ShoppingListEntryRepository {

    fun getAll(uid: String): Flow<List<Recipe>>

    fun getById(id: Long): Flow<Recipe?>

    suspend fun insert(shoppingListEntry: ShoppingListEntry): Long

    suspend fun update(shoppingListEntry: ShoppingListEntry)

    suspend fun delete(shoppingListEntry: ShoppingListEntry)

}