package uk.co.imoqen.nourish.db.ingredient

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.RecipeIngredient
import javax.inject.Inject

class IngredientRepository @Inject constructor(
    private val ingredientDao: IngredientDao
){
    @Query("SELECT * FROM ingredient")
    fun getAll(): Flow<List<Ingredient>> = ingredientDao.getAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun getIdByName(name: String): Long? = ingredientDao.getIdByName(name)
}