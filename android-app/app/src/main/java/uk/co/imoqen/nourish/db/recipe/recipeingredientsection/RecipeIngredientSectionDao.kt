package uk.co.imoqen.nourish.db.recipe.recipeingredientsection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientSectionDao {
    // insert one
    @Insert
    suspend fun insert(recipeIngredientSection: RecipeIngredientSection): Long

    @Query("SELECT * FROM RecipeIngredientSection WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<RecipeIngredientSection?>

    //@Query("DELETE FROM RecipeIngredientSection")

}