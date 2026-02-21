package uk.co.imoqen.nourish.db.ingredient

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.RecipeType

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredient")
    fun getAll(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ingredient: Ingredient): Long

    @Query("SELECT id FROM ingredient WHERE name = :name LIMIT 1")
    suspend fun getIdByName(name: String): Long?
}