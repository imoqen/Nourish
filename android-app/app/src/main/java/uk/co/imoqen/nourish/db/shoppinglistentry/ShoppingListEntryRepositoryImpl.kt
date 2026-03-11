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
    fun getByIngredientAndUidAndUnit(ingredientId: Long, uid: String, unit: IngredientUnit): Flow<ShoppingListEntry?> {
        return shoppingListEntryDao.getByIngredientAndUidAndUnit(ingredientId, uid, unit)
    }

    override suspend fun insert(shoppingListEntry: ShoppingListEntry): Long {
        return shoppingListEntryDao.insert(shoppingListEntry)
    }

    // will only
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

        val existingShoppingListEntry = getByIngredientAndUidAndUnit(ingredientId, uid, unit).first()
        if (existingShoppingListEntry != null) {

            // if not null AND not UNIT unit, update it
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
                    purchased = false // setby default to not have been purchased.
                )
            )
        }
    }

    override suspend fun update(shoppingListEntry: ShoppingListEntry) {
        shoppingListEntryDao.update(shoppingListEntry)
    }

    override suspend fun delete(shoppingListEntry: ShoppingListEntry) {
        shoppingListEntryDao.delete(shoppingListEntry)
    }

}