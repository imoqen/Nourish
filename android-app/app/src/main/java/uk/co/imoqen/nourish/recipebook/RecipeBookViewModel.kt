package uk.co.imoqen.nourish.recipebook

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import javax.inject.Inject

@HiltViewModel
class RecipeBookViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authenticationManager: AuthenticationManager,
    private val apiManager: ApiManager,
    private val uiEventBus: UiEventBus,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
): ViewModel() {

    // gets all recipes for the currently logged in user. ensures that if user not logged in, empty list returned.
    fun getAllRecipes(): Flow<List<Recipe>> {
        val uid = authenticationManager.getUid()
        if (uid == null) return flowOf(emptyList())
        return recipeRepository.getAllRecipes(uid)
    }

    // not sure if this is needed
    /*fun getIngredientsForRecipe(recipeId: Long): Flow<RecipeWithIngredients> {
        return recipeRepository.getRecipeWithIngredients(recipeId)
    }*/

    // use instead
    fun getRecipeIngredientsForRecipe(recipeId: Long): Flow<RecipeWithRecipeIngredients> {
        return recipeRepository.getRecipeWithRecipeIngredients(recipeId)
    }

    fun getIngredientById(ingredientId: Long): Flow<Ingredient?> {
        return ingredientRepository.getById(ingredientId)
    }

}