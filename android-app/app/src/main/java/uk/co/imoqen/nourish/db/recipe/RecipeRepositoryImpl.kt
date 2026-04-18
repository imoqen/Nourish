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

/**
 * The RecipeRepositoryImpl, implementing the repository for Recipe by interacting with
 * the required DAOs.
 * Doc comments are not provided for most methods, because most are trivial CRUD operations explained
 * simply by their signature. For those which are non-trivial, a doc comment is provided.
 * @param db - The database used within the app
 * the following tables are internal to Recipe, thus DAOs are used:
 * @param recipeDao - the Recipe DAO, for performing CRUD operations on Recipe
 * @param recipeTypeDao - the RecipeType DAO, for performing CRUD operations on RecipeType
 * @param recipeIngredientDao - the RecipeIngredients DAO, for performing CRUD operations on RecipeIngredient
 * @param recipeIngredientSectionDao - the RecipeIngredientSection DAO, for performing CRUD operations on RecipeIngredientSection
 * @param recipeTagDao - the RecipeTag DAO, for performing CRUD operations on RecipeTag
 * @param recipeStepDao - the RecipeStep DAO, for performing CRUD operations on RecipeStep
 * @param recipeNutritionDao - the RecipeNutrition DAO, for performing CRUD operations on RecipeNutrition
 * @param recipeQueryDao - the RecipeQuery DAO, for performing CRUD operations on RecipeQuery
 * the following tables are external to Recipe, so Repositories are used instead:
 * @param ingredientRepository - the IngredientRepository, for accessing CRUD logic on Ingredient
 * @param tagRepository - the TagRepository, for accessing CRUD logic on Tag.
 */
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
    // Recipe Logic:
    override fun getAllRecipes(uid: String): Flow<List<Recipe>> = recipeDao.getAll(uid)
    override fun getRecipeById(recipeId: Long): Flow<Recipe?> = recipeDao.getById(recipeId)
    override suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insert(recipe)
    override fun getRecipeWithRecipeIngredients(
        recipeId: Long
    ): Flow<RecipeWithRecipeIngredients> = recipeDao.getRecipeWithRecipeIngredients(recipeId)
    override fun getRecipeWithRecipeTags(
        recipeId: Long
    ): Flow<RecipeWithRecipeTags> = recipeDao.getRecipeWithRecipeTags(recipeId)
    override fun getRecipeWithRecipeSteps(
        recipeId: Long
    ): Flow<RecipeWithRecipeSteps> = recipeDao.getRecipeWithRecipeSteps(recipeId)

    // Recipe Type Logic:
    override fun getAllRecipeTypes(): Flow<List<RecipeType>> = recipeTypeDao.getAll()
    override fun getRecipeTypeById(id: Long): Flow<RecipeType?> = recipeTypeDao.getById(id)
    override suspend fun insertRecipeType(recipeType: RecipeType): Long = recipeTypeDao.insert(recipeType)
    override suspend fun getIdByTitle(title: String): Long? = recipeTypeDao.getIdByTitle(title)

    // Recipe Ingredient Logic:
    override suspend fun insertRecipeIngredient(
        recipeIngredient: RecipeIngredient
    ): Long = recipeIngredientDao.insert(recipeIngredient)

    // Recipe Ingredient Section Logic:
    override suspend fun insertRecipeIngredientSection(
        recipeIngredientSection: RecipeIngredientSection
    ): Long = recipeIngredientSectionDao.insert(recipeIngredientSection)
    override fun getRecipeSectionById(
        id: Long
    ): Flow<RecipeIngredientSection?> = recipeIngredientSectionDao.getById(id)

    // Recipe Step Logic:
    override suspend fun insertRecipeStep(
        recipeStep: RecipeStep
    ): Long = recipeStepDao.insert(recipeStep)
    override suspend fun updateStepCompleteness(
        recipeStepId: Long, complete: Boolean
    ) = recipeStepDao.updateStepCompleteness(recipeStepId, complete)

    // Recipe Tag Logic:
    override suspend fun insertRecipeTag(recipeTag: RecipeTag): Long = recipeTagDao.insert(recipeTag)

    // Recipe Nutrition Logic:
    override suspend fun insertRecipeNutrition(recipeNutrition: RecipeNutrition) {
        recipeNutritionDao.insert(recipeNutrition)
    }

    override suspend fun deleteNutritionForRecipeId(recipeId: Long){
        recipeNutritionDao.deleteByRecipeId(recipeId)
    }

    override fun getNutritionForRecipe(recipeId: Long): Flow<RecipeNutrition?> {
        return recipeNutritionDao.getByRecipeId(recipeId)
    }

    // Recipe Query Logic:
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
     * Creates a recipe in the database and its corresponding entities
     * @param uid - The uid of the recipe to add to the db
     * @param llmRecipe - The LlmRecipe to add to the db
     * @param shouldNotScaleIngredients - ensures that altered recipes don't get divided by serving
     *                                    size. only new recipes should have this.
     * @param recipeId - The id of the recipe to add.
     * @param imagePath - The image path of the image attached to the recipe, and "" if none is provided.
     * @param llmGenerated - determines if the recipe was user inputted or generated by the LLM
     */
    override suspend fun createRecipe(
        uid: String,
        llmRecipe: LlmRecipe,
        shouldNotScaleIngredients: Boolean,
        recipeId: Long,
        imagePath: String,
        llmGenerated: Boolean
    ) {
        // If throws, it is handled accordingly where it is called.
        db.withTransaction {
            // 1. Create or get the recipe type, only add in form where first char is capital
            // (i.e. dinner -> Dinner) to ensure no dupes
            val mealTypeFormatted = (llmRecipe.mealType).lowercase().replaceFirstChar {
                it.uppercase()
            }
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
                    val singular = if (llmGenerated) {
                        ingredient.ingredientSingular.lowercase() // always lowercase from LLM
                    } else {
                        ingredient.ingredientSingular
                    }
                    val plural = if (llmGenerated) {
                        ingredient.ingredientPlural.lowercase() // always lowercase from LLM
                    } else {
                        ingredient.ingredientPlural
                    }
                    // purpose of shouldNotScaleIngredients is to ensure that altered recipes
                    // don't get divided by serving size. only LLM generated recipes should have this.
                    // Otherwise, this is handled in the parseToLlmRecipe for user-added/-edited recipes,
                    // and already by the LLM in alterations.
                    val amount = if (shouldNotScaleIngredients) {
                        ingredient.ingredientAmount
                    } else {
                        // divide by number of servings, so ingredient is stored for 1 serving.
                        ingredient.ingredientAmount / recipe.numberOfServings
                    }
                    val unit = ingredient.ingredientAmountUnit
                    // add it to ingredient table if doesn't already exist in its plural form,
                    // check both values due to human error:
                    var ingredientId = ingredientRepository.getIdByPlural(plural)

                    if (ingredientId == null) {
                        // if plural doesn't match, singular is checked to ensure it doesn't exist:
                        // (against plural - as this is the field populated by the user when adding an ingredient)
                        ingredientId = ingredientRepository.getIdByPlural(singular)
                        if (ingredientId == null) {
                            // if still null, it doesn't exist so add the ingredient
                            ingredientId = ingredientRepository.insert(Ingredient(
                                singular = singular, plural = plural, userAdded = !llmGenerated)
                            )
                        }
                    }

                    // Correct the user inputted value:
                    // if the ingredient has been added by the user and not yet updated,
                    // check if the true singular = the stored singular, and same for plural.
                    // If either don't match, update it to be correct.
                    val ingredient = ingredientRepository.getById(ingredientId).first()
                    if (ingredient != null && ingredient.userAdded &&
                        (ingredient.singular != singular || ingredient.plural != plural)) {
                        ingredientRepository.update(ingredient.copy(
                            plural = plural, singular = singular, userAdded = false)
                        )
                    }

                    // Ingredient unit: find the corresponding unit in the enum
                    val ingredientUnit = IngredientUnit.valueOf(unit)
                    // Then create a RecipeIngredient with the Recipe, Ingredient and the newly
                    // added RecipeSection:
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
                // per user identifier.
                val tagId = tagRepository.getIdByIdentifier(tagIdentifier, uid)
                // must already exist; llm has to choose from predefined (not make own).
                // only users can add more.
                if (tagId != null) {
                    // just ignore it if null; don't add the tag. may be produced from llm
                    // hallucination, user doesn't need to know about this.
                    recipeTagDao.insert(
                        RecipeTag(recipeId = recipeId, tagId = tagId)
                    )
                }
            }
        }
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.update(recipe)
    }

    /**
     * Scales a recipe's serving size.
     * Transactional so the scale is reverted if an error occurs, so the recipe scale stays correct.
     */
    override suspend fun scaleRecipe(recipe: Recipe, currentServingSize: Int, increase: Boolean) {
        db.withTransaction {
            val newServingSize = if (increase) { currentServingSize+1 } else { currentServingSize-1 }
            // update serving size:
            recipeDao.update(recipe.copy(numberOfServings = newServingSize))
        }
    }

    /**
     * Replaces a Recipe, by deleting the Recipe to replace and creating a new Recipe.
     * @param currentRecipe - The current recipe, which is to be deleted
     * @param newRecipe - The new recipe, to be added to the DB under the same ID as currentRecipe
     *                    once deleted.
     * @param updatedImagePath - The path of the new image, if a new image was added.
     * @param llmGenerated - Boolean reflecting whether the replaced recipe was LLM generated (from
     *                       alteration) or not (from manual edit).
     */
    override suspend fun replaceRecipe(
        currentRecipe: Recipe,
        newRecipe: LlmRecipe,
        updatedImagePath: String,
        llmGenerated: Boolean
    ) {
        val originalRecipeId = currentRecipe.id
        val originalRecipeAuthor = currentRecipe.authorUid

        // the previous nutritional values get deleted as they will no longer be accurate
        deleteNutritionForRecipeId(originalRecipeId)

        deleteRecipe(currentRecipe)
        createRecipe(
            originalRecipeAuthor,
            newRecipe,
            shouldNotScaleIngredients = true,
            originalRecipeId,
            updatedImagePath,
            llmGenerated = llmGenerated
        )
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.delete(recipe)
    }

}