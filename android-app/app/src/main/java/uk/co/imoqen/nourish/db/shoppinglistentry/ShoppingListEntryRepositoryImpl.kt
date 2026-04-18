package uk.co.imoqen.nourish.db.shoppinglistentry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient
import javax.inject.Inject

/**
 * The ShoppingListEntryRepositoryImpl, implementing the repository for ShoppingListEntry by
 * interacting with the required DAO.
 * Doc comments are not provided for most methods, because most are trivial. For those which are non
 * -trivial, a doc comment is present.
 * @param shoppingListEntryDao - the ShoppingListEntry DAO
 */
class ShoppingListEntryRepositoryImpl @Inject constructor(
    private val shoppingListEntryDao: ShoppingListEntryDao
): ShoppingListEntryRepository {

    override fun getAll(uid: String): Flow<List<ShoppingListEntry>> {
        return shoppingListEntryDao.getAll(uid)
    }

    override fun getById(id: Long): Flow<ShoppingListEntry?> {
        return shoppingListEntryDao.getById(id)
    }

    /**
     * Private, non-overriding auxiliary function for addShoppingListEntry.
     * Finds all unpurchased items by ingredient id, uid, and unit.
     * @param ingredientId - the id of the ingredient to find
     * @param uid - the id of the user which authored the ingredient to find.
     * @param unit - the unit of the ingredient to find.
     */
    fun getUnpurchasedByIngredientAndUidAndUnit(
        ingredientId: Long,
        uid: String,
        unit: IngredientUnit
    ): Flow<ShoppingListEntry?> {
        return shoppingListEntryDao.getByIngredientAndUidAndUnitUnpurchased(ingredientId, uid, unit)
    }

    /**
     * Adds a shopping list entry to the database.
     * @param shoppingListIngredient - The ingredient to add.
     * @param uid - The id of the user adding the entry.
     * @param userAdded - declares if added from a recipe, or manually added by the user. user has
     *                    more freedom than adding from a recipe.
     */
    override suspend fun addShoppingListEntry(
        shoppingListIngredient: ShoppingListIngredient,
        uid: String,
        userAdded: Boolean,
    ) {
        val unit = shoppingListIngredient.unit
        val amount = shoppingListIngredient.amount
        val ingredientId = shoppingListIngredient.ingredientId
        // Checks that a shopping list entry doesn't already exist for this ingredient by this uid.
        // If it does, simply increment the amount if appropriate.
        val existingShoppingListEntry =
            getUnpurchasedByIngredientAndUidAndUnit(ingredientId, uid, unit).first()
        if (existingShoppingListEntry != null && !existingShoppingListEntry.purchased) {
            // if the unit should be displayed in the shopping list, or it is added by the user, update it
            update(existingShoppingListEntry.copy(amount =
                // only update it if the unit should be kept in the shopping list & above the threshold
                if (unit.keepUnitInShoppingList || userAdded) {
                    // user has more freedom, if user added it is assumed that they intend to add
                    // even if its something like oil, salt, etc.
                    existingShoppingListEntry.amount.plus(
                        shoppingListIngredient.amount
                    )
                } else {
                    1.0
                }
            ))
        } else {
            // else, insert a new ShoppingListEntry
            shoppingListEntryDao.insert(
                ShoppingListEntry(
                    ingredientId = ingredientId,
                    purchaserUid = uid,
                    unit = unit,
                    amount = amount,
                    // set by default to not have been purchased:
                    purchased = false,
                    // increment max to get next val (put it at the end of the list by default):
                    ordinal = getMaxOrdinalForUser(uid) + 1
                )
            )
        }
    }

    /**
     * Private auxiliary non-overridden method to find the max ordinal, this incremented will be the
     * position of the next item to add.
     * @param uid - The id of the user adding the entry.
     */
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

    override suspend fun deletePurchasedForUser(uid: String) {
        shoppingListEntryDao.deletePurchasedForUser(uid)
    }

    override suspend fun updatePurchaseStatus(id: Long, purchased: Boolean) {
        val existingEntry = getById(id).first()
        if (existingEntry == null) {
            throw NullPointerException()
        }
        shoppingListEntryDao.update(
            existingEntry.copy(
                purchased = purchased,
            )
        )
    }

}