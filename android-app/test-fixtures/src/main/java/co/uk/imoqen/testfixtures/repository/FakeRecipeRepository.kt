package co.uk.imoqen.testfixtures.repository

import co.uk.imoqen.testfixtures.common.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeSteps
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeTags
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipenutrition.RecipeNutrition
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQuery
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStep
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTag
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.network.response.LlmRecipe

class FakeRecipeRepository(): RecipeRepository {
    override fun getAllRecipes(uid: String): Flow<List<Recipe>> {
        TODO("Not yet implemented")
    }

    override fun getRecipeById(recipeId: Long): Flow<Recipe?> {
        if (recipeId == -1L) {
            return flowOf(null)
        }
        if (recipeId == 50L) {
            return flowOf(
                TestData.recipe_withInvalidRecipeTypeId
            )
        }
        return flowOf(
            TestData.recipe_salad
        )
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        TODO("Not yet implemented")
    }

    override fun getRecipeWithRecipeIngredients(recipeId: Long): Flow<RecipeWithRecipeIngredients> {
        if (recipeId == -1L) { // for invalid
            return flowOf(
                RecipeWithRecipeIngredients(
                    recipe = TestData.recipe_salad,
                    ingredients = listOf(RecipeIngredient(
                        id = -1, // invalid id
                        recipeId = 0,
                        ingredientId = -1, // invalid ingredient id, triggers null return
                        ingredientUnit = IngredientUnit.G,
                        amount = 500.0,
                        sectionId = 0
                    ))
                )
            )
        }
        else if (recipeId == 3L) { // water:
            return flowOf(
                RecipeWithRecipeIngredients(
                    recipe = TestData.recipe_withWaterIngredients,
                    ingredients = listOf(RecipeIngredient(
                        id = 3, // water recipe id
                        recipeId = 0,
                        ingredientId = 3, // water ingredient id
                        ingredientUnit = IngredientUnit.ML,
                        amount = 100.0,
                        sectionId = 0
                    ))
                )
            )
        }
        else if (recipeId == 5L) { // duplicate ingredients:
            return flowOf(
                RecipeWithRecipeIngredients(
                    recipe = TestData.recipe_withWaterIngredients,
                    ingredients = listOf(
                        RecipeIngredient(
                            id = 5,
                            recipeId = 0,
                            ingredientId = 5, // dupe ingredient id
                            ingredientUnit = IngredientUnit.ML,
                            amount = 100.0,
                            sectionId = 0
                        ),
                        RecipeIngredient(
                            id = 5,
                            recipeId = 0,
                            ingredientId = 5, // dupe ingredient id
                            ingredientUnit = IngredientUnit.ML,
                            amount = 100.0,
                            sectionId = 0
                        )
                    )
                )
            )
        }
        else if (recipeId == 7L) {
            return flowOf(
                RecipeWithRecipeIngredients(
                    recipe = TestData.recipe_salad,
                    ingredients = listOf(RecipeIngredient(
                        id = 7L,
                        recipeId = 0,
                        ingredientId = 7,
                        ingredientUnit = IngredientUnit.HEAD, // gets converted to UNIT when adding to shopping list
                        amount = 500.0,
                        sectionId = 0
                    ))
                )
            )
        }
        else if (recipeId == 8L) {
            return flowOf(
                RecipeWithRecipeIngredients(
                    recipe = TestData.recipe_salad,
                    ingredients = listOf(RecipeIngredient(
                        id = 8L,
                        recipeId = 0,
                        ingredientId = 8,
                        ingredientUnit = IngredientUnit.SINGLE, // to remove from the shopping list
                        amount = 0.0,
                        sectionId = 0
                    ))
                )
            )
        }
        return flowOf(
            RecipeWithRecipeIngredients(
                recipe = TestData.recipe_salad,
                ingredients = listOf(RecipeIngredient(
                    id = 0,
                    recipeId = 0,
                    ingredientId = 0,
                    ingredientUnit = IngredientUnit.G,
                    amount = 500.0,
                    sectionId = 0
                ))
            )
        )
    }

    override fun getRecipeWithRecipeTags(recipeId: Long): Flow<RecipeWithRecipeTags> {
        return flowOf(
            RecipeWithRecipeTags(
                recipe = TestData.recipe_salad,
                recipeTags = listOf(RecipeTag(0, 0, 0))
            )
        )
    }

    override fun getRecipeWithRecipeSteps(recipeId: Long): Flow<RecipeWithRecipeSteps> {
        return flowOf(
            RecipeWithRecipeSteps(
                recipe = TestData.recipe_salad,
                recipeSteps = listOf(RecipeStep(
                    id = 0,
                    recipeId = 0,
                    index = 0,
                    instruction = "Wash the cherries",
                    complete = false
                ))
            )
        )
    }

    override fun getAllRecipeTypes(): Flow<List<RecipeType>> {
        return flowOf( TestData.recipeTypes )
    }

    override fun getRecipeTypeById(id: Long): Flow<RecipeType?> {
        if (id == -1L) { // for testing null recipe type return value
            return flowOf(null)
        }
        return flowOf(TestData.recipeType_dessert)
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

    override suspend fun insertRecipeNutrition(recipeNutrition: RecipeNutrition) {
        TODO("Not yet implemented")
    }

    override fun getNutritionForRecipe(recipeId: Long): Flow<RecipeNutrition?> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteNutritionForRecipeId(recipeId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecipeQuery(recipeQuery: RecipeQuery): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateRecipeQuery(recipeQuery: RecipeQuery) {
        TODO("Not yet implemented")
    }

    override fun getAllQueriesForRecipe(recipeId: Long): Flow<List<RecipeQuery?>> {
        TODO("Not yet implemented")
    }

    override fun getRecipeQueryById(recipeQueryId: Long): Flow<RecipeQuery?> {
        TODO("Not yet implemented")
    }

    override suspend fun createRecipe(
        uid: String,
        llmRecipe: LlmRecipe,
        shouldNotScaleIngredients: Boolean,
        recipeId: Long,
        imagePath: String,
        llmGenerated: Boolean
    ) {
        // simply do nothing; no side effects
    }

    override suspend fun replaceRecipe(
        currentRecipe: Recipe,
        newRecipe: LlmRecipe,
        updatedImagePath: String,
        llmGenerated: Boolean
    ) {
        // simply do nothing; no side effects
    }

    override suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient) {
        TODO("Not yet implemented")
    }

    override suspend fun scaleRecipe(
        recipe: Recipe,
        currentServingSize: Int,
        increase: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        TODO("Not yet implemented")
    }

}