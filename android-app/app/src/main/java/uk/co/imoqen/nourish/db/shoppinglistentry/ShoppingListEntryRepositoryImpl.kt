package uk.co.imoqen.nourish.db.shoppinglistentry

import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.Recipe
import javax.inject.Inject

class ShoppingListEntryRepositoryImpl @Inject constructor(
    private val shoppingListEntryDao: ShoppingListEntryDao
): ShoppingListEntryRepository {

    override fun getAll(uid: String): Flow<List<Recipe>> {
        return shoppingListEntryDao.getAll(uid)
    }

    override fun getById(id: Long): Flow<Recipe?> {
        return shoppingListEntryDao.getById(id)
    }

    override suspend fun insert(shoppingListEntry: ShoppingListEntry): Long {
        return shoppingListEntryDao.insert(shoppingListEntry)
    }

    override suspend fun update(shoppingListEntry: ShoppingListEntry) {
        shoppingListEntryDao.update(shoppingListEntry)
    }

    override suspend fun delete(shoppingListEntry: ShoppingListEntry) {
        shoppingListEntryDao.delete(shoppingListEntry)
    }

}