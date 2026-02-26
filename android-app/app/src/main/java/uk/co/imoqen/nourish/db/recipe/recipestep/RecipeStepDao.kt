package uk.co.imoqen.nourish.db.recipe.recipestep

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeStepDao {
    @Query("SELECT * FROM RecipeStep")
    fun getAll(): Flow<List<RecipeStep>> // flow is better than normal list for things which should update. as cant access db on main thread

    // insert one
    @Insert
    suspend fun insert(recipeStep: RecipeStep): Long

    @Query("SELECT * FROM RecipeStep WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<RecipeStep?>

    @Query("UPDATE RecipeStep SET complete = :complete WHERE id = :id")
    suspend fun updateStepCompleteness(id: Long, complete: Boolean)
}