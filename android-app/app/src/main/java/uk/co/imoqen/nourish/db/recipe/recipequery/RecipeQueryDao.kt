package uk.co.imoqen.nourish.db.recipe.recipequery

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeQueryDao {
    @Query("SELECT * FROM RecipeQuery")
    fun getAll(): Flow<List<RecipeQuery>> // flow is better than normal list for things which should update. as cant access db on main thread

    // insert one
    @Insert
    suspend fun insert(recipeQuery: RecipeQuery): Long

    @Query("SELECT * FROM RecipeQuery WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<RecipeQuery?>

    @Query("SELECT * FROM RecipeQuery WHERE recipeId = :recipeId")
    fun getAllByRecipeId(recipeId: Long): Flow<List<RecipeQuery>>

    @Update
    suspend fun update(recipeQuery: RecipeQuery)
}