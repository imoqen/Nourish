package uk.co.imoqen.nourish.db.recipe.recipetag;

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeTagDao {
    @Query("SELECT * FROM RecipeTag")
    fun getAll(): Flow<List<RecipeTag>> // flow is better than normal list for things which should update. as cant access db on main thread

    // insert one
    @Insert
    suspend fun insert(recipeTag: RecipeTag): Long
}