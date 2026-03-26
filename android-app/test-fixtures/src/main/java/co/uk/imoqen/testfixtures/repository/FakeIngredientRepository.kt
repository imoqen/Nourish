package co.uk.imoqen.testfixtures.repository

import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository

class FakeIngredientRepository(): IngredientRepository {
    override fun getAll(): Flow<List<Ingredient>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): Flow<Ingredient?> {
        TODO("Not yet implemented")
    }

    override suspend fun insert(ingredient: Ingredient): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getIdBySingular(singular: String): Long? {
        TODO("Not yet implemented")
    }

    override suspend fun getIdByPlural(plural: String): Long? {
        TODO("Not yet implemented")
    }

    override suspend fun update(ingredient: Ingredient) {
        TODO("Not yet implemented")
    }

}