package uk.co.imoqen.nourish.db.recipe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients

// keeping recipes stuff together for simplicity

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe WHERE ownerUid = :uid")
    fun getAll(uid: String): Flow<List<Recipe>> // flow is better than normal list for things which should update. as cant access db on main thread

    /*@Insert
    fun insertAllRecipes(vararg recipes: Recipe)

    @Delete
    fun deleteRecipe(recipe: Recipe)*/

    // insert one
    @Insert
    suspend fun insert(recipe: Recipe): Long

    /*@Transaction
    @Query("SELECT * FROM Recipe WHERE id = :recipeId")
    fun getRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients>*/

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