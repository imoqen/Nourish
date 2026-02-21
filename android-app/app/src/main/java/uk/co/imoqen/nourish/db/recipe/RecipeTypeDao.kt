package uk.co.imoqen.nourish.db.recipe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeTypeDao {
    @Query("SELECT * FROM recipeType")
    fun getAll(): Flow<List<RecipeType>> // flow is better than normal list for things which should update. as cant access db on main thread

    // insert one
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recipeType: RecipeType): Long

    @Query("SELECT id FROM recipeType WHERE title = :title LIMIT 1")
    suspend fun getIdByTitle(title: String): Long?
}