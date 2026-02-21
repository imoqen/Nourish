package uk.co.imoqen.nourish.db.recipe

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.ingredient.Ingredient

// keeping recipes stuff together for simplicity

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe")
    fun getAll(): Flow<List<Recipe>> // flow is better than normal list for things which should update. as cant access db on main thread

    /*@Insert
    fun insertAllRecipes(vararg recipes: Recipe)

    @Delete
    fun deleteRecipe(recipe: Recipe)*/

    // insert one
    @Insert
    suspend fun insert(recipe: Recipe): Long
}