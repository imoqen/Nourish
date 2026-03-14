package uk.co.imoqen.nourish.db.shoppinglistentry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe

@Dao
interface ShoppingListEntryDao {

    // gets all for a specific user, no instance where would need to get for every user.
    @Query("SELECT * FROM shoppingListEntry WHERE purchaserUid = :uid")
    fun getAll(uid: String): Flow<List<ShoppingListEntry>>

    @Query("SELECT * FROM shoppingListEntry WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<ShoppingListEntry?>

    @Query("SELECT * FROM shoppingListEntry WHERE purchaserUid = :uid AND ingredientId = :ingredientId AND unit = :unit AND purchased = 0")
    fun getByIngredientAndUidAndUnitUnpurchased(ingredientId: Long, uid: String, unit: IngredientUnit): Flow<ShoppingListEntry?>

    @Insert
    suspend fun insert(shoppingListEntry: ShoppingListEntry): Long

    @Update
    suspend fun update(shoppingListEntry: ShoppingListEntry)

    @Delete
    suspend fun delete(shoppingListEntry: ShoppingListEntry)

    @Query("SELECT MAX(ordinal) FROM shoppingListEntry WHERE purchaserUid = :uid")
    suspend fun getMaxOrdinalForUser(uid: String): Int

    // sets the ordinal, this represents the position of an entry in the shopping list. occurs when reordering items.
    @Query("UPDATE shoppinglistentry SET ordinal = :ordinal WHERE id = :id")
    suspend fun setOrdinal(id: Long, ordinal: Int)

}