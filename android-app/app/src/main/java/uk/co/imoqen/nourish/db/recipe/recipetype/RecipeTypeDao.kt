package uk.co.imoqen.nourish.db.recipe.recipetype

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * The RecipeTypeDao, holding logic to perform CRUD operations on the database relating to RecipeType.
 */

@Dao
interface RecipeTypeDao {
    // Flow is better than normal list for things which should update, as the db can't be accessed
    // on the main thread. Hence, Flow is used here.
    @Query("SELECT * FROM recipeType")
    fun getAll(): Flow<List<RecipeType>>

    // insert one
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(recipeType: RecipeType): Long

    @Query("SELECT * FROM recipeType WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<RecipeType?>

    // Made not case sensitive via COLLATE NOCASE.
    @Query("SELECT id FROM recipeType WHERE title = :title LIMIT 1 COLLATE NOCASE")
    suspend fun getIdByTitle(title: String): Long?
}