package uk.co.imoqen.nourish.db.recipe.recipetag;

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeTagDao {
    // insert one
    @Insert
    suspend fun insert(recipeTag: RecipeTag): Long
}