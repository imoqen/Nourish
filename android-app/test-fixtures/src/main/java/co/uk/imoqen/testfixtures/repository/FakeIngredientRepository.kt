package co.uk.imoqen.testfixtures.repository

import co.uk.imoqen.testfixtures.common.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository

class FakeIngredientRepository(): IngredientRepository {
    override fun getAll(): Flow<List<Ingredient>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): Flow<Ingredient?> {
        if (id == -1L) {
            return flowOf(null)
        }
        if (id == 3L) {
            return flowOf(TestData.ingredient_water)
        }
        if (id == 5L) {
            return flowOf(TestData.ingredient_lettuce)
        }
        return flowOf(TestData.ingredient_cherry)
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