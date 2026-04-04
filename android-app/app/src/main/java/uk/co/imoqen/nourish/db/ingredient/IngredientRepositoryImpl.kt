package uk.co.imoqen.nourish.db.ingredient

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IngredientRepositoryImpl @Inject constructor(
    private val ingredientDao: IngredientDao
): IngredientRepository {
    override fun getById(id: Long): Flow<Ingredient?> = ingredientDao.getById(id)
    override suspend fun insert(ingredient: Ingredient): Long = ingredientDao.insert(ingredient)
    override suspend fun getIdBySingular(singular: String): Long? = ingredientDao.getIdBySingular(singular)
    override suspend fun getIdByPlural(plural: String): Long? = ingredientDao.getIdByPlural(plural)
    override suspend fun update(ingredient: Ingredient) = ingredientDao.update(ingredient)
}