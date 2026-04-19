package uk.co.imoqen.nourish.db.recipe

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients

/**
 * The RecipeDao, holding logic to perform CRUD operations on the database relating to Recipe.
 *
 * @author Imogen Pemberton, 2026
 */

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe WHERE ownerUid = :uid")
    fun getAll(uid: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipe WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<Recipe?>

    @Insert
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Transaction
    @Query("SELECT * FROM Recipe WHERE id = :recipeId")
    fun getRecipeWithRecipeIngredients(recipeId: Long): Flow<RecipeWithRecipeIngredients>

    @Transaction
    @Query("SELECT * FROM Recipe WHERE id = :recipeId")
    fun getRecipeWithRecipeTags(recipeId: Long): Flow<RecipeWithRecipeTags>

    @Transaction
    @Query("SELECT * FROM Recipe WHERE id = :recipeId")
    fun getRecipeWithRecipeSteps(recipeId: Long): Flow<RecipeWithRecipeSteps>
}