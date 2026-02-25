package uk.co.imoqen.nourish.db.tag

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType

@Dao
interface TagDao {
    @Query("SELECT * FROM Tag")
    fun getAll(): Flow<List<Tag>> // flow is better than normal list for things which should update. as cant access db on main thread

    // insert one
    @Insert
    suspend fun insert(tag: Tag): Long

    @Query("SELECT id FROM tag WHERE identifier = :identifier LIMIT 1")
    suspend fun getIdByIdentifier(identifier: String): Long?

    @Query("SELECT * FROM tag WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<Tag?>
}