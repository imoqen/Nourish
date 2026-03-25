package uk.co.imoqen.nourish.db.recipe

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepositoryImpl
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredient
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredientDao
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSectionDao
import uk.co.imoqen.nourish.db.recipe.recipenutrition.RecipeNutrition
import uk.co.imoqen.nourish.db.recipe.recipenutrition.RecipeNutritionDao
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQuery
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQueryDao
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStep
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStepDao
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTag
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTagDao
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeTypeDao
import uk.co.imoqen.nourish.db.tag.TagRepositoryImpl
import uk.co.imoqen.nourish.network.response.LlmRecipe
import javax.inject.Inject
import kotlin.math.round

class RecipeRepositoryImpl @Inject constructor(
    private val db: NourishDatabase,

    // internal recipe types, therefore must use Dao as all use this shared Repository:
    private val recipeDao: RecipeDao,
    private val recipeTypeDao: RecipeTypeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val recipeIngredientSectionDao: RecipeIngredientSectionDao,
    private val recipeTagDao: RecipeTagDao,
    private val recipeStepDao: RecipeStepDao,
    private val recipeNutritionDao: RecipeNutritionDao,
    private val recipeQueryDao: RecipeQueryDao,

    // external therefore use Repositories:
    private val ingredientRepository: IngredientRepositoryImpl,
    private val tagRepository: TagRepositoryImpl,
): RecipeRepository {
    // RECIPE LOGIC:
    // get all recipes: (not suspend)
    override fun getAllRecipes(uid: String): Flow<List<Recipe>> = recipeDao.getAll(uid)
    override fun getRecipeById(recipeId: Long): Flow<Recipe?> = recipeDao.getById(recipeId)
    override suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insert(recipe)
    //fun getRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients> = recipeDao.getRecipeWithIngredients(recipeId)
    override fun getRecipeWithRecipeIngredients(recipeId: Long): Flow<RecipeWithRecipeIngredients> = recipeDao.getRecipeWithRecipeIngredients(recipeId)
    override fun getRecipeWithRecipeTypes(recipeId: Long): Flow<RecipeWithRecipeTags> = recipeDao.getRecipeWithRecipeTags(recipeId)
    override fun getRecipeWithRecipeSteps(recipeId: Long): Flow<RecipeWithRecipeSteps> = recipeDao.getRecipeWithRecipeSteps(recipeId)



    // RECIPE TYPE LOGIC:
    override fun getAllRecipeTypes(): Flow<List<RecipeType>> = recipeTypeDao.getAll()
    override fun getRecipeTypeById(id: Long): Flow<RecipeType?> = recipeTypeDao.getById(id)
    override suspend fun insertRecipeType(recipeType: RecipeType): Long = recipeTypeDao.insert(recipeType)
    override suspend fun getIdByTitle(title: String): Long? = recipeTypeDao.getIdByTitle(title)


    // RECIPE INGREDIENT LOGIC:
    override fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>> = recipeIngredientDao.getAll()
    override suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient): Long = recipeIngredientDao.insert(recipeIngredient)

    // RECIPE INGREDIENT SECTION LOGIC:
    override fun getAllRecipeIngredientSections(): Flow<List<RecipeIngredientSection>> = recipeIngredientSectionDao.getAll()
    override suspend fun insertRecipeIngredientSection(recipeIngredientSection: RecipeIngredientSection): Long = recipeIngredientSectionDao.insert(recipeIngredientSection)
    override fun getRecipeSectionById(id: Long): Flow<RecipeIngredientSection?> = recipeIngredientSectionDao.getById(id)

    // RECIPE STEP LOGIC:
    override fun getAllRecipeSteps(): Flow<List<RecipeStep>> = recipeStepDao.getAll()
    override suspend fun insertRecipeStep(recipeStep: RecipeStep): Long = recipeStepDao.insert(recipeStep)
    override suspend fun updateStepCompleteness(recipeStepId: Long, complete: Boolean) = recipeStepDao.updateStepCompleteness(recipeStepId, complete)

    // RECIPE TAG LOGIC:
    override fun getAllRecipeTags(): Flow<List<RecipeTag>> = recipeTagDao.getAll()
    override suspend fun insertRecipeTag(recipeTag: RecipeTag): Long = recipeTagDao.insert(recipeTag)

    // RECIPE NUTRITION LOGIC:
    override suspend fun insertRecipeNutrition(recipeNutrition: RecipeNutrition) {
        recipeNutritionDao.insert(recipeNutrition)
    }

    override suspend fun deleteNutritionForRecipeId(recipeId: Long){
        recipeNutritionDao.deleteByRecipeId(recipeId)
    }

    override fun getNutritionForRecipe(recipeId: Long): Flow<RecipeNutrition?> {
        return recipeNutritionDao.getByRecipeId(recipeId)
    }

    // RECIPE QUERY LOGIC:
    override suspend fun insertRecipeQuery(recipeQuery: RecipeQuery): Long {
        return recipeQueryDao.insert(recipeQuery)
    }

    override suspend fun updateRecipeQuery(recipeQuery: RecipeQuery) {
        recipeQueryDao.update(recipeQuery)
    }

    override fun getAllQueriesForRecipe(recipeId: Long): Flow<List<RecipeQuery?>> {
        return recipeQueryDao.getAllByRecipeId(recipeId)
    }

    override fun getRecipeQueryById(recipeQueryId: Long): Flow<RecipeQuery?> {
        return recipeQueryDao.getById(recipeQueryId)
    }

    /**
     * Creates a recipe in the database and its corresponding entitites
     */
    override suspend fun createRecipe(
        uid: String,
        llmRecipe: LlmRecipe,
        alteration: Boolean, // purpose of alteration is to ensure that altered recipes dont get divided by serving size. only new reicpes should have this.
        recipeId: Long,
        imagePath: String, // just set to "" if none, which is fine.
        llmGenerated: Boolean // to determine if the recipe was user inputted or generated by the llm
    ) {
        // if throws, it is handled accordingly where it is called.
        db.withTransaction {
            // 1. Create or get the recipe type, only add in form where first char is capital
            // (i.e. dinner -> Dinner) to ensure no dupes
            val mealTypeFormatted = (llmRecipe.mealType).lowercase().replaceFirstChar { it.uppercase() }
            val recipeTypeId = getIdByTitle(mealTypeFormatted)
            // must already exist; llm has to choose it from given list. if wrong, throw error.
            if (recipeTypeId == null) {
                throw NullPointerException()
            }

            // 2. Create the recipe and add it to the database
            var recipe: Recipe
            if (recipeId.toInt() != -1) { // for replacing recipes, so use the original recipe id
                recipe = Recipe(
                    id = recipeId,
                    authorUid = uid,
                    title = llmRecipe.title,
                    numberOfServings = llmRecipe.servings,
                    colour = llmRecipe.colour.uppercase(),
                    generated = llmGenerated,
                    imagePath = imagePath,
                    archived = false,
                    storageInstructions = llmRecipe.storage,
                    recipeTypeId = recipeTypeId,
                    emoji = llmRecipe.emoji
                )
            } else {
                recipe = Recipe(
                    authorUid = uid,
                    title = llmRecipe.title,
                    numberOfServings = llmRecipe.servings,
                    colour = llmRecipe.colour.uppercase(),
                    generated = llmGenerated,
                    imagePath = imagePath,
                    archived = false,
                    storageInstructions = llmRecipe.storage,
                    recipeTypeId = recipeTypeId,
                    emoji = llmRecipe.emoji
                )
            }
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
                    // purpose of alteration is to ensure that altered recipes dont get divided by serving size. only new reicpes should have this.
                    val amount = if (alteration) {ingredient.ingredientAmount} else { ingredient.ingredientAmount / recipe.numberOfServings }// divide by number of servings, so ingredient is stored for 1 serving.
                    val unit = ingredient.ingredientAmountUnit
                    // add it to ingredient table if doesn't already exist in its plural form, check both values due to human error:
                    var ingredientId = ingredientRepository.getIdByPlural(plural)

                    if (ingredientId == null) {
                        // if plural doesn't match, singular is checked to ensure it doesnt exist: (against plural - as this is the field populated by the user when adding an ingredient)
                        ingredientId = ingredientRepository.getIdByPlural(singular)
                        if (ingredientId == null) {
                            // if still null, it doesnt exist so add the ingredient
                            ingredientId = ingredientRepository.insert(Ingredient(singular = singular, plural = plural))
                        }
                    }

                    // SANITISE the user inputted. user inputted always has singular = "". so check this, if it is then update it and the plural value accordingly.
                    val ingredient = ingredientRepository.getById(ingredientId).first()
                    if (ingredient != null && ingredient.singular.trim() == "") {
                        ingredientRepository.update(ingredient.copy(plural = plural, singular = singular))
                    }

                    // Ingredient unit: find the corresponding unit in the enum
                    val ingredientUnit = IngredientUnit.valueOf(unit)
                    // Then create a RecipeIngredient with the Recipe, Ingredient and the newly added RecipeSection:
                    recipeIngredientDao.insert(
                        RecipeIngredient(
                            recipeId = recipeId, ingredientId = ingredientId,
                            ingredientUnit = ingredientUnit, amount = amount,
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
                val tagId = tagRepository.getIdByIdentifier(tagIdentifier, uid) // per user identifier.
                // must already exist; llm has to choose from predefined (not make own). only users can add more.

                if (tagId != null) {
                    // just ignore it if null; dont add the tag. may be produced from llm hallucination, user doesn't need to know about this.

                    recipeTagDao.insert(
                        RecipeTag(recipeId = recipeId, tagId = tagId)
                    )
                }
            }
        }
    }

    override suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient) {
        recipeIngredientDao.update(recipeIngredient)
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.update(recipe)
    }

    // transactional so the scale is reverted if an error occurs, so the recipe scale stays correct
    override suspend fun scaleRecipe(recipe: Recipe, currentServingSize: Int, increase: Boolean) {
        db.withTransaction {
            val newServingSize = if (increase) { currentServingSize+1 } else { currentServingSize-1 }
           /* val scalingFactor = newServingSize.toFloat() / currentServingSize.toFloat()

            val recipeIngredients = getRecipeWithRecipeIngredients(recipe.id).first()

            recipeIngredients.ingredients.forEach { recipeIngredient ->
                // rounds so that it is to nearest 0.25 to prevent weird numbers
                var scaledAmount = recipeIngredient.amount*scalingFactor
                if (scaledAmount < 0.25) {
                    scaledAmount = 0.25 // cant go less than 0.25
                }
                updateRecipeIngredient(
                    recipeIngredient.copy(amount = scaledAmount)
                )
            }*/

            // update serving size:
            recipeDao.update(recipe.copy(numberOfServings = newServingSize))
        }
    }

    override suspend fun replaceRecipe(currentRecipe: Recipe, newRecipe: LlmRecipe, updatedImagePath: String, llmGenerated: Boolean) {
        // things to update:

        // DELETE ALL AND UPLOAD NEW with the currentRecipe id:
        // recipe ingredients
        // ingredient sections
        // will potentially have to add new ingredients as a result, BUT DONT DELETE THE OLD INGREDIENTS.
        // recipe tags

        // replace the recipe

        // oir just save the original recipe id, then add recipe USING that recipe id.
        val originalRecipeId = currentRecipe.id
        val originalRecipeAuthor = currentRecipe.authorUid

        // the previous nutritional values get deleted as they will no longer be accurate
        deleteNutritionForRecipeId(originalRecipeId)

        deleteRecipe(currentRecipe)
        createRecipe(originalRecipeAuthor,newRecipe, alteration = true, originalRecipeId, updatedImagePath, llmGenerated = llmGenerated)
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.delete(recipe)
    }

}