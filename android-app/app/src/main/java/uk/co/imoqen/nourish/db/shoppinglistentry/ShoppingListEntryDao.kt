package uk.co.imoqen.nourish.db.shoppinglistentry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

/**
 * The ShoppingListEntryDao, holding logic to perform CRUD operations on the database relating to
 * ShoppingListEntry.
 */

@Dao
interface ShoppingListEntryDao {

    // Gets all for a specific user, no instance where would need to get for every user.
    // Also, orders by purchased status so that unpurchased items come first.
    @Query("SELECT * FROM shoppingListEntry WHERE purchaserUid = :uid ORDER BY purchased ASC, ordinal ASC")
    fun getAll(uid: String): Flow<List<ShoppingListEntry>>

    @Query("SELECT * FROM shoppingListEntry WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<ShoppingListEntry?>

    @Query("SELECT * FROM shoppingListEntry WHERE purchaserUid = :uid " +
            "AND ingredientId = :ingredientId AND unit = :unit " +
            "AND purchased = 0 ORDER BY purchased ASC, ordinal ASC")
    fun getByIngredientAndUidAndUnitUnpurchased(
        ingredientId: Long, uid: String, unit: IngredientUnit
    ): Flow<ShoppingListEntry?>

    @Insert
    suspend fun insert(shoppingListEntry: ShoppingListEntry): Long

    @Update
    suspend fun update(shoppingListEntry: ShoppingListEntry)

    @Delete
    suspend fun delete(shoppingListEntry: ShoppingListEntry)

    // Deletes all shopping list entries of a particular user.
    @Query("DELETE FROM shoppingListEntry WHERE purchaserUid = :uid")
    suspend fun deleteAllForUser(uid: String)

    // Deletes only the purchased items
    @Query("DELETE FROM shoppingListEntry WHERE purchaserUid = :uid AND purchased = 1")
    suspend fun deletePurchasedForUser(uid: String)

    @Query("SELECT MAX(ordinal) FROM shoppingListEntry WHERE purchaserUid = :uid")
    suspend fun getMaxOrdinalForUser(uid: String): Int

    // Sets the ordinal, this represents the position of an entry in the shopping list.
    // Called when reordering items.
    @Query("UPDATE shoppinglistentry SET ordinal = :ordinal WHERE id = :id")
    suspend fun setOrdinal(id: Long, ordinal: Int)

}