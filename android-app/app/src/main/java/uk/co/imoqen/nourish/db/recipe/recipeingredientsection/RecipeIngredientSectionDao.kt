package uk.co.imoqen.nourish.db.recipe.recipeingredientsection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientSectionDao {
    @Query("SELECT * FROM RecipeIngredientSection")
    fun getAll(): Flow<List<RecipeIngredientSection>> // flow is better than normal list for things which should update. as cant access db on main thread

    // insert one
    @Insert
    suspend fun insert(recipeIngredientSection: RecipeIngredientSection): Long

    @Query("SELECT * FROM RecipeIngredientSection WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<RecipeIngredientSection?>

}