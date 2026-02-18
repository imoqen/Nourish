package uk.co.imoqen.nourish.db.recipe

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
){
    // get all recipes: (not suspend)
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
}