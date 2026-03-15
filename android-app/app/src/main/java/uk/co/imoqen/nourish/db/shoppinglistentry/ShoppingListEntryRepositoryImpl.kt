package uk.co.imoqen.nourish.db.shoppinglistentry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient
import javax.inject.Inject

class ShoppingListEntryRepositoryImpl @Inject constructor(
    private val shoppingListEntryDao: ShoppingListEntryDao
): ShoppingListEntryRepository {

    override fun getAll(uid: String): Flow<List<ShoppingListEntry>> {
        return shoppingListEntryDao.getAll(uid)
    }

    override fun getById(id: Long): Flow<ShoppingListEntry?> {
        return shoppingListEntryDao.getById(id)
    }

    // not override as private, used as helper method for addShoppingListEntry
    fun getUnpurchasedByIngredientAndUidAndUnit(ingredientId: Long, uid: String, unit: IngredientUnit): Flow<ShoppingListEntry?> {
        return shoppingListEntryDao.getByIngredientAndUidAndUnitUnpurchased(ingredientId, uid, unit)
    }

    override suspend fun insert(shoppingListEntry: ShoppingListEntry): Long {
        return shoppingListEntryDao.insert(shoppingListEntry)
    }

    override suspend fun addShoppingListEntry(
        shoppingListIngredient: ShoppingListIngredient,
        uid: String,
        userAdded: Boolean, // declares if added from a recipe, or was added by the user. user has more freedom than adding from a recipe.
    ) {
        val unit = shoppingListIngredient.unit
        val amount = shoppingListIngredient.amount
        val ingredientId = shoppingListIngredient.ingredientId

        // need to check that a shopping list entry doesnt already exist for this ingredient by this uid. if it does, simply increment
        // the amount if appropriate:

        val existingShoppingListEntry = getUnpurchasedByIngredientAndUidAndUnit(ingredientId, uid, unit).first()
        if (existingShoppingListEntry != null && !existingShoppingListEntry.purchased) {

            // if the unit should be displayed in the shopping list, or it is added by the user, update it
            update(existingShoppingListEntry.copy(amount =
                // only update it if the unit should be kept in the shopping list & above the threshold
                if (unit.keepUnitInShoppingList || userAdded) { // user has more freedom, if user added it is assumed that they intend to add even if its something like oil, salt, etc.
                    existingShoppingListEntry.amount.plus(
                        shoppingListIngredient.amount
                    )
                } else {
                    1.0
                }
            ))
        } else {
            // else, insert a new ShoppingListEntry
            insert(
                ShoppingListEntry(
                    ingredientId = ingredientId,
                    purchaserUid = uid,
                    unit = unit,
                    amount = amount,
                    purchased = false, // set by default to not have been purchased.
                    ordinal = getMaxOrdinalForUser(uid) + 1 // increment max to get next val (put it at the end of the list by default)
                )
            )
        }
    }

    // private helper method to find the max ordinal, this incremented will be the position of
    // the next item to add.
    suspend fun getMaxOrdinalForUser(uid: String): Int {
        return shoppingListEntryDao.getMaxOrdinalForUser(uid)
    }

    override suspend fun setOrdinal(id: Long, ordinal: Int) {
        shoppingListEntryDao.setOrdinal(id, ordinal)
    }

    override suspend fun update(shoppingListEntry: ShoppingListEntry) {
        shoppingListEntryDao.update(shoppingListEntry)
    }

    override suspend fun delete(shoppingListEntry: ShoppingListEntry) {
        shoppingListEntryDao.delete(shoppingListEntry)
    }

    override suspend fun deleteAllForUser(uid: String) {
        shoppingListEntryDao.deleteAllForUser(uid)
    }

    override suspend fun updatePurchaseStatus(id: Long, purchased: Boolean) {
        val existingEntry = getById(id).first()
        if (existingEntry == null) {
            throw NullPointerException()
        }
        shoppingListEntryDao.update(
            existingEntry.copy(
                purchased = purchased,
                /*ordinal = if (purchased) {
                    // if setting to purchased, then put the item at the end of the list
                    getMaxOrdinalForUser(existingEntry.purchaserUid) + 1
                } else {
                    // otherwise, setting back to unpurchased so put right
                    getMaxOrdinalForUser(existingEntry.purchaserUid)
                }*/
            )
        )
    }

}