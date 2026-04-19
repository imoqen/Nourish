package uk.co.imoqen.nourish.db.recipe

import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipenutrition.RecipeNutrition
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQuery
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStep
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTag
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.network.response.LlmRecipe

/**
 * The RecipeRepository, providing a contract of methods to interact with the DAO regarding
 * Recipe-related logic.
 * These are all bundled together into this interface to reduce the number of dependencies on
 * ViewModels, providing a simple, single repository for all Recipe-related CRUD operations.
 *
 * @author Imogen Pemberton, 2026
 */
interface RecipeRepository {

    // Recipe Logic:
    fun getAllRecipes(uid: String): Flow<List<Recipe>>

    fun getRecipeById(recipeId: Long): Flow<Recipe?>
    suspend fun insertRecipe(recipe: Recipe): Long
    fun getRecipeWithRecipeIngredients(recipeId: Long): Flow<RecipeWithRecipeIngredients>
    fun getRecipeWithRecipeTags(recipeId: Long): Flow<RecipeWithRecipeTags>
    fun getRecipeWithRecipeSteps(recipeId: Long): Flow<RecipeWithRecipeSteps>

    // Recipe Type Logic:
    fun getAllRecipeTypes(): Flow<List<RecipeType>>
    fun getRecipeTypeById(id: Long): Flow<RecipeType?>
    suspend fun insertRecipeType(recipeType: RecipeType): Long
    suspend fun getIdByTitle(title: String): Long?

    // Recipe Ingredient Logic:
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long

    // Recipe Ingredient Section Logic:
    suspend fun insertRecipeIngredientSection(recipeIngredientSection: RecipeIngredientSection): Long
    fun getRecipeSectionById(id: Long): Flow<RecipeIngredientSection?>

    // Recipe Step Logic:
    suspend fun insertRecipeStep(recipeStep: RecipeStep): Long
    suspend fun updateStepCompleteness(recipeStepId: Long, complete: Boolean)

    // Recipe Tag Logic:
    suspend fun insertRecipeTag(recipeTag: RecipeTag): Long

    // Recipe Nutrition Logic:
    suspend fun insertRecipeNutrition(recipeNutrition: RecipeNutrition)
    fun getNutritionForRecipe(recipeId: Long): Flow<RecipeNutrition?>
    suspend fun deleteNutritionForRecipeId(recipeId: Long)

    // Recipe Query Logic:
    suspend fun insertRecipeQuery(recipeQuery: RecipeQuery): Long
    suspend fun updateRecipeQuery(recipeQuery: RecipeQuery)
    fun getAllQueriesForRecipe(recipeId: Long): Flow<List<RecipeQuery?>>

    fun getRecipeQueryById(recipeQueryId: Long): Flow<RecipeQuery?>

    // Creates a recipe in the database and its corresponding entities
    suspend fun createRecipe(uid: String, llmRecipe: LlmRecipe, shouldNotScaleIngredients: Boolean, recipeId: Long = -1, imagePath: String = "", llmGenerated: Boolean = true)

    suspend fun replaceRecipe(currentRecipe: Recipe, newRecipe: LlmRecipe, updatedImagePath: String, llmGenerated: Boolean = true)

    suspend fun scaleRecipe(recipe: Recipe, currentServingSize: Int, increase: Boolean)

    suspend fun updateRecipe(recipe: Recipe)

    suspend fun deleteRecipe(recipe: Recipe)
}