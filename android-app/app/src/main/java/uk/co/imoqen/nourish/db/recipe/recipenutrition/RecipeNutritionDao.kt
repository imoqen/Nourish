package uk.co.imoqen.nourish.db.recipe.recipenutrition

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeNutritionDao {
    // insert one
    @Insert
    suspend fun insert(recipeNutrition: RecipeNutrition): Long

    @Query("SELECT * FROM RecipeNutrition WHERE recipeId = :recipeId LIMIT 1")
    fun getByRecipeId(recipeId: Long): Flow<RecipeNutrition?>

    @Query("DELETE FROM RecipeNutrition WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)
}