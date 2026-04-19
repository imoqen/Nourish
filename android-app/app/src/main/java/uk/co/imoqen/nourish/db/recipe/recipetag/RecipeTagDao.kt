package uk.co.imoqen.nourish.db.recipe.recipetag;

import androidx.room.Dao
import androidx.room.Insert

/**
 * The RecipeTagDao, holding logic to perform CRUD operations on the database relating to RecipeTag.
 *
 * @author Imogen Pemberton, 2026
 */

@Dao
interface RecipeTagDao {
    // insert one
    @Insert
    suspend fun insert(recipeTag: RecipeTag): Long
}