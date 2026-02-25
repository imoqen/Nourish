package uk.co.imoqen.nourish.db.ingredient

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import javax.inject.Inject

class IngredientRepository @Inject constructor(
    private val ingredientDao: IngredientDao
){
    @Query("SELECT * FROM ingredient")
    fun getAll(): Flow<List<Ingredient>> = ingredientDao.getAll()

    @Query("SELECT * FROM ingredient WHERE id = :id")
    fun getById(id: Long): Flow<Ingredient?> = ingredientDao.getById(id)

    @Insert
    suspend fun insert(ingredient: Ingredient): Long = ingredientDao.insert(ingredient)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun getIdBySingular(singular: String): Long? = ingredientDao.getIdBySingular(singular)
}