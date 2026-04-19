package uk.co.imoqen.nourish.recipebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeSteps
import uk.co.imoqen.nourish.db.recipe.RecipeWithRecipeTags
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeWithRecipeIngredients
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSection
import uk.co.imoqen.nourish.db.recipe.recipenutrition.RecipeNutrition
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQuery
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.network.response.LlmRecipe
import java.lang.NullPointerException
import javax.inject.Inject
import kotlin.collections.getOrPut
import kotlin.math.round

/**
 * The ViewModel for RecipeBook related logic. Utilised in the View to handle the recipe book display,
 * searching, filtering, and recipe creation.
 * @param authenticationManager - The AuthenticationManager to authenticate using Firebase.
 * @param apiManager - The ApiManager to communicate with the LLM, required with actions in RecipeCard.
 * @param uiEventBus - The UiEventBus to display alerts / Snackbar messages.
 * @param recipeRepository - The Recipe Repository, to access the Recipe db table.
 * @param ingredientRepository - The Ingredient Repository, to access the Ingredient db table.
 * @param tagRepository - The Tag Repository, to access the Tag db table.
 * @param recipeActions - The Auxiliary class RecipeActions to access and use these common Recipe actions.
 * @param shoppingListEntryRepository - The ShoppingListEntry Repository, to access the
 *                                      ShoppingListEntry db table.
 */
