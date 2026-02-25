package uk.co.imoqen.nourish.db.tag
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import javax.inject.Inject

class TagRepository @Inject constructor(
    private val tagDao: TagDao
){
    @Query("SELECT * FROM Tag")
    fun getAll(): Flow<List<Tag>> = tagDao.getAll()

    @Insert
    suspend fun insert(tag: Tag): Long = tagDao.insert(tag)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun getIdByIdentifier(identifier: String): Long? = tagDao.getIdByIdentifier(identifier)
}