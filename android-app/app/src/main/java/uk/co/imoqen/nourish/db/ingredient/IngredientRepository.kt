package uk.co.imoqen.nourish.db.ingredient

import kotlinx.coroutines.flow.Flow

/**
 * The IngredientRepository, providing a contract of methods to interact with the DAO regarding
 * Ingredient-related logic.
 */
interface IngredientRepository {
    fun getById(id: Long): Flow<Ingredient?>
    suspend fun insert(ingredient: Ingredient): Long
    // Gets the id of an ingredient given its singular name
    suspend fun getIdBySingular(singular: String): Long?
    // Gets the id of an ingredient given its plural name
    suspend fun getIdByPlural(plural: String): Long?
    suspend fun update(ingredient: Ingredient)
}