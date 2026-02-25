package uk.co.imoqen.nourish.db.recipe.recipeingredient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipeIngredient")
    fun getAll(): Flow<List<RecipeIngredient>> // flow is better than normal list for things which should update. as cant access db on main thread

    // insert one
    @Insert
    suspend fun insert(recipeIngredient: RecipeIngredient): Long
}