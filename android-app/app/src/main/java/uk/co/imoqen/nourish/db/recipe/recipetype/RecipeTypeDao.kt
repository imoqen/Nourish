package uk.co.imoqen.nourish.db.recipe.recipetype

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.ingredient.Ingredient

@Dao
interface RecipeTypeDao {
    @Query("SELECT * FROM recipeType")
    fun getAll(): Flow<List<RecipeType>> // flow is better than normal list for things which should update. as cant access db on main thread

    // insert one
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(recipeType: RecipeType): Long

    @Query("SELECT * FROM recipeType WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<RecipeType?>

    // is not case sensitive via COLLATE NOCASE
    @Query("SELECT id FROM recipeType WHERE title = :title LIMIT 1 COLLATE NOCASE")
    suspend fun getIdByTitle(title: String): Long?
}