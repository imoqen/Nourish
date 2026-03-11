package uk.co.imoqen.nourish.recipebook

import android.util.Log
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.collectAsState
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
import uk.co.imoqen.nourish.common.recipeColour
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
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
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.db.tag.TagRepository
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.network.response.LlmRecipe
import uk.co.imoqen.nourish.recipediscovery.LlmChatStage
import uk.co.imoqen.nourish.recipediscovery.LlmMode
import java.io.File
import java.lang.NullPointerException
import javax.inject.Inject
import kotlin.collections.getOrPut

@HiltViewModel
class RecipeBookViewModel @Inject constructor(
    private val apiService: ApiService,
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

    fun setIngredientsToAddToShoppingList(ingredientsToAddToShoppingList: MutableList<Pair<ShoppingListIngredient, Boolean>>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientsToAddToShoppingList = ingredientsToAddToShoppingList
            )
        }
    }

    fun alterIngredientShoppingListStatus(
        ingredientToAddToShoppingList: ShoppingListIngredient,
        index: Int, // safe to use index because it wont change after it is populated. for safety, done in runcatching
        selected: Boolean
    ) {
        viewModelScope.launch() {
            runCatching {
                // IMPORTANT NOTe for future: toMutableList() solves composition problems by creating a NEW list.
                val updatedList = mutableUiState.value.ingredientsToAddToShoppingList?.toMutableList().apply {
                    this?.set(index, Pair(ingredientToAddToShoppingList, selected))
                }
                setIngredientsToAddToShoppingList(updatedList)
            }
                .onFailure {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Cannot edit ingredient status."))
                    Log.i("ingredient status change", "failed")
                }
        }
    }

    fun formatIngredientsForShoppingList() {
        viewModelScope.launch() {
            runCatching {
                // get the recipe:
                val recipe = mutableUiState.value.selectedRecipe
                if (recipe == null) {
                    return@launch
                }

                // PREPROCESSING
                // first pass of the list: add up all same ingredients so that we have totals for each ingredient + combine them if needed
                // e.g. if multiple ingredients appear in different sections. we dont want to add them to the shopping list twice.
                val shoppingListIngredients: MutableList<ShoppingListIngredient> = mutableListOf()
                val recipeIngredients =
                    recipeRepository.getRecipeWithRecipeIngredients(recipe.id).first().ingredients

                recipeIngredients.forEach { recipeIngredient ->
                    val ingredient =
                        ingredientRepository.getById(recipeIngredient.ingredientId).first()
                    if (ingredient == null) {
                        return@launch // error
                    }

                    //TODO if the ingredient amount is very small like tsp, then should simply store this as unit AND
                    // ignore the amount. as only should need to ever buy one ofthese (e..g tsp salt -> salt.) so set amount to 1g always if tsp. and dont let them add up.
                    // potentially do same for very small g values too etc e.g. 5g salt -> just salt. no one is going to buy just 5g of salt
                    // lets say if the amount is less than 15g then just say buy the item itself.

                    val unit = recipeIngredient.ingredientUnit
                    val amount = recipeIngredient.amount

                    val existingIngredientIndex =
                        (shoppingListIngredients.indexOfFirst {
                            it.ingredientId == recipeIngredient.ingredientId && it.unit == unit
                        })

                    if (existingIngredientIndex != -1) {// check if the ingredient is already in the list and only update it if the amount needs to change (i.e. not tsp etc)
                        val existingIngredient = shoppingListIngredients[existingIngredientIndex]
                        // if it is, then alter the existing list entry
                        // stays same but amount just gets changed
                        val updatedIngredient =
                                existingIngredient.copy(
                                    // sets amount to added amount if  unit is large, otherwise just remains as 1.
                                    amount = if (unit.keepUnitInShoppingList) { existingIngredient.amount + recipeIngredient.amount } else { amount }
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

                // THEN go through the array containing no duplicates and add them to the selection list

                // declare the list for storing the selections after preprocessing:
                val shoppingListIngredientSelections: MutableList<Pair<ShoppingListIngredient, Boolean>> = mutableListOf()

                shoppingListIngredients.forEach { shoppingListIngredient ->
                    val ingredient =
                        ingredientRepository.getById(shoppingListIngredient.ingredientId).first()

                    if (ingredient == null) {
                        return@launch // error
                    }

                    //TODO if the ingredient amount is very small like tsp, then should simply store this as unit AND
                    // ignore the amount. as only should need to ever buy one ofthese (e..g tsp salt -> salt.) so set amount to 1g always if tsp. and dont let them add up.
                    // potentially do same for very small g values too etc e.g. 5g salt -> just salt. no one is going to buy just 5g of salt
                    // lets say if the amount is less than 15g then just say buy the item itself.

                    var unit = shoppingListIngredient.unit
                    var amount = shoppingListIngredient.amount
                    if (!unit.keepUnitInShoppingList || (unit != IngredientUnit.UNIT && amount <= 15)) { // if the unit shouldnt be kept, or the amount is inconsiderable (less than 15) then just set to buy 1.
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
                .onFailure {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Ingredients cannot be added. Please try another recipe, or add manually."))
                    Log.i("ingredient add display", "failed")
                }
        }
    }

    fun addIngredientsToShoppingList() {
        viewModelScope.launch() {
            runCatching {

                // get all the ingredients:
                val shoppingListIngredients = mutableUiState.value.ingredientsToAddToShoppingList

                // get uid:
                val uid = authenticationManager.getUid()
                if (uid == null) {
                    return@launch
                }

                // go through them all and add the ones to the db which should be added:
                shoppingListIngredients?.forEach { (ingredient, selected) ->
                    if (selected) {
                        // add to db and account for if the ingredient is already in the db.
                        // this is handled in the repository.
                        shoppingListEntryRepository.addShoppingListEntry(ingredient, uid, userAdded=false)
                    }
                }

            }
                .onFailure {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Ingredients cannot be added. Please try another recipe, or add manually."))
                    Log.i("ingredient add to shopping list", "failed")
                }
        }
    }

    // gets all recipes for the currently logged in user. ensures that if user not logged in, empty list returned.
    fun getAllRecipes(): Flow<List<Recipe>> {
        val uid = authenticationManager.getUid()
        if (uid == null) return flowOf(emptyList())
        return recipeRepository.getAllRecipes(uid)
    }

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

    fun getRecipeById(recipeId: Long): Flow<Recipe?> {
        return recipeRepository.getRecipeById(recipeId)
    }

    fun getSectionById(sectionId: Long): Flow<RecipeIngredientSection?> {
        return recipeRepository.getRecipeSectionById(sectionId)
    }

    fun getRecipeStepsForRecipe(recipeId: Long): Flow<RecipeWithRecipeSteps> {
        return recipeRepository.getRecipeWithRecipeSteps(recipeId)
    }

    // RECIPE NUTRITION LOGIC:
    fun getNutritionForRecipe(recipeId: Long): Flow<RecipeNutrition?> {
        return recipeRepository.getNutritionForRecipe(recipeId)
    }

    // RECIPE QUERY LOGIC:
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
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("The step could not be marked as complete. Please try again later."))
                    Log.i("step check", "failed: $e")
                }
        }
    }

    fun buildRecipeCard() {
        val recipe = mutableUiState.value.selectedRecipe
        viewModelScope.launch() {
            if (recipe == null) {
                return@launch
            }
            runCatching {
                val title = recipe.title
                val ingredientsMap =
                    buildIngredientsMap(getRecipeIngredientsForRecipe(recipe.id).first())
                val method = buildRecipeSteps(recipe.id)
                val serves = recipe.numberOfServings
                val recipeType = findRecipeType(recipe.recipeTypeId)
                val recipeTags = buildRecipeTags(recipe.id)
                val storageInstructions = recipe.storageInstructions

                if (ingredientsMap == null || recipeType == null) {
                    Log.i("recipe book error", "null")
                    return@launch
                }

                val recipeCard = RecipeCardExpanded(
                    title, ingredientsMap, method, serves, recipeType,
                    recipeTags, storageInstructions, recipe.imagePath, recipe.emoji,
                    enumValueOf<NourishColour>(recipe.colour.uppercase())
                )

                setRecipeCard(recipeCard)
            }
                .onSuccess {
                    Log.i("recipe book", "successfully parsed into recipe card")
                }
                .onFailure { e ->
                    Log.i("recipe book", "failed: $e")
                }
        }
    }

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

    suspend fun buildRecipeSteps(
        recipeId: Long
    ): MutableList<Triple<String, Boolean, Long>> {
        val recipeStepsList: MutableList<Triple<String, Boolean, Long>> = mutableListOf()
        getRecipeStepsForRecipe(recipeId).first().recipeSteps.forEach{ step ->
            recipeStepsList.add(Triple(step.instruction, step.complete, step.id))
        }
        return recipeStepsList
    }

    suspend fun findRecipeType(
        recipeTypeId: Long
    ): Pair<String, Int>? {
        val recipeType = getRecipeTypeById(recipeTypeId).first()
        if (recipeType == null) {
            return recipeType
        }
        return Pair(recipeType.title, recipeType.ordinal)
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
            val unit = if (recipeActions.determineUnitPlurality(ingredientAmount)) {
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

    @OptIn(FlowPreview::class)
    fun getRecipeBookMap() {
        viewModelScope.launch() {
            runCatching {
                Log.i("all recipes", "getting all")
                val allRecipes = getAllRecipes().debounce(300).first() // get all recipes. debouncing is added to provide smoothness
                // Filter to search term if search has been made:
                /*if (mutableUiState.value.searchTerm != "") {
                    allRecipes = allRecipes.filter { recipe ->
                        recipe.title.contains(mutableUiState.value.searchTerm, true) // ignore case
                    }
                }*/
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
                val sorted = recipeBookMap.toSortedMap(compareBy { it.ordinal }) // sort by ordinal values of the recipeType.
                setRecipeBookMap(sorted)
                Log.i("sorted", "done")
            }
                .onSuccess {  }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Couldn't fetch recipes. Please try again later."))
                    Log.i("recipe book", "failed: $e")
                }
        }
    }

    fun alterServingSize(
        increase: Boolean // true = increase by 1, false = decrease by 1. cap at 1.
    ) {
        viewModelScope.launch() {
            val recipe = mutableUiState.value.selectedRecipe
            if (recipe == null) {
                Log.i("Serving", "Cannot update serving size of a null recipe.")
                return@launch
            }

            val currentServingSize = recipe.numberOfServings
            if (currentServingSize == 1 && !increase) {
                Log.i("Serving", "Cannot decrease serving size from 1.")
                return@launch
            }

            // scale the recipe and save to db, reverting back if failed:
            recipeRepository.scaleRecipe(recipe, currentServingSize, increase)
        }
    }

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
                Log.i("setting types", "not null: ${updatedTypes}")
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

    // recipe actions
    fun formatIngredientAmount(
        ingredientAmount: Double
    ): String {
        return recipeActions.formatIngredientAmount(ingredientAmount)
    }

    // api

    suspend fun parseRecipeToLlmRecipe(
        recipe: Recipe
    ): LlmRecipe {
        val recipeIngredients = getRecipeIngredientsForRecipe(recipe.id).first()
        val recipeType = findRecipeType(recipe.recipeTypeId)?.first ?: ""
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
                ingredientAmount = recipeIngredient.amount,
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
            servings = recipe.numberOfServings,
            mealType = recipeType,
            method = method,
            tags = tags,
            storage = recipe.storageInstructions,
            recipeNotPossible = false,
            emoji = recipe.emoji,
            colour = recipe.colour
        )
    }

    fun makeLlmApiCall(
        type: LlmActionType,
    ) {
        setActionChatStage(ActionChatStage.AWAITING_LLM_RESPONSE)
        viewModelScope.launch() {
            runCatching {
                val userPrompt = mutableUiState.value.userActionRequest
                val recipe = mutableUiState.value.selectedRecipe

                // validation
                if (userPrompt == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("The input must not be blank to proceed."))
                    return@launch
                }
                if (recipe == null) {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("A recipe must be selected to proceed."))
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
                var allNutritionalFilters = mutableUiState.value.recipeTags?.joinToString(", ") { it.tag.identifier }
                if (allNutritionalFilters == null) {
                    allNutritionalFilters = "[]" // signifies empty list
                }
                // make it into a request:
                val request = LlmActionRequest(
                    parseRecipeToLlmRecipe(recipe), userPrompt, allNutritionalFilters, dishType
                )

                // validate the user's token:
                val tokenResult = authenticationManager.getUserToken()
                if (tokenResult == null) {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                    // error occurred; go back to the USER_INPUT stage (MAY NOT WORK: TEST THIS).
                    return@launch
                }

                apiManager.makeRetrofitLlmActionCall(
                    type,
                    request,
                    tokenResult,
                    onQuestionAnswered = { llmQuestionResponse ->
                        val llmAnswer = llmQuestionResponse?.answer
                        if (llmAnswer != null) {
                            Log.i("LLM Answer", llmAnswer)
                            viewModelScope.launch() {
                                // add the answer to the DB and display to the user in the chat UI
                                val query = recipeRepository.getRecipeQueryById(queryId).first()
                                if (query != null) {
                                    recipeRepository.updateRecipeQuery(query.copy(llmAnswer = llmAnswer))
                                }
                                setActionChatStage(ActionChatStage.RESPONSE_RECEIVED)
                                setUserActionRequest("") // so the text doesnt still appear in their send bar
                            }
                        }

                    },
                    onRecipeAltered = { llmRecipe ->
                        if (llmRecipe != null) {
                            viewModelScope.launch() {
                                setActionChatStage(ActionChatStage.RESPONSE_RECEIVED)
                                if (llmRecipe.recipeNotPossible) {
                                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Valid recipe could not be generated. Please enter something different."))
                                    return@launch
                                }
                                // display the recipe to the user. there will be a button to overwrite the exising recipe, or save as a new recipe. when clicked, this will do the according process.
                                setAlteredRecipe(llmRecipe)
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
                                        salt = llmNutritionResponse.salt
                                    )
                                )
                                setActionChatStage(ActionChatStage.RESPONSE_RECEIVED)
                            }
                        }

                    }
                )
            }
                .onFailure {
                    // error occurred
                    setActionChatStage(ActionChatStage.USER_INPUT) // no longer awaiting - error occurred.
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.API_ERROR_SERVICE_UNREACHABLE))
                }
        }
    }

    // only used for llmingredient as they are dealt with differently.
    fun formatIngredientText(
        ingredient: LlmIngredient
    ): String {
        runCatching {
            return recipeActions.formatIngredientText(ingredient)
        }
            .onFailure {
                viewModelScope.launch {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("The recipe could not be generated. Please try again."))
                    setActionChatStage(ActionChatStage.USER_INPUT)
                    return@launch
                }
            }
        return "" // invalid if reached here
    }

    fun addRecipe(llmRecipe: LlmRecipe) {
        viewModelScope.launch() {
            runCatching {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == null) {
                    throw NullPointerException()
                } // user is null; throw to be handled appropriately
                // Add the recipe to the database:
                recipeRepository.createRecipe(uid, llmRecipe)
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

    fun replaceRecipe(
        originalRecipe: Recipe,
        newRecipe: LlmRecipe
    ) {
        viewModelScope.launch() {
            runCatching {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == null) {
                    throw NullPointerException()
                } // user is null; throw to be handled appropriately
                // Add the recipe to the database:
                recipeRepository.replaceRecipe(originalRecipe, newRecipe)
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

}