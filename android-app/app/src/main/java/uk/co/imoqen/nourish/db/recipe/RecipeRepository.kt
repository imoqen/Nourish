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

interface RecipeRepository {

    // RECIPE LOGIC:
    fun getAllRecipes(uid: String): Flow<List<Recipe>>

    fun getRecipeById(recipeId: Long): Flow<Recipe?>
    suspend fun insertRecipe(recipe: Recipe): Long
    fun getRecipeWithRecipeIngredients(recipeId: Long): Flow<RecipeWithRecipeIngredients>
    fun getRecipeWithRecipeTypes(recipeId: Long): Flow<RecipeWithRecipeTags>
    fun getRecipeWithRecipeSteps(recipeId: Long): Flow<RecipeWithRecipeSteps>

    // RECIPE TYPE LOGIC:
    fun getAllRecipeTypes(): Flow<List<RecipeType>>
    fun getRecipeTypeById(id: Long): Flow<RecipeType?>
    suspend fun insertRecipeType(recipeType: RecipeType): Long
    suspend fun getIdByTitle(title: String): Long?

    // RECIPE INGREDIENT LOGIC:
    fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>>
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long

    // RECIPE INGREDIENT SECTION LOGIC:
    fun getAllRecipeIngredientSections(): Flow<List<RecipeIngredientSection>>
    suspend fun insertRecipeIngredientSection(recipeIngredientSection: RecipeIngredientSection): Long
    fun getRecipeSectionById(id: Long): Flow<RecipeIngredientSection?>

    // RECIPE STEP LOGIC:
    fun getAllRecipeSteps(): Flow<List<RecipeStep>>
    suspend fun insertRecipeStep(recipeStep: RecipeStep): Long
    suspend fun updateStepCompleteness(recipeStepId: Long, complete: Boolean)

    // RECIPE TAG LOGIC:
    fun getAllRecipeTags(): Flow<List<RecipeTag>>
    suspend fun insertRecipeTag(recipeTag: RecipeTag): Long

    // RECIPE NUTRITION LOGIC:
    suspend fun insertRecipeNutrition(recipeNutrition: RecipeNutrition)
    fun getNutritionForRecipe(recipeId: Long): Flow<RecipeNutrition?>
    suspend fun deleteNutritionForRecipeId(recipeId: Long)

    // RECIPE QUERY LOGIC:
    suspend fun insertRecipeQuery(recipeQuery: RecipeQuery): Long
    suspend fun updateRecipeQuery(recipeQuery: RecipeQuery)
    fun getAllQueriesForRecipe(recipeId: Long): Flow<List<RecipeQuery?>>

    fun getRecipeQueryById(recipeQueryId: Long): Flow<RecipeQuery?>

    /**
     * Creates a recipe in the database and its corresponding entities
     */
    suspend fun createRecipe(uid: String, llmRecipe: LlmRecipe, alteration: Boolean, recipeId: Long = -1, imagePath: String = "", llmGenerated: Boolean = true)

    suspend fun replaceRecipe(currentRecipe: Recipe, newRecipe: LlmRecipe)

    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient)

    suspend fun scaleRecipe(recipe: Recipe, currentServingSize: Int, increase: Boolean)

    suspend fun updateRecipe(recipe: Recipe)

}