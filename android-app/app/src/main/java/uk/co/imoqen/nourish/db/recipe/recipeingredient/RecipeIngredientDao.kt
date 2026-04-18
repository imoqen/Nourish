package uk.co.imoqen.nourish.db.recipe.recipeingredient

import androidx.room.Dao
import androidx.room.Insert

/**
 * The RecipeIngredientDao, holding logic to perform CRUD operations on the database relating to
 * RecipeIngredient.
 */

@Dao
interface RecipeIngredientDao {
    @Insert
    suspend fun insert(recipeIngredient: RecipeIngredient): Long
}