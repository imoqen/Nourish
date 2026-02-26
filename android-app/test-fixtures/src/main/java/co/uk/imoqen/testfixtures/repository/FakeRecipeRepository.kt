package co.uk.imoqen.testfixtures.repository

import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeSteps
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeTags
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStep
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTag
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.network.response.LlmRecipe

class FakeRecipeRepository(): RecipeRepository {
    override fun getAllRecipes(uid: String): Flow<List<Recipe>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        TODO("Not yet implemented")
    }

    override fun getRecipeWithRecipeIngredients(recipeId: Long): Flow<RecipeWithRecipeIngredients> {
        TODO("Not yet implemented")
    }

    override fun getRecipeWithRecipeTypes(recipeId: Long): Flow<RecipeWithRecipeTags> {
        TODO("Not yet implemented")
    }

    override fun getRecipeWithRecipeSteps(recipeId: Long): Flow<RecipeWithRecipeSteps> {
        TODO("Not yet implemented")
    }

    override fun getAllRecipeTypes(): Flow<List<RecipeType>> {
        TODO("Not yet implemented")
    }

    override fun getRecipeTypeById(id: Long): Flow<RecipeType?> {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecipeType(recipeType: RecipeType): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getIdByTitle(title: String): Long? {
        TODO("Not yet implemented")
    }

    override fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long {
        TODO("Not yet implemented")
    }

    override fun getAllRecipeIngredientSections(): Flow<List<RecipeIngredientSection>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecipeIngredientSection(recipeIngredientSection: RecipeIngredientSection): Long {
        TODO("Not yet implemented")
    }

    override fun getRecipeSectionById(id: Long): Flow<RecipeIngredientSection?> {
        TODO("Not yet implemented")
    }

    override fun getAllRecipeSteps(): Flow<List<RecipeStep>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecipeStep(recipeStep: RecipeStep): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateStepCompleteness(
        recipeStepId: Long,
        complete: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllRecipeTags(): Flow<List<RecipeTag>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecipeTag(recipeTag: RecipeTag): Long {
        TODO("Not yet implemented")
    }

    override suspend fun createRecipe(
        uid: String,
        llmRecipe: LlmRecipe
    ) {
        TODO("Not yet implemented")
    }
}