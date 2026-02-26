package uk.co.imoqen.nourish.db.recipe

import android.util.Log
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredientDao
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSectionDao
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStep
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStepDao
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTag
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTagDao
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeTypeDao
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository
import uk.co.imoqen.nourish.network.response.LlmRecipe
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val db: NourishDatabase,

    // internal recipe types, therefore must use Dao as all use this shared Repository:
    private val recipeDao: RecipeDao,
    private val recipeTypeDao: RecipeTypeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val recipeIngredientSectionDao: RecipeIngredientSectionDao,
    private val recipeTagDao: RecipeTagDao,
    private val recipeStepDao: RecipeStepDao,

    // external therefore use Repositories:
    private val ingredientRepository: IngredientRepository,
    private val tagRepository: TagRepository,
){
    // RECIPE LOGIC:
    // get all recipes: (not suspend)
    fun getAllRecipes(uid: String): Flow<List<Recipe>> = recipeDao.getAll(uid)
    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insert(recipe)
    //fun getRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients> = recipeDao.getRecipeWithIngredients(recipeId)
    fun getRecipeWithRecipeIngredients(recipeId: Long): Flow<RecipeWithRecipeIngredients> = recipeDao.getRecipeWithRecipeIngredients(recipeId)
    fun getRecipeWithRecipeTypes(recipeId: Long): Flow<RecipeWithRecipeTags> = recipeDao.getRecipeWithRecipeTags(recipeId)
    fun getRecipeWithRecipeSteps(recipeId: Long): Flow<RecipeWithRecipeSteps> = recipeDao.getRecipeWithRecipeSteps(recipeId)



    // RECIPE TYPE LOGIC:
    fun getAllRecipeTypes(): Flow<List<RecipeType>> = recipeTypeDao.getAll()
    fun getRecipeTypeById(id: Long): Flow<RecipeType?> = recipeTypeDao.getById(id)
    suspend fun insertRecipeType(recipeType: RecipeType): Long = recipeTypeDao.insert(recipeType)
    suspend fun getIdByTitle(title: String): Long? = recipeTypeDao.getIdByTitle(title)


    // RECIPE INGREDIENT LOGIC:
    fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>> = recipeIngredientDao.getAll()
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long = recipeIngredientDao.insert(recipeIngredient)

    // RECIPE INGREDIENT SECTION LOGIC:
    fun getAllRecipeIngredientSections(): Flow<List<RecipeIngredientSection>> = recipeIngredientSectionDao.getAll()
    suspend fun insertRecipeIngredientSection(recipeIngredientSection: RecipeIngredientSection): Long = recipeIngredientSectionDao.insert(recipeIngredientSection)
    fun getRecipeSectionById(id: Long): Flow<RecipeIngredientSection?> = recipeIngredientSectionDao.getById(id)

    // RECIPE STEP LOGIC:
    fun getAllRecipeSteps(): Flow<List<RecipeStep>> = recipeStepDao.getAll()
    suspend fun insertRecipeStep(recipeStep: RecipeStep): Long = recipeStepDao.insert(recipeStep)
    suspend fun updateStepCompleteness(recipeStepId: Long, complete: Boolean) = recipeStepDao.updateStepCompleteness(recipeStepId, complete)

    // RECIPE TAG LOGIC:
    fun getAllRecipeTags(): Flow<List<RecipeTag>> = recipeTagDao.getAll()
    suspend fun insertRecipeTag(recipeTag: RecipeTag): Long = recipeTagDao.insert(recipeTag)

    /**
     * Creates a recipe in the database and its corresponding entitites
     */
    suspend fun createRecipe(
        uid: String,
        llmRecipe: LlmRecipe
    ) {
        // if throws, it is handled accordingly where it is called.
        db.withTransaction {
            // 1. Create or get the recipe type
            var recipeTypeId = getIdByTitle(llmRecipe.mealType)
            if (recipeTypeId == null) {
                recipeTypeId = insertRecipeType(RecipeType(title = llmRecipe.mealType))
            }

            // 2. Create the recipe and add it to the database
            val recipe = Recipe(
                authorUid = uid, title = llmRecipe.title, numberOfServings = llmRecipe.servings.toInt(),
                colour = "todo", generated = true, image = null, archived = false,
                storageInstructions = llmRecipe.storage, recipeTypeId = recipeTypeId,
            )
            val recipeId = recipeDao.insert(recipe)

            // 3. Search for each Ingredient and create it if it doesn't already exist:
            llmRecipe.sectionIngredients.forEach{sectionIngredient ->
                val section = sectionIngredient.section
                val ingredients = sectionIngredient.ingredients
                // add the section:
                val sectionId = recipeIngredientSectionDao.insert(RecipeIngredientSection(title = section))
                // for each Ingredient in the section:
                ingredients.forEach{ingredient ->
                    val singular = ingredient.ingredientSingular
                    val plural = ingredient.ingredientPlural
                    val amount = ingredient.ingredientAmount
                    val unit = ingredient.ingredientAmountUnit
                    // add it to ingredient table if doesn't already exist in its singular form:
                    var ingredientId = ingredientRepository.getIdBySingular(singular)
                    if (ingredientId == null) {
                        ingredientId = ingredientRepository.insert(Ingredient(singular = singular, plural = plural))
                    }
                    // Ingredient unit: find the corresponding unit in the enum
                    val ingredientUnit = IngredientUnit.valueOf(unit)
                    // Then create a RecipeIngredient with the Recipe, Ingredient and the newly added RecipeSection:
                    recipeIngredientDao.insert(
                        RecipeIngredient(
                            recipeId = recipeId, ingredientId = ingredientId,
                            ingredientUnit = ingredientUnit, amount = amount.toDouble(),
                            sectionId = sectionId
                        )
                    )
                }
            }

            // 4. Create recipe steps
            val steps = llmRecipe.method
            steps.forEachIndexed{ i, step ->
                recipeStepDao.insert(
                    RecipeStep(
                        recipeId = recipeId,
                        index = i+1, // +1 so its human readable (i.e. step 0 -> step 1)
                        instruction = step,
                        complete = false // auto set to false as user must not have started recipe
                    )
                )
            }

            // 5. Create tags
            llmRecipe.tags.forEach { tagIdentifier ->
                var tagId = tagRepository.getIdByIdentifier(tagIdentifier)
                if (tagId == null) {
                    tagId = tagRepository.insert(
                        Tag(
                            identifier = tagIdentifier,
                        )
                    )
                }
                recipeTagDao.insert(
                    RecipeTag(recipeId = recipeId, tagId = tagId)
                )
            }
        }
    }
}