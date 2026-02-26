package uk.co.imoqen.nourish.db.ingredient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredient")
    fun getAll(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ingredient: Ingredient): Long

    @Query("SELECT id FROM ingredient WHERE singular = :singular LIMIT 1")
    suspend fun getIdBySingular(singular: String): Long?

    @Query("SELECT * FROM ingredient WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<Ingredient?>
}