package uk.co.imoqen.nourish.recipecreation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.ingredient.IngredientSheetAction
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.common.tag.TagCreationException
import uk.co.imoqen.nourish.common.tag.TagSheetAction
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.shoppinglist.UserInteractionStatus
import javax.inject.Inject

@HiltViewModel
class RecipeCreationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authenticationManager: AuthenticationManager,
    private val apiManager: ApiManager,
    private val uiEventBus: UiEventBus,
    private val recipeRepository: RecipeRepository,
    private val recipeActions: RecipeActions,
): ViewModel() {
    private val mutableUiState = MutableStateFlow(RecipeCreationUiState())
    val uiState: StateFlow<RecipeCreationUiState> = mutableUiState.asStateFlow()

    fun setTitle(title: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                title = title
            )
        }
    }

    fun setNumberOfServings(numberOfServings: Int) {
        mutableUiState.update { currentState ->
            currentState.copy(
                numberOfServings = numberOfServings
            )
        }
    }

    fun setRecipeType(recipeType: RecipeType?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeType = recipeType
            )
        }
    }

    fun setStorageGuidance(storageGuidance: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                storageGuidance = storageGuidance
            )
        }
    }

    fun setDisplayRecipeTypeSelector(displayRecipeTypeSelector: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                displayRecipeTypeSelector = displayRecipeTypeSelector
            )
        }
    }

    fun setIngredients(ingredients: List<LlmIngredient>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredients = ingredients
            )
        }
    }

    fun setRecipeTypes(recipeTypes: List<RecipeType>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeTypes = recipeTypes
            )
        }
    }

    fun setShowAddIngredientSheet(show: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showAddIngredientSheet = show
            )
        }
    }

    fun setFilterDisplayState(filterDisplayState: FilterState) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    filterDisplayState = filterDisplayState
                )
            )
        }
    }

    fun setRecipeTags(recipeTags: List<TagSelection>?) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    recipeTags = recipeTags
                )
            )
        }
    }

    fun setCreatedTagName(createdTagName: String) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    createdTagName = createdTagName
                )
            )
        }
    }

    fun setCreatedTagColour(createdTagColour: NourishColour?) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    createdTagColour = createdTagColour
                )
            )
        }
    }

    fun setTagCreationError(tagCreationError: String) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    tagCreationError = tagCreationError
                )
            )
        }
    }

    fun setShowTagConfirmationDialog(showTagConfirmationDialog: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    showTagConfirmationDialog = showTagConfirmationDialog
                )
            )
        }
    }

    fun setTagToDelete(tagToDelete: Tag?) {
        mutableUiState.update { currentState ->
            currentState.copy( // to update the sheet ui state, need to nest the copy
                tagSheetState = currentState.tagSheetState.copy (
                    tagToDelete = tagToDelete
                )
            )
        }
    }

    // Setters for the ingredient object
    fun setIngredientToAddName(name: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientToAdd = currentState.ingredientToAdd.copy(
                    name = name
                )
            )
        }
    }

    fun setIngredientToAddUnit(unit: IngredientUnit) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientToAdd = currentState.ingredientToAdd.copy(
                    unit = unit
                )
            )
        }
    }

    fun setIngredientToAddAmount(amount: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientToAdd = currentState.ingredientToAdd.copy(
                    amount = amount
                )
            )
        }
    }

    fun setIngredientToAddError(error: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientToAdd = currentState.ingredientToAdd.copy(
                    error = error
                )
            )
        }
    }

    // Editing Ingredient Index and Amount
    fun setEditIngredientIndex(index: Int) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientToEdit = currentState.ingredientToEdit.copy(
                    index = index
                )
            )
        }
    }
    fun setEditIngredientAmount(amount: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientToEdit = currentState.ingredientToEdit.copy(
                    amount = amount
                )
            )
        }
    }

    // Recipe Tag Expose methods
    fun getAllTagsForUser(
        reset: Boolean = false // to reset to get all tags as not selected; by default set to false
    ) {
        viewModelScope.launch() {
            (recipeActions.getAllTagsForUser(
                mutableUiState.value.tagSheetState.recipeTags, reset
            ))?.let { updatedTags ->
                setRecipeTags(updatedTags) // only sets if not null
            }
        }
    }

    // only sets if the update is not null.
    fun getAllRecipeTypes() {
        viewModelScope.launch() {
            (recipeActions.getAllRecipeTypes())?.let { updatedTypes ->
                setRecipeTypes(updatedTypes) // only sets if not null
            }
        }
    }

    fun updateSelected(tag: Tag, status: Boolean) {
        viewModelScope.launch() {
            setRecipeTags(
                recipeActions.updateSelected(tag, status, mutableUiState.value.tagSheetState.recipeTags)
            )
        }
    }

    fun createTag(
    ) {
        viewModelScope.launch() {
            runCatching {
                recipeActions.createTag(
                    mutableUiState.value.tagSheetState.createdTagName,
                    mutableUiState.value.tagSheetState.createdTagColour,
                    mutableUiState.value.tagSheetState.recipeTags)
            }
                .onSuccess {
                    // when the tag has been successfully made,
                    getAllTagsForUser() // update the tags as a new one now exists
                    closeTagCreation()
                }
                .onFailure { e ->
                    when (e) {
                        is TagCreationException -> setTagCreationError(
                            e.message ?: "The tag could not be created."
                        )
                        else -> setTagCreationError("The tag could not be created.")
                    }
                }
        }
    }

    fun deleteTag() {
        viewModelScope.launch() {
            runCatching {
                recipeActions.deleteTag(mutableUiState.value.tagSheetState.tagToDelete)
            }
                .onSuccess {
                    // after tag is deleted, refresh the available tags:
                    getAllTagsForUser()
                }

        }
    }

    // closes the tag creation area. Purely for updating the UI state and does not use any shared action.
    fun closeTagCreation() {
        setFilterDisplayState(FilterState.APPLY) // go back to apply filter menu
        setCreatedTagColour(null)
        setCreatedTagName("")
        setTagCreationError("")
    }

    // the actions for the tag sheet:
    fun onTagSheetAction(action: TagSheetAction) {
        when (action) {
            is TagSheetAction.SetFilterDisplayState -> setFilterDisplayState(action.displayState)
            is TagSheetAction.CloseTagCreation -> closeTagCreation()
            is TagSheetAction.CreateTag -> createTag()
            is TagSheetAction.DeleteTag -> deleteTag()
            is TagSheetAction.SetCreatedTagColour -> setCreatedTagColour(action.colour)
            is TagSheetAction.SetCreatedTagName -> setCreatedTagName(action.name)
            is TagSheetAction.SetTagToDelete -> setTagToDelete(action.tag)
            is TagSheetAction.ShowTagConfirmationDialog -> setShowTagConfirmationDialog(action.show)
            is TagSheetAction.UpdateSelected -> updateSelected(action.tag, action.selected)
        }
    }

    // for updating the number of servings of a recipe
    // much less complex than for a recipe card, as this doesnt scale the ingredients. its simply what the user enters.
    fun alterServingSize(
        increase: Boolean
    ) {
        if (increase) {
            setNumberOfServings(mutableUiState.value.numberOfServings+1)
        } else {
            // ensure it doesnt go negative or 0
            setNumberOfServings(
                if (mutableUiState.value.numberOfServings > 1) {
                    mutableUiState.value.numberOfServings - 1
                } else {
                    mutableUiState.value.numberOfServings
                }
            )
        }
    }

    // design choice: ingredients, if the same, will not be merged. this may be for if a user wants to
    // separate amounts (e.g. 5g butter for the batter, 5g of butter for frying) etc.
    fun addIngredientToRecipe() {
        viewModelScope.launch() {
            runCatching {
                // validate
                val amount = mutableUiState.value.ingredientToAdd.amount.replace(',', '.').toDoubleOrNull()
                val name = mutableUiState.value.ingredientToAdd.name.trim()
                val unit = mutableUiState.value.ingredientToAdd.unit
                if (name == "") {
                    setIngredientToAddError("The name cannot be blank.")
                    return@launch
                }
                if (amount == null) {
                    setIngredientToAddError("The amount cannot be blank.")
                    return@launch
                }
                // then simply update the ingredients list such that it is populated correctly (with name in the plural field) for use in recipe repo createRecipe()
                val ingredient = LlmIngredient(
                    ingredientAmount = amount, // if user inputted invalid, it just gets set to 1.0
                    ingredientPlural = name,
                    ingredientAmountUnit = unit.name, // will always be safe as user must select, all options are ok.
                    ingredientSingular = "" // user inputted ingredients value is always set to singular.
                )
                setIngredients(
                    // adds the ingredient or makes a new list with the ingredient if it is empty
                    (mutableUiState.value.ingredients ?: emptyList()) + ingredient
                )
            }
                .onSuccess {
                    resetIngredientAdditionUI() // close and reset the sheet
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Ingredient added to recipe."))
                }
                .onFailure {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Ingredient could not be added."))
                }
        }
        //todo. make sure that ingredient amount is not optional
    }

    fun resetIngredientAdditionUI() {
        setIngredientToAddAmount("")
        setIngredientToAddUnit(IngredientUnit.UNIT)
        setIngredientToAddName("")
        setShowAddIngredientSheet(false)
        setIngredientToAddError("")
    }

    // the actions for the ingredient addition sheet;
    fun onIngredientSheetAction(action: IngredientSheetAction) {
        when (action) {
            is IngredientSheetAction.OnDismissRequest -> { setShowAddIngredientSheet(false) }
            is IngredientSheetAction.OnComplete -> { addIngredientToRecipe() }
            is IngredientSheetAction.SetIngredientAmount -> { setIngredientToAddAmount(action.amount) }
            is IngredientSheetAction.SetIngredientName -> { setIngredientToAddName(action.name) }
            is IngredientSheetAction.SetIngredientUnit -> { setIngredientToAddUnit(action.unit) }
        }
    }

    // action for persisting an ingredient amount update:
    fun updateIngredientAmount() {
        viewModelScope.launch() {
            runCatching {
                val ingredients = mutableUiState.value.ingredients
                val index = mutableUiState.value.ingredientToEdit.index
                if (ingredients == null) {
                    return@launch
                }
                val ingredient = ingredients[index]
                val amount = mutableUiState.value.ingredientToEdit.amount.replace(',','.').toDoubleOrNull() ?: 1.0 // 1.0 if the input is invalid.
                // update the ingredients list:
                setIngredients(
                    ingredients.toMutableList().apply {
                        this[index] = this[index].copy(
                            ingredientAmount = amount
                        )
                    }
                )

                // reset in UI:
                setEditIngredientIndex(-1)
                setEditIngredientAmount("")
            } .onFailure {
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Couldn't update the amount. Please try again later."
                ))
            }
        }
    }

    // exposing the recipe actions format method
    fun formatIngredientAmount(
        ingredientAmount: Double,
        roundToNearestQuarter: Boolean = true
    ): String {
        var amount = ingredientAmount
        if (amount < 0.01) {
            amount = 0.01 // cant go less than 0.01 in recipe creation - has a lower threshold than the others.
        }
        return recipeActions.formatIngredientAmount(amount, roundToNearestQuarter)
    }

}