@HiltViewModel
class RecipeBookViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager,
    private val apiManager: ApiManager,
    private val uiEventBus: UiEventBus,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val tagRepository: TagRepository,
    private val recipeActions: RecipeActions,
    private val shoppingListEntryRepository: ShoppingListEntryRepository
): ViewModel() {

    private val mutableUiState = MutableStateFlow(RecipeBookUiState())
    val uiState: StateFlow<RecipeBookUiState> = mutableUiState.asStateFlow()

    // Setters for Ui State values:
    fun setRecipeCard(selectedRecipeCard: RecipeCardExpanded) {
        mutableUiState.update { currentState ->
            currentState.copy(
                selectedRecipeCard = selectedRecipeCard
            )
        }
    }

    fun setSelectedRecipe(selectedRecipe: Recipe) {
        mutableUiState.update { currentState ->
            currentState.copy(
                selectedRecipe = selectedRecipe
            )
        }
    }

    fun setRecipeBookMap(recipeBookMap: MutableMap<RecipeType, MutableList<RecipeCardUnexpanded>>) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeBookMap = recipeBookMap
            )
        }
    }

    fun setSearchTerm(searchTerm: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                searchTerm = searchTerm
            )
        }
    }

    fun setDisplayDropdownFilter(displayDropdownFilter: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                displayDropdownFilter = displayDropdownFilter
            )
        }
    }

    fun setRecipeTags(recipeTags: List<TagSelection>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeTags = recipeTags
            )
        }
    }

    fun setRecipeTypes(recipeTypes: List<TypeSelection>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeTypes = recipeTypes
            )
        }
    }

    fun setLlmActionType(llmActionType: LlmActionType) {
        mutableUiState.update { currentState ->
            currentState.copy(
                llmActionType = llmActionType
            )
        }
    }

    fun setLlmAwaitActionType(llmAwaitActionType: LlmActionType) {
        mutableUiState.update { currentState ->
            currentState.copy(
                llmAwaitActionType = llmAwaitActionType
            )
        }
    }

    fun setUserActionRequest(userActionRequest: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                userActionRequest = userActionRequest
            )
        }
    }

    fun setActionChatStage(actionChatStage: ActionChatStage) {
        mutableUiState.update { currentState ->
            currentState.copy(
                actionChatStage = actionChatStage
            )
        }
    }

    fun setAlteredRecipe(alteredRecipe: LlmRecipe?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                alteredRecipe = alteredRecipe
            )
        }
    }

    fun setVisible(visible: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                visible = visible
            )
        }
    }

    fun setIngredientsToAddToShoppingList(
        ingredientsToAddToShoppingList: MutableList<Pair<ShoppingListIngredient, Boolean>>?
    ) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientsToAddToShoppingList = ingredientsToAddToShoppingList
            )
        }
    }

    fun setDeleteConfirmation(deleteConfirmation: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                deleteConfirmation = deleteConfirmation
            )
        }
    }

    /**
     * Alters the status of an ingredient in the shopping list.
     * @param ingredientToAddToShoppingList - The ingredient to add
     * @param index - Index to add; safe to use  because it wont change after populated.
     *                For safety, done in runCatching.
     * @param selected - Whether the ingredient is selected
     */
    fun alterIngredientShoppingListStatus(
        ingredientToAddToShoppingList: ShoppingListIngredient,
        index: Int,
        selected: Boolean
    ) {
        viewModelScope.launch() {
            runCatching {
                val updatedList = mutableUiState.value.ingredientsToAddToShoppingList?.toMutableList().apply {
                    this?.set(index, Pair(ingredientToAddToShoppingList, selected))
                }
                setIngredientsToAddToShoppingList(updatedList)
            }
                .onFailure {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Cannot edit ingredient status."))
                }
        }
    }

    /**
     * Formats all ingredients to be placed into the shopping list.
     */
    fun formatIngredientsForShoppingList() {
        viewModelScope.launch() {
            runCatching {
                // Get the recipe:
                val recipe = mutableUiState.value.selectedRecipe
                if (recipe == null) {
                    return@launch
                }

                // Preprocessing:
                // First pass of the list: add up all same ingredients so that we have totals for
                // each ingredient + combine them if needed.
                // e.g. if multiple ingredients appear in different sections. we don't want to add
                // them to the shopping list twice.
                val shoppingListIngredients: MutableList<ShoppingListIngredient> = mutableListOf()
                val recipeIngredients =
                    recipeRepository.getRecipeWithRecipeIngredients(recipe.id).first().ingredients

                recipeIngredients.forEach { recipeIngredient ->
                    val ingredient =
                        ingredientRepository.getById(recipeIngredient.ingredientId).first()
                    if (ingredient == null) {
                        return@launch // error
                    }
                    // Water is not added - don't need to buy that (in UK)
                    if (ingredient.singular.lowercase() != "water") {

                        // Get the unit and if it is a unit to convert into UNIT type, then do that now
                        // this is so that ingredients e.g. "head of, sprig of, ..." are still countable
                        // as this pretty much converts to 1 of those ingredients.
                        var unit = recipeIngredient.ingredientUnit
                        val toConvertToUnit: List<IngredientUnit> = listOf(
                            IngredientUnit.HEAD, IngredientUnit.BUNCH, IngredientUnit.BULB,
                            IngredientUnit.STALK, IngredientUnit.STICK, IngredientUnit.LOAF,
                            IngredientUnit.ROLL, IngredientUnit.SHEET, IngredientUnit.BLOCK,
                            IngredientUnit.BAR
                        )
                        if (toConvertToUnit.contains(unit)) {
                            unit = IngredientUnit.UNIT // So it converts to a countable
                        }
                        val amount =
                            // Multiply by the desired serving size of the user
                            recipeIngredient.amount * recipe.numberOfServings

                        val existingIngredientIndex =
                            (shoppingListIngredients.indexOfFirst {
                                it.ingredientId == recipeIngredient.ingredientId && it.unit == unit
                            })

                        // Check if the ingredient is already in the list and only update it if the
                        // amount needs to change (i.e. not tsp etc)
                        if (existingIngredientIndex != -1) {
                            val existingIngredient =
                                shoppingListIngredients[existingIngredientIndex]
                            // If it is, then alter the existing list entry
                            // stays same but amount just gets changed
                            val updatedIngredient =
                                existingIngredient.copy(
                                    // sets amount to added amount if  unit is large, otherwise just remains as 1.
                                    amount = if (unit.keepUnitInShoppingList) {
                                        existingIngredient.amount + amount
                                    } else {
                                        amount
                                    } // add the scaled amount
                                )
                            // then overwrite it
                            shoppingListIngredients[existingIngredientIndex] = updatedIngredient
                        } else {
                            // if it doesn't already exist in the list, then simply add it:
                            shoppingListIngredients.add(
                                ShoppingListIngredient(
                                    ingredient.id,
                                    ingredient.plural,
                                    unit,
                                    amount
                                )
                            )

                        }
                    }
                }

                // Then go through the array containing no duplicates and add them to the
                // selection list - declare the list for storing the selections after preprocessing:
                val shoppingListIngredientSelections: MutableList<Pair<ShoppingListIngredient, Boolean>> = mutableListOf()

                shoppingListIngredients.forEach { shoppingListIngredient ->
                    val ingredient =
                        ingredientRepository.getById(shoppingListIngredient.ingredientId).first()
                    if (ingredient == null) {
                        return@launch // error
                    }
                    var unit = shoppingListIngredient.unit
                    var amount = shoppingListIngredient.amount
                    // If the unit shouldn't be kept, or the amount is inconsiderable
                    // (less than 15 IF not UNIT or PINT, l and kg doesn't matter because we store
                    // it in ml /g anyway.) then just set to buy 1.
                    if (!unit.keepUnitInShoppingList ||
                        ((unit != IngredientUnit.PINT && unit != IngredientUnit.UNIT) && amount <= 15)
                        ) {
                        unit = IngredientUnit.SINGLE
                        amount = 1.0
                    }

                    shoppingListIngredientSelections.add(Pair(
                        ShoppingListIngredient(
                            ingredient.id,
                            ingredient.plural,
                            unit,
                            amount
                        ), true) // by default it is true; to add to shopping list.
                    )

                }

                // success:
                setIngredientsToAddToShoppingList(shoppingListIngredientSelections)
            }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "Ingredients cannot be added. Please try another recipe, or add manually."
                        )
                    )
                }
        }
    }

    /**
     * Adds selected ingredients of a recipe to the shopping list.
     */
    fun addIngredientsToShoppingList() {
        viewModelScope.launch() {
            runCatching {

                // get all the ingredients:
                val shoppingListIngredients = mutableUiState.value.ingredientsToAddToShoppingList

                // get uid:
                val uid = authenticationManager.getUid()
                if (uid == "") {
                    return@launch
                }

                // go through them all and add the ones to the db which should be added:
                shoppingListIngredients?.forEach { (ingredient, selected) ->
                    if (selected) {
                        // add to db and account for if the ingredient is already in the db.
                        // this is handled in the repository.
                        shoppingListEntryRepository.addShoppingListEntry(
                            ingredient, uid, userAdded=false
                        )
                    }
                }

            }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "Ingredients cannot be added. Please try another recipe, or add manually."
                        )
                    )
                }
                .onSuccess {
                    // reset the selection and notify the user
                    setIngredientsToAddToShoppingList(null)
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "Ingredients have been added to your shopping list."
                        )
                    )

                }
        }
    }

    // CRUD functionalities:

    fun getAllRecipes(): Flow<List<Recipe>> {
        val uid = authenticationManager.getUid()
        if (uid == "") return flowOf(emptyList())
        return recipeRepository.getAllRecipes(uid)
    }

    fun getRecipeIngredientsForRecipe(recipeId: Long): Flow<RecipeWithRecipeIngredients> {
        return recipeRepository.getRecipeWithRecipeIngredients(recipeId)
    }

    fun getRecipeTagsForRecipe(recipeId: Long): Flow<RecipeWithRecipeTags> {
        return recipeRepository.getRecipeWithRecipeTags(recipeId)
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

    fun getRecipeById(recipeId: Long): Flow<Recipe?> {
        return recipeRepository.getRecipeById(recipeId)
    }

    fun getSectionById(sectionId: Long): Flow<RecipeIngredientSection?> {
        return recipeRepository.getRecipeSectionById(sectionId)
    }

    fun getRecipeStepsForRecipe(recipeId: Long): Flow<RecipeWithRecipeSteps> {
        return recipeRepository.getRecipeWithRecipeSteps(recipeId)
    }

    // Recipe Nutrition Logic:
    fun getNutritionForRecipe(recipeId: Long): Flow<RecipeNutrition?> {
        return recipeRepository.getNutritionForRecipe(recipeId)
    }

    // Recipe Query Logic:
    fun getAllQueriesForRecipe(recipeId: Long): Flow<List<RecipeQuery?>> {
        return recipeRepository.getAllQueriesForRecipe(recipeId)
    }

    fun markStepAsComplete(recipeStepId: Long, complete: Boolean) {
        viewModelScope.launch() {
            runCatching {
                recipeRepository.updateStepCompleteness(recipeStepId, complete)
            }
                .onSuccess {
                    // rebuild the recipe card to update it in the ui
                    buildRecipeCard()
                }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The step could not be marked as complete. Please try again later."
                        )
                    )
                }
        }
    }

    /**
     * Builds up a recipe card, given the current selected recipe.
     */
    fun buildRecipeCard() {
        val recipe = mutableUiState.value.selectedRecipe
        viewModelScope.launch() {
            if (recipe == null) {
                return@launch
            }
            runCatching {
                val title = recipe.title
                val ingredientsMap =
                    buildIngredientsMap(
                        getRecipeIngredientsForRecipe(recipe.id).first()
                    )
                val method = buildRecipeSteps(recipe.id)
                val serves = recipe.numberOfServings
                val recipeType = recipeActions.findRecipeType(recipe.recipeTypeId)
                val recipeTags = buildRecipeTags(recipe.id)
                val storageInstructions = recipe.storageInstructions
                if (ingredientsMap == null || recipeType == null) {
                    return@launch
                }
                val recipeCard = RecipeCardExpanded(
                    title, ingredientsMap, method, serves, recipeType,
                    recipeTags, storageInstructions, recipe.imagePath, recipe.emoji,
                    enumValueOf<NourishColour>(recipe.colour.uppercase()), recipe.generated
                )
                setRecipeCard(recipeCard)
            }
        }
    }

    /**
     * Builds recipe tags for a recipe.
     * @param recipeId - The id of the recipe to get the tags of.
     * @return a mutable list of Tags
     */
    suspend fun buildRecipeTags(
        recipeId: Long
    ): MutableList<Pair<String, NourishColour>> {
        val tagsList: MutableList<Pair<String, NourishColour>> = mutableListOf()
        val tags = getRecipeTagsForRecipe(recipeId).first()
        tags.recipeTags.forEach{ recipeTag ->
            val tag = getTagByTagId(recipeTag.tagId).first()
            // delegate up to deal with error
            if (tag == null) {
                throw NullPointerException()
            }
            tagsList.add(Pair(tag.displayName, enumValueOf<NourishColour>(tag.colour)))
        }
        return tagsList
    }

    /**
     * Builds recipe steps for a recipe.
     * @param recipeId - The id of the recipe to get the recipe steps of.
     * @return a mutable list of RecipeSteps
     */
    suspend fun buildRecipeSteps(
        recipeId: Long
    ): MutableList<Triple<String, Boolean, Long>> {
        val recipeStepsList: MutableList<Triple<String, Boolean, Long>> = mutableListOf()
        getRecipeStepsForRecipe(recipeId).first().recipeSteps.forEach{ step ->
            recipeStepsList.add(Triple(step.instruction, step.complete, step.id))
        }
        return recipeStepsList
    }

    /**
     * Builds an map of ingredients, given a recipe with its recipe ingredients.
     * @param recipeIngredients - The recipe with its recipe ingredients.
     * @return a mutable map of the ingredients.
     */
    suspend fun buildIngredientsMap(
        recipeIngredients: RecipeWithRecipeIngredients?
    ): MutableMap<String, MutableList<Triple<String, Double, Long>>>? {
        if (recipeIngredients == null) { return null }
        val recipe = mutableUiState.value.selectedRecipe
        if (recipe == null) { return null }

        // map with key sectionTitle and value of list of pair of ingredient string and ingredient
        // id (so it can be referenced in future)
        val sectionsWithIngredients: MutableMap<String, MutableList<Triple<String, Double, Long>>> = mutableMapOf()

        // first bundle them all by section
        recipeIngredients.ingredients.forEach { ingredient ->

            // title of the section:
            val sectionId = ingredient.sectionId
            val sectionTitle = getSectionById(sectionId).first()?.title

            // determine pluralities
            var ingredientAmount = ingredient.amount * recipe.numberOfServings
            var ingredientUnit = ingredient.ingredientUnit

            // edge case: if g or ml and >= 1000, use kg and l respectively and scale the amount.
            if (ingredientAmount >= 1000 &&
                (ingredientUnit == IngredientUnit.G || ingredientUnit == IngredientUnit.ML)) {
                when (ingredientUnit) {
                    IngredientUnit.G -> { ingredientUnit = IngredientUnit.KG }
                    IngredientUnit.ML -> { ingredientUnit = IngredientUnit.L }
                    else -> {} // make no change
                }
                ingredientAmount = ingredientAmount / 1000
            }

            val unit = if (recipeActions.determineUnitPlurality(ingredientAmount)) { // scaled to num servings
                ingredientUnit.pluralName
            } else {
                ingredientUnit.singularName
            }
            val ingredientName = getIngredientById(ingredient.ingredientId).first()

            if (ingredientName != null && sectionTitle != null) {
                val ingredientPluralisedName =
                    if (recipeActions.determineIngredientPlurality(ingredientAmount, ingredientUnit)) {
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

    /**
     * Gets all recipes and places them into a recipe book map.
     * Utilises debouncing for smoothness.
     */
    @OptIn(FlowPreview::class) // for debounce: smoothness
    fun getRecipeBookMap() {
        viewModelScope.launch() {
            runCatching {
                // Get all recipes. debouncing is added to provide smoothness
                val allRecipes = getAllRecipes().debounce(300).first()
                val recipeBookMap: MutableMap<RecipeType, MutableList<RecipeCardUnexpanded>> = mutableMapOf()
                allRecipes.forEach { recipe ->
                    val type = recipeRepository.getRecipeTypeById(recipe.recipeTypeId).first()
                    // build the unexpanded recipe card:
                    val recipeCardUnexpanded = RecipeCardUnexpanded(
                        title = recipe.title,
                        recipeTags = buildRecipeTags(recipe.id),
                        imagePath = recipe.imagePath,
                        emoji = recipe.emoji,
                        recipeId = recipe.id,
                        colour = enumValueOf<NourishColour>(recipe.colour)
                    )
                    if (type == null) {
                        throw NullPointerException()
                    }
                    recipeBookMap.getOrPut(type) { mutableListOf() }
                        .add(recipeCardUnexpanded)
                }
                // to ensure recipes are printed: Breakfast -> Lunch -> Dinner -> Dessert -> Side -> Drink
                // sort by ordinal values of the recipeType:
                val sorted = recipeBookMap.toSortedMap(compareBy { it.ordinal })
                setRecipeBookMap(sorted)
            }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "Couldn't fetch recipes. Please try again later."
                        )
                    )
                }
        }
    }

    /**
     * Alters the serving size of a recipe.
     * @param increase - False to decrease, True to increase.
     */
    fun alterServingSize(
        increase: Boolean // true = increase by 1, false = decrease by 1. cap at 1.
    ) {
        viewModelScope.launch() {
            val recipe = mutableUiState.value.selectedRecipe
            if (recipe == null) {
                return@launch
            }

            val currentServingSize = recipe.numberOfServings
            if (currentServingSize == 1 && !increase) {
                return@launch
            }

            // scale the recipe and save to db, reverting back if failed:
            recipeRepository.scaleRecipe(recipe, currentServingSize, increase)
        }
    }

    // RecipeActions:

    fun getAllTagsForUser() {
        viewModelScope.launch() {
            (recipeActions.getAllTagsForUser(
                mutableUiState.value.recipeTags
            ))?.let { updatedTags ->
                setRecipeTags(updatedTags) // only sets if not null
            }
        }
    }

    fun getAllRecipeTypes() {
        viewModelScope.launch() {
            (recipeActions.getAllRecipeTypesSelectionList(
                mutableUiState.value.recipeTypes
            ))?.let { updatedTypes ->
                setRecipeTypes(updatedTypes) // only sets if not null
            }
        }
    }

    fun updateSelected(tag: Tag, status: Boolean) {
        viewModelScope.launch() {
            setRecipeTags(
                recipeActions.updateSelected(
                    tag,
                    status,
                    mutableUiState.value.recipeTags
                )
            )
        }
    }

    fun updateSelected(recipeType: RecipeType, status: Boolean) {
        viewModelScope.launch() {
            setRecipeTypes(
                recipeActions.updateSelected(
                    recipeType,
                    status,
                    mutableUiState.value.recipeTypes
                )
            )
        }
    }

    fun formatIngredientAmount(
        ingredientAmount: Double,
        roundToNearestQuarter: Boolean = true
    ): String {
        return recipeActions.formatIngredientAmount(ingredientAmount, roundToNearestQuarter)
    }

    /**
     * Parses a Recipe to an LLM recipe, to send it for use in Llm Action Requests (nutrition, query,
     * or alteration).
     * @param recipe - The recipe to parse
     * @param includeServings - Whether servings should be included or not.
     */
    suspend fun parseRecipeToLlmRecipe(
        recipe: Recipe,
        includeServings: Boolean
    ): LlmRecipe {
        val recipeType = recipeActions.findRecipeType(recipe.recipeTypeId)?.first ?: ""
        val tags = getRecipeTagsForRecipe(recipe.id).first().recipeTags.map { tag ->
            getTagByTagId(tag.tagId).first()?.identifier ?: ""
        }

        // build ingredients

        val map: MutableMap<String, MutableList<LlmIngredient>> = mutableMapOf()
        val allIngredients = getRecipeIngredientsForRecipe(recipe.id).first().ingredients

        allIngredients.forEach { recipeIngredient ->
            val section = getSectionById(recipeIngredient.sectionId).first()?.title ?: ""
            val ingredient = getIngredientById(recipeIngredient.ingredientId).first()
            map.getOrPut(section) { mutableListOf() }.add(LlmIngredient(
                ingredientSingular = ingredient?.singular ?: "",
                ingredientPlural = ingredient?.plural ?: "",
                // so that value is not too many digits long; saves tokens:
                ingredientAmount = round(recipeIngredient.amount * 100) / 100,
                ingredientAmountUnit = recipeIngredient.ingredientUnit.name
            ))
        }

        val ingredientsList: MutableList<LlmIngredientSection> = mutableListOf()
        map.forEach { section, ingredients ->
            ingredientsList.add(LlmIngredientSection(
                section, ingredients
            ))
        }

        val method = getRecipeStepsForRecipe(recipe.id).first().recipeSteps.map { step ->
            step.instruction
        }

        return LlmRecipe(
            title = recipe.title,
            sectionIngredients = ingredientsList,
            // values are given for 1 serving, but the llm should preserve this value as it was set
            // by the user for scaling.
            servings = if (includeServings) { recipe.numberOfServings } else 1,
            mealType = recipeType,
            method = method,
            tags = tags,
            storage = recipe.storageInstructions,
            recipeNotPossible = false,
            emoji = recipe.emoji,
            colour = recipe.colour
        )
    }

    /**
     * Makes the LLM API call to perform an LLM Action.
     * @param type - the type of LLM Action to perform.
     */
    fun makeLlmApiCall(
        type: LlmActionType,
    ) {
        setLlmAwaitActionType(type)
        setActionChatStage(ActionChatStage.AWAITING_LLM_RESPONSE)
        viewModelScope.launch() {
            runCatching {
                val userPrompt = mutableUiState.value.userActionRequest
                val recipe = mutableUiState.value.selectedRecipe

                // validation
                if (userPrompt.trim() == "") {
                    setActionChatStage(ActionChatStage.USER_INPUT) // error occurred - not awaiting.
                    setLlmAwaitActionType(LlmActionType.NONE)
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The input must not be blank to proceed."
                        )
                    )
                    return@launch
                }
                if (recipe == null) {
                    setActionChatStage(ActionChatStage.USER_INPUT) // error occurred - not awaiting.
                    setLlmAwaitActionType(LlmActionType.NONE)
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "A recipe must be selected to proceed."
                        )
                    )
                    return@launch
                }

                var queryId: Long = -1
                if (type == LlmActionType.QUESTION) {
                    // create the query object in the db just with the user's question
                    queryId = recipeRepository.insertRecipeQuery(
                        RecipeQuery(
                            recipeId = recipe.id,
                            userQuestion = userPrompt,
                            llmAnswer = ""
                        )
                    )
                }

                val dishType = getRecipeTypeById(recipe.recipeTypeId).first()?.title ?: ""
                var allNutritionalFilters = mutableUiState.value.recipeTags?.joinToString(", ") {
                    it.tag.identifier
                }
                if (allNutritionalFilters == null) {
                    allNutritionalFilters = "[]" // signifies empty list
                }
                // make it into a request:
                val includeServings = (type == LlmActionType.ALTERATION) // alteration only has servings
                val request = LlmActionRequest(
                    parseRecipeToLlmRecipe(recipe, includeServings),
                    userPrompt, allNutritionalFilters, dishType
                )

                // validate the user's token:
                val tokenResult = authenticationManager.getUserToken()
                if (tokenResult == null) {
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            ErrorMessage.API_ERROR_SERVICE_UNREACHABLE
                        )
                    )
                    return@launch
                }

                apiManager.makeRetrofitLlmActionCall(
                    type,
                    request,
                    tokenResult,
                    onQuestionAnswered = { llmQuestionResponse ->
                        val llmAnswer = llmQuestionResponse?.answer
                        if (llmAnswer != null) {
                            viewModelScope.launch() {
                                // add the answer to the DB and display to the user in the chat UI
                                val query = recipeRepository.getRecipeQueryById(
                                    queryId
                                ).first()
                                if (query != null) {
                                    recipeRepository.updateRecipeQuery(
                                        query.copy(llmAnswer = llmAnswer)
                                    )
                                }
                                setActionChatStage(ActionChatStage.RESPONSE_RECEIVED)
                                setLlmAwaitActionType(LlmActionType.NONE)
                                setUserActionRequest("") // so the text doesn't still appear in their send bar
                            }
                        }

                    },
                    onRecipeAltered = { llmRecipe ->
                        if (llmRecipe != null) {
                            viewModelScope.launch() {
                                setActionChatStage(ActionChatStage.RESPONSE_RECEIVED)
                                setLlmAwaitActionType(LlmActionType.NONE)
                                if (llmRecipe.recipeNotPossible) {
                                    uiEventBus.enqueueEvent(
                                        UiEvent.ShowSnackbar(
                                            "Valid recipe could not be generated. Please enter something different."
                                        )
                                    )
                                    return@launch
                                }
                                // Display the recipe to the user. there will be a button to
                                // overwrite the existing recipe, or save as a new recipe. when
                                // clicked, this will do the according process.
                                setAlteredRecipe(llmRecipe)
                                setUserActionRequest("")
                            }
                        }
                    },
                    onNutritionGenerated = { llmNutritionResponse ->
                        if (llmNutritionResponse != null) {
                            viewModelScope.launch() {
                                // add nutrition to the DB and display to user.
                                recipeRepository.insertRecipeNutrition(
                                    RecipeNutrition(
                                        recipeId = recipe.id,
                                        servings = llmNutritionResponse.servings,
                                        calories = llmNutritionResponse.calories,
                                        fat = llmNutritionResponse.fat,
                                        saturates = llmNutritionResponse.saturates,
                                        carbohydrate = llmNutritionResponse.carbohydrate,
                                        sugars = llmNutritionResponse.sugars,
                                        protein = llmNutritionResponse.protein,
                                        salt = llmNutritionResponse.salt,
                                        fibre = llmNutritionResponse.fibre
                                    )
                                )
                                setActionChatStage(ActionChatStage.RESPONSE_RECEIVED)
                                setLlmAwaitActionType(LlmActionType.NONE)
                                setUserActionRequest("")
                            }
                        }

                    },
                    onError = { errorMessage ->
                        viewModelScope.launch() {
                            // show the error and reset the ui
                            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(errorMessage))
                            setActionChatStage(ActionChatStage.USER_INPUT)
                            setLlmAwaitActionType(LlmActionType.NONE)
                        }
                    }
                )
            }
                .onFailure {
                    // error occurred
                    setActionChatStage(ActionChatStage.USER_INPUT) // error occurred - not awaiting.
                    setLlmAwaitActionType(LlmActionType.NONE)
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                        ErrorMessage.API_ERROR_SERVICE_UNREACHABLE)
                    )
                }
        }
    }

    /**
     * Format ingredient text for LLMIngredient, as they are dealt with differently.
     * @param ingredient - The ingredient to format.
     * @return String of formatted ingredient text.
     */
    fun formatIngredientText(
        ingredient: LlmIngredient
    ): String {
        runCatching {
            return recipeActions.formatIngredientText(ingredient)
        }
            .onFailure {
                viewModelScope.launch {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                        "The recipe could not be generated. Please try again.")
                    )
                    setActionChatStage(ActionChatStage.USER_INPUT)
                    return@launch
                }
            }
        return "" // invalid if reached here
    }

    /**
     * Adds a Recipe to the database.
     * @param llmRecipe - The LLMRecipe to add as a Recipe
     * @param alteration - Whether an alteration occurred.
     */
    fun addRecipe(llmRecipe: LlmRecipe, alteration: Boolean = false) {
        viewModelScope.launch() {
            runCatching {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == "") {
                    throw NullPointerException()
                } // user is null; throw to be handled appropriately
                // Add the recipe to the database:
                // don't scale ingredients if it is an LLM alteration, otherwise scale.
                recipeRepository.createRecipe(uid, llmRecipe, shouldNotScaleIngredients = alteration)
            }
                .onSuccess {
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The recipe has been saved."
                        )
                    )
                }

                .onFailure { e ->
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The recipe could not be saved. Please try again, or enter a different alteration."
                        )
                    )
                }
        }
    }

    /**
     * Replaces a recipe by deleting the previous and adding the new one.
     * @param originalRecipe - The recipe to delete
     * @param newRecipe - The new recipe to replace.
     */
    fun replaceRecipe(
        originalRecipe: Recipe,
        newRecipe: LlmRecipe
    ) {
        viewModelScope.launch() {
            runCatching {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == "") {
                    throw NullPointerException()
                } // user is null; throw to be handled appropriately
                // Add the recipe to the database:
                recipeRepository.replaceRecipe(
                    originalRecipe, newRecipe, originalRecipe.imagePath
                ) // original image is preserved
            }
                .onSuccess {
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The recipe has been updated."
                        )
                    )
                }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The recipe could not be updated. Please try again, or enter a different alteration."
                        )
                    )
                }
        }
    }

    /**
     * Deletes a recipe.
     */
    fun deleteRecipe() {
        viewModelScope.launch() {
            runCatching {
                // get and delete the recipe
                val uiRecipe = mutableUiState.value.selectedRecipe
                if (uiRecipe == null) {
                    return@launch
                }
                val recipe = recipeRepository.getRecipeById(uiRecipe.id).first()
                if (recipe == null) {
                    return@launch
                }
                recipeRepository.deleteRecipe(recipe) // delete
            }
                .onSuccess {
                    // when successfully deleted, update the id in the ui to -1 to trigger it to go
                    // back to the recipe all recipe book view
                    if (mutableUiState.value.selectedRecipe != null) {
                        setSelectedRecipe(
                            mutableUiState.value.selectedRecipe!!.copy(
                                id = -1
                            )
                        )
                    }
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The recipe has been deleted."
                        )
                    )
                }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "The recipe could not be deleted."
                        )
                    )
                }
        }
    }

}