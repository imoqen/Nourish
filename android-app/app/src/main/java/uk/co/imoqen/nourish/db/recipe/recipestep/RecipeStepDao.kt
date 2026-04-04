package uk.co.imoqen.nourish.db.recipe.recipestep

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeStepDao {
    // insert one
    @Insert
    suspend fun insert(recipeStep: RecipeStep): Long

    @Query("UPDATE RecipeStep SET complete = :complete WHERE id = :id")
    suspend fun updateStepCompleteness(id: Long, complete: Boolean)
}