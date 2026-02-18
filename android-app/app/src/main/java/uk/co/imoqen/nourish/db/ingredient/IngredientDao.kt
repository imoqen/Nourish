package uk.co.imoqen.nourish.db.ingredient

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredient")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert
    fun insertAllIngredients(vararg ingredients: Ingredient)

    // insert one
    @Insert
    fun insert(ingredient: Ingredient)

    @Delete
    fun deleteIngredient(ingredient: Ingredient)
}