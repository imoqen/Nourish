package co.uk.imoqen.testfixtures.repository

import co.uk.imoqen.testfixtures.common.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntry
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient

class FakeShoppingListEntryRepository(): ShoppingListEntryRepository {
    override fun getAll(uid: String): Flow<List<ShoppingListEntry>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): Flow<ShoppingListEntry?> {
        if (id != 1L) { // errors to simulate out of bounds
            return flowOf(null)
        }
        return flowOf(TestData.shoppingListEntry_oatMilk)
    }

    override suspend fun insert(shoppingListEntry: ShoppingListEntry): Long {
        TODO("Not yet implemented")
    }

    override suspend fun addShoppingListEntry(
        shoppingListIngredient: ShoppingListIngredient,
        uid: String,
        userAdded: Boolean
    ) {
        // simulate error so that onFailure response can be checked:
        if (shoppingListIngredient == TestData.shoppingListIngredient_triggerError) {
            throw Exception()
        }
        // otherwise, simply do nothing to simulate success
    }

    override suspend fun update(shoppingListEntry: ShoppingListEntry) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(shoppingListEntry: ShoppingListEntry) {
        // do nothing to simulate success
    }

    override suspend fun deleteAllForUser(uid: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePurchasedForUser(uid: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setOrdinal(id: Long, ordinal: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePurchaseStatus(id: Long, purchased: Boolean) {
        TODO("Not yet implemented")
    }
}