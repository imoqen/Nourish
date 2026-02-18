package uk.co.imoqen.nourish.db.ingredient

import javax.inject.Inject

class IngredientRepository @Inject constructor(
    private val ingredientDao: IngredientDao
){
    //suspend fun numberOfItemsInDB() = ingredientDao.numberOfItemsInDB()
}