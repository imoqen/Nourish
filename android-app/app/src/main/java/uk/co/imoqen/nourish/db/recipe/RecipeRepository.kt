package uk.co.imoqen.nourish.db.recipe

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientDao
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.network.response.LlmRecipe
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val db: NourishDatabase,
    private val recipeDao: RecipeDao,
    private val recipeTypeDao: RecipeTypeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val ingredientDao: IngredientDao
){
    // RECIPE LOGIC:
    // get all recipes: (not suspend)
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAll()
    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insert(recipe)

    // RECIPE TYPE LOGIC:
    fun getAllRecipeTypes(): Flow<List<RecipeType>> = recipeTypeDao.getAll()
    suspend fun insertRecipeType(recipeType: RecipeType): Long = recipeTypeDao.insert(recipeType)
    suspend fun getIdByTitle(title: String): Long? = recipeTypeDao.getIdByTitle(title)

    // RECIPE INGREDIENT LOGIC:
    fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>> = recipeIngredientDao.getAll()
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long = recipeIngredientDao.insert(recipeIngredient)

    // RECIPE CREATION to call in viewmodel:
    suspend fun createRecipe(
        uid: String,
        llmRecipe: LlmRecipe
    ) {
        db.withTransaction {
            // recipe is send with fields:
            // a. title, instructions, num servings, recipe type
            // then units and ingredients, which are stored in RecipeIngredient
            // 1. make the recipe type defined in  a. recipe type if it doesnt already exist
            // 1. now make the Recipe with the things in a. and connect it to the currently logged in user and recipe type
            // 2. search for the ingredient in Ingredient and add it if it doesnt already exist
            // 3. now make each RecipeIngredient, connecting it to Recipe and Ingredient (b.)

            // 1. make or get the recipe type
            var recipeTypeId = getIdByTitle(llmRecipe.title)
            if (recipeTypeId == null) {
                recipeTypeId = insertRecipeType(RecipeType(title = llmRecipe.title))
            }

            val recipe = Recipe(
                authorUid = uid,
                title = llmRecipe.title,
                instructions = llmRecipe.method,
                numberOfServings = llmRecipe.servings.toInt(),
                colour = "todo",
                generated = true,
                image = null,
                archived = false,
                recipeTypeId = recipeTypeId,
            )

            // add the recipe to the db:
            val recipeId = recipeDao.insert(recipe)

            val ingredientsList = (llmRecipe.ingredients).split(",")
            val ingredientUnitsList = (llmRecipe.ingredientAmountsUnits).split(",")
            val ingredientAmountsList = (llmRecipe.ingredientAmounts).split(",")

            ingredientsList.forEachIndexed{ index, ingredient ->
                // insert it into Ingredient table if not already exist
                var ingredientId = ingredientDao.getIdByName(ingredient)
                if (ingredientId == null) {
                    ingredientId = ingredientDao.insert(Ingredient(name = ingredient))
                }

                // ingredient unit: find the corresponding unit in the enum
                val ingredientUnit = IngredientUnit.valueOf(ingredientUnitsList[index])

                // ingredient amount: find the amount
                val ingredientAmount = ingredientAmountsList[index]

                // then add to recipe ingredient with the recipe:
                recipeIngredientDao.insert(RecipeIngredient(
                    recipeId = recipeId,
                    ingredientId = ingredientId,
                    ingredientUnit = ingredientUnit,
                    amount = ingredientAmount.toInt()
                ))

            }

        }
    }
}