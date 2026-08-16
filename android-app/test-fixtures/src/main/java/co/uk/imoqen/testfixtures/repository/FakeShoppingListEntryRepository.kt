package co.uk.imoqen.testfixtures.repository

import co.uk.imoqen.testfixtures.common.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntry
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient

/**
 * Mock ShoppingListEntryRepository for tests.
 */
class FakeShoppingListEntryRepository(): ShoppingListEntryRepository {
    override fun getAll(uid: String): Flow<List<ShoppingListEntry>> {
        if (uid == "user_with_ingr_to_convert_l_and_kg") {
            return flowOf(listOf(TestData.shoppingListEntry_toConvertToL, TestData.shoppingListEntry_toConvertToKG))
        }
        if (uid == "user_who_throws_error") {
            throw Exception() // to test how the methods deal with exceptions
        }
        if (uid == "valid_user_shoppinglist_clearPurchased") {
            return flowOf(TestData.shoppingListEntries) // no change made; as none have been purchased
        }
        if (uid == "valid_user_shoppinglist_clearAll") {
            return flowOf(listOf<ShoppingListEntry>()) // empty as all have been deleted
        }
        return flowOf(listOf(TestData.shoppingListEntry_oatMilk, TestData.shoppingListEntry_cherries))
    }

    override fun getById(id: Long): Flow<ShoppingListEntry?> {
        if (id == 0L) { // first element
            return flowOf(TestData.shoppingListEntry_cherries)
        }
        if (id == 2L || id == 3L || id == 4L) { // errors to simulate out of bounds
            return flowOf(null)
        }
        // second element:
        return flowOf(TestData.shoppingListEntry_oatMilk)
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
        // do nothing to simulate success
    }

    override suspend fun delete(shoppingListEntry: ShoppingListEntry) {
        // do nothing to simulate success
    }

    override suspend fun deleteAllForUser(uid: String) {
        // do nothing to simulate success
    }

    override suspend fun deletePurchasedForUser(uid: String) {
        // do nothing to simulate success
    }

    override suspend fun setOrdinal(id: Long, ordinal: Int) {
        // do nothing to simulate success
    }

    override suspend fun updatePurchaseStatus(id: Long, purchased: Boolean) {
        if (id == 2L) { //exceeds list bounds
            throw Exception()
        }
        // else, do nothing to simulate success
    }
}