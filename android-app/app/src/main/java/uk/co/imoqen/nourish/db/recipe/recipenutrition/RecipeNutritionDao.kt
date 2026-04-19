package uk.co.imoqen.nourish.db.recipe.recipenutrition

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * The RecipeNutritionDao, holding logic to perform CRUD operations on the database relating to
 * RecipeNutrition.
 *
 * @author Imogen Pemberton, 2026
 */

@Dao
interface RecipeNutritionDao {
    @Insert
    suspend fun insert(recipeNutrition: RecipeNutrition): Long

    @Query("SELECT * FROM RecipeNutrition WHERE recipeId = :recipeId LIMIT 1")
    fun getByRecipeId(recipeId: Long): Flow<RecipeNutrition?>

    @Query("DELETE FROM RecipeNutrition WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)
}