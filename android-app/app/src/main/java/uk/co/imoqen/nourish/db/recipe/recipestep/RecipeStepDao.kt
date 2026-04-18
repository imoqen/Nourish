package uk.co.imoqen.nourish.db.recipe.recipestep

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * The RecipeStepDao, holding logic to perform CRUD operations on the database relating to
 * RecipeStep.
 */

@Dao
interface RecipeStepDao {
    @Insert
    suspend fun insert(recipeStep: RecipeStep): Long

    @Query("UPDATE RecipeStep SET complete = :complete WHERE id = :id")
    suspend fun updateStepCompleteness(id: Long, complete: Boolean)
}