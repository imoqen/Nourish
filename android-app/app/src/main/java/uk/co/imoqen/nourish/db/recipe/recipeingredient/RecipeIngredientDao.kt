package uk.co.imoqen.nourish.db.recipe.recipeingredient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    // insert one
    @Insert
    suspend fun insert(recipeIngredient: RecipeIngredient): Long
}