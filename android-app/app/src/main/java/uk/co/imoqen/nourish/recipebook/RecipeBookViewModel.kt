package uk.co.imoqen.nourish.recipebook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeTags
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository
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
    private val ingredientRepository: IngredientRepository,
    private val tagRepository: TagRepository
): ViewModel() {

    private val mutableUiState = MutableStateFlow(RecipeBookUiState())
    val uiState: StateFlow<RecipeBookUiState> = mutableUiState.asStateFlow()

    // Setters for Ui State values:
    fun setRecipeCard(selectedRecipeCard: RecipeCard) {
        mutableUiState.update { currentState ->
            currentState.copy(
                selectedRecipeCard = selectedRecipeCard
            )
        }
    }

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

    fun getRecipeTagsForRecipe(recipeId: Long): Flow<RecipeWithRecipeTags> {
        return recipeRepository.getRecipeWithRecipeTypes(recipeId)
    }

    fun getIngredientById(ingredientId: Long): Flow<Ingredient?> {
        return ingredientRepository.getById(ingredientId)
    }

    fun getRecipeTypeById(recipeTypeId: Long): Flow<RecipeType?> {
        return recipeRepository.getRecipeTypeById(recipeTypeId)
    }

    fun getTagByTagId(tagId: Long): Flow<Tag?> {
        return tagRepository.getById(tagId)
    }

    fun getSectionById(sectionId: Long): Flow<RecipeIngredientSection?> {
        return recipeRepository.getRecipeSectionById(sectionId)
    }

    fun buildRecipeCard(
        recipe: Recipe
    ) {
        viewModelScope.launch() {
            runCatching {
                val title = recipe.title
                val ingredientsMap =
                    buildIngredientsMap(getRecipeIngredientsForRecipe(recipe.id).first())
                val method = recipe.method
                val serves = recipe.numberOfServings
                val recipeType = findRecipeType(recipe.recipeTypeId)
                val recipeTags = buildRecipeTags(recipe.id)
                val storageInstructions = recipe.storageInstructions

                if (ingredientsMap == null || recipeType == null) {
                    Log.i("recipe book error", "null")
                    return@launch
                }

                val recipeCard = RecipeCard(
                    title, ingredientsMap, method, serves, recipeType,
                    recipeTags, storageInstructions
                )

                setRecipeCard(recipeCard)
            }
                .onSuccess {
                    Log.i("recipe book", "successfully parsed into recipe card")
                }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("The recipe could not be displayed. Please try again later."))
                    Log.i("recipe book", "failed: $e")
                }
        }
    }

    suspend fun buildRecipeTags(
        recipeId: Long
    ): MutableList<String> {
        val tagsList: MutableList<String> = mutableListOf()
        val tags = getRecipeTagsForRecipe(recipeId).first()
            tags.recipeTags.forEach{ tag ->
                val tagIdentifier = getTagByTagId(tag.tagId).first()?.identifier
                // delegate up to deal with error
                if (tagIdentifier == null) {
                    throw NullPointerException()
                }
                tagsList.add(tagIdentifier)
            }
        return tagsList
    }

    suspend fun findRecipeType(
        recipeTypeId: Long
    ): String? {
            return getRecipeTypeById(recipeTypeId).first()?.title

    }

    suspend fun buildIngredientsMap(
        recipeIngredients: RecipeWithRecipeIngredients?
    ): MutableMap<String, MutableList<Triple<String, Double, Long>>>? {
        if (recipeIngredients == null) { return null }

        // map with key sectionTitle and value of list of pair of ingredient string and ingredient id (so it can be referenced in future)
        val sectionsWithIngredients: MutableMap<String, MutableList<Triple<String, Double, Long>>> = mutableMapOf()

            // first bundle them all by section
            recipeIngredients.ingredients.forEach { ingredient ->

                // title of the section:
                val sectionId = ingredient.sectionId
                val sectionTitle = getSectionById(sectionId).first()?.title // this may fail if the values stored in the db update while user is looking at it. for this, would need to use .collect(). consider this if issues occur in future.

                // determine pluralities
                val ingredientAmount = ingredient.amount
                val ingredientUnit = ingredient.ingredientUnit
                val unit = if (determineUnitPlurality(ingredientAmount)) {
                    ingredientUnit.pluralName
                } else {
                    ingredientUnit.singularName
                }
                val ingredientName = getIngredientById(ingredient.ingredientId).first()


                if (ingredientName != null && sectionTitle != null) {

                    val ingredientPluralisedName =
                        if (determineIngredientPlurality(ingredientAmount, ingredientUnit)) {
                            ingredientName.plural
                        } else {
                            ingredientName.singular
                        }

                    var ingredientText = ""
                    ingredientText = if (unit != "") {
                        ("$unit $ingredientPluralisedName")
                    } else {
                        (ingredientPluralisedName)
                    }

                    sectionsWithIngredients.getOrPut(sectionTitle) { mutableListOf() }
                        .add(Triple(ingredientText, ingredientAmount, ingredient.ingredientId))

                }
            }


        return sectionsWithIngredients

    }

    // determine if plural form should be used or not
// true if plural, false if singular
    fun determineUnitPlurality(
        ingredientAmount: Double,
    ): Boolean {
        // plural form should be used in most cases; just a few cases where it shouldn't.
        return ingredientAmount != 1.0 // if only 1, should always be singular.
    }

    fun determineIngredientPlurality(
        ingredientAmount: Double,
        ingredientUnit: IngredientUnit
    ): Boolean {
        if (ingredientAmount <= 1.0) {
            // e.g. 1 strawberry = singular
            // but 1 cup strawberry should be plural: 1 cup strawberries
            if (ingredientUnit.useSingularForOne) {
                return false // use singular
            }
        }
        return true
    }


}