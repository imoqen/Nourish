package uk.co.imoqen.nourish.db.tag

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * The TagDao, holding logic to perform CRUD operations on the database relating to Tag.
 *
 * @author Imogen Pemberton, 2026
 */

@Dao
interface TagDao {
    @Query("SELECT * FROM Tag WHERE authorId = :uid")
    fun getAllForUser(uid: String): Flow<List<Tag>>

    // Inserting one will be ignored if a Tag already exists which has the authorId (uid) and
    // displayName combination. due to the index defined in Tag
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT id FROM tag WHERE identifier = :identifier AND authorId = :uid LIMIT 1")
    suspend fun getIdByIdentifier(identifier: String, uid: String): Long?

    @Query("SELECT * FROM tag WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<Tag?>
}