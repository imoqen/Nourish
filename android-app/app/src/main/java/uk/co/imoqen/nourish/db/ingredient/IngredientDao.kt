package uk.co.imoqen.nourish.db.ingredient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * The IngredientDao, holding logic to perform CRUD operations on the database relating to Ingredient.
 *
 * @author Imogen Pemberton, 2026
 */
@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ingredient: Ingredient): Long

    @Query("SELECT id FROM ingredient WHERE singular = :singular LIMIT 1")
    suspend fun getIdBySingular(singular: String): Long?

    @Query("SELECT id FROM ingredient WHERE plural = :plural LIMIT 1")
    suspend fun getIdByPlural(plural: String): Long?

    @Query("SELECT * FROM ingredient WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<Ingredient?>

    @Update
    suspend fun update(ingredient: Ingredient)
}