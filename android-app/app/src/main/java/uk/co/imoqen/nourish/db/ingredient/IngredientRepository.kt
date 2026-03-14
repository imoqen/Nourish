package uk.co.imoqen.nourish.db.ingredient

import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAll(): Flow<List<Ingredient>>

    fun getById(id: Long): Flow<Ingredient?>

    suspend fun insert(ingredient: Ingredient): Long

    suspend fun getIdBySingular(singular: String): Long?
    suspend fun getIdByPlural(plural: String): Long?
}