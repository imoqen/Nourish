package uk.co.imoqen.nourish.db.shoppinglistentry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.Recipe

@Dao
interface ShoppingListEntryDao {

    // gets all for a specific user, no instance where would need to get for every user.
    @Query("SELECT * FROM shoppingListEntry WHERE purchaserUid = :uid")
    fun getAll(uid: String): Flow<List<Recipe>>

    @Query("SELECT * FROM shoppingListEntry WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<Recipe?>

    @Insert
    suspend fun insert(shoppingListEntry: ShoppingListEntry): Long

    @Update
    suspend fun update(shoppingListEntry: ShoppingListEntry)

    @Delete
    suspend fun delete(shoppingListEntry: ShoppingListEntry)

}