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

    var lastCalled: String = "" // to tell if a call was last made to a certain method, in the test

    override fun getAllRecipes(uid: String): Flow<List<Recipe>> {
        if (uid == "has_recipe_with_invalid_recipeTypeId") {
            return flowOf(
                listOf(
                    TestData.recipe_withInvalidRecipeTypeId
                )
            )
        }
        return flowOf(
            listOf(
                TestData.recipe_salad,
                TestData.recipe_withWaterIngredients
            )
        )
    }

    override fun getRecipeById(recipeId: Long): Flow<Recipe?> {
        if (recipeId == -1L) {
            return flowOf(null)
        }
        if (recipeId == 2L) {
            return flowOf(TestData.recipe_salad.copy(title = "should throw exception"))
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
        return 0
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
            TestData.recipeWithRecipeIngredient_salad
        )
    }

    override fun getRecipeWithRecipeTags(recipeId: Long): Flow<RecipeWithRecipeTags> {
        if (recipeId == 10L) {
            return flowOf(
                TestData.recipeWithRecipeTags_empty
            )
        }
        return flowOf(
            TestData.recipeWithRecipeTags_salad
        )
    }

    override fun getRecipeWithRecipeSteps(recipeId: Long): Flow<RecipeWithRecipeSteps> {
        return flowOf(
            TestData.recipeWithRecipeSteps_salad
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
        return 0
    }

    override suspend fun getIdByTitle(title: String): Long? {
        return 0
    }

    override suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long {
        return 0
    }

    override suspend fun insertRecipeIngredientSection(recipeIngredientSection: RecipeIngredientSection): Long {
        return 0
    }

    override fun getRecipeSectionById(id: Long): Flow<RecipeIngredientSection?> {
        if (id == 0L) {
            return flowOf(TestData.recipeIngredientSection_salad)
        }
        if (id == 1L) {
            return flowOf(TestData.recipeIngredientSection_salad)
        }
        return flowOf(TestData.recipeIngredientSection_salad)
    }

    override suspend fun insertRecipeStep(recipeStep: RecipeStep): Long {
        return 0
    }

    override suspend fun updateStepCompleteness(
        recipeStepId: Long,
        complete: Boolean
    ) {
        if (recipeStepId == -1L) {
            throw Exception() // to test how it handles exceptions
        }
        // else, do nothing to simulate success
    }

    override suspend fun insertRecipeTag(recipeTag: RecipeTag): Long {
        return 0
    }

    override suspend fun insertRecipeNutrition(recipeNutrition: RecipeNutrition) {
        lastCalled = "insertRecipeNutrition"
    }

    override fun getNutritionForRecipe(recipeId: Long): Flow<RecipeNutrition?> {
        return flowOf(TestData.recipeNutrition_salad)
    }

    override suspend fun deleteNutritionForRecipeId(recipeId: Long) {
    }

    override suspend fun insertRecipeQuery(recipeQuery: RecipeQuery): Long {
        if (recipeQuery.userQuestion == "trigger error") {
            return -1 // for testing how errors are handled in makeLlmApiCall
        }
        // else, run as normal:
        return 0
    }

    override suspend fun updateRecipeQuery(recipeQuery: RecipeQuery) {
        lastCalled = "updateRecipeQuery"
    }

    override fun getAllQueriesForRecipe(recipeId: Long): Flow<List<RecipeQuery?>> {
        return flowOf(TestData.recipeQueriesList_salad)
    }

    override fun getRecipeQueryById(recipeQueryId: Long): Flow<RecipeQuery?> {
        return flowOf(
            TestData.recipeQuery_salad_healthiness
        )
    }

    override suspend fun createRecipe(
        uid: String,
        llmRecipe: LlmRecipe,
        shouldNotScaleIngredients: Boolean,
        recipeId: Long,
        imagePath: String,
        llmGenerated: Boolean
    ) {
        lastCalled = "createRecipe"
    }

    override suspend fun replaceRecipe(
        currentRecipe: Recipe,
        newRecipe: LlmRecipe,
        updatedImagePath: String,
        llmGenerated: Boolean
    ) {
        lastCalled = "replaceRecipe"
    }

    override suspend fun scaleRecipe(
        recipe: Recipe,
        currentServingSize: Int,
        increase: Boolean
    ) {
        lastCalled = "scaleRecipe"
    }

    override suspend fun updateRecipe(recipe: Recipe) {
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        if (recipe.title == "should throw exception") {
            throw Exception()
        }
        lastCalled = "deleteRecipe"
    }

}