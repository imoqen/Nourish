package uk.co.imoqen.nourish.recipecreation

import android.content.Context
import android.net.Uri
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
import uk.co.imoqen.nourish.common.exception.RecipeCreationException
import uk.co.imoqen.nourish.common.ingredient.IngredientSheetAction
import uk.co.imoqen.nourish.common.saveMipmap
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.db.tag.Tag
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.common.exception.TagCreationException
import uk.co.imoqen.nourish.common.tag.TagSheetAction
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.tag.TagRepository
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.network.response.LlmIngredientSection
import uk.co.imoqen.nourish.network.response.LlmRecipe
import java.io.File
import java.lang.NullPointerException
import java.util.UUID
import javax.inject.Inject
import kotlin.math.round

/**
 * The ViewModel for RecipeCreation related logic. Utilised in the View to handle recipe creation
 * and editing.
 * @param authenticationManager - The AuthenticationManager to get the user's uid.
 * @param uiEventBus - The UiEventBus to display alerts / Snackbar messages.
 * @param recipeRepository - The Recipe Repository, to access the Recipe db table.
 * @param recipeActions - The Auxiliary class RecipeActions to access and use these common Recipe actions.
 * @param tagRepository - The Tag Repository, to access the Tag db table.
 * @param ingredientRepository - The Ingredient Repository, to access the Ingredient db table.
 *
 * @author Imogen Pemberton, 2026
 */
@HiltViewModel
class RecipeCreationViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager,
    private val uiEventBus: UiEventBus,
    private val recipeRepository: RecipeRepository,
    private val recipeActions: RecipeActions,
    private val tagRepository: TagRepository,
    private val ingredientRepository: IngredientRepository
): ViewModel() {
    private val mutableUiState = MutableStateFlow(RecipeCreationUiState())
    val uiState: StateFlow<RecipeCreationUiState> = mutableUiState.asStateFlow()

    // Setters for UiState:

    // To go back:
    fun setShowBackConfirmation(showBackConfirmation: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showBackConfirmation = showBackConfirmation
            )
        }
    }

    // For editing:
    fun setCreationMode(creationMode: CreationMode) {
        mutableUiState.update { currentState ->
            currentState.copy(
                creationMode = creationMode
            )
        }
    }
    fun setRecipeEditId(recipeEditId: Long) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeEditId = recipeEditId
            )
        }
    }

    // To go back if recipe created
    fun setRecipeCreated(recipeCreated: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeCreated = recipeCreated
            )
        }
    }

    // Error for recipe creation
    fun setRecipeCreationError(recipeCreationError: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeCreationError = recipeCreationError
            )
        }
    }

    // Display (image, colour, and emoji):
    fun setImageFile(imageFile: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                imageFile = imageFile
            )
        }
    }

    fun setEmoji(emoji: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                emoji = emoji
            )
        }
    }
    fun setShowEmojiSelector(showEmojiSelector: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showEmojiSelector = showEmojiSelector
            )
        }
    }

    fun setColour(colour: NourishColour) {
        mutableUiState.update { currentState ->
            currentState.copy(
                colour = colour
            )
        }
    }
    fun setShowColourSelector(showColourSelector: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showColourSelector = showColourSelector
            )
        }
    }

    // Method setters:
    fun setMethod(method: List<RecipeStep>) {
        mutableUiState.update { currentState ->
            currentState.copy(
                method = method
            )
        }
    }
    fun setShowAddMethodSheet(showAddMethodSheet: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showAddMethodSheet = showAddMethodSheet
            )
        }
    }
    fun setStepToAdd(stepToAdd: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                stepToAdd = stepToAdd
            )
        }
    }
    fun setStepToEdit(stepToEdit: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                stepToEdit = stepToEdit
            )
        }
    }
    fun setStepIndexToEdit(stepIndexToEdit: Int) {
        mutableUiState.update { currentState ->
            currentState.copy(
                stepIndexToEdit = stepIndexToEdit
            )
        }
    }
    fun setStepIndexToDelete(stepIndexToDelete: Int) {
        mutableUiState.update { currentState ->
            currentState.copy(
                stepIndexToDelete = stepIndexToDelete
            )
        }
    }

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

    fun setIngredientIndexToDelete(indexToDelete: Int) {
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientIndexToDelete = indexToDelete
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

    /**
     * Updates which Tags are currently selected, by calling RecipeActions.
     * @param tag - The Tag to update;
     * @param status - whether the Tag is selected or not, to be updated.
     */
    fun updateSelected(tag: Tag, status: Boolean) {
        viewModelScope.launch() {
            setRecipeTags(
                recipeActions.updateSelected(tag, status, mutableUiState.value.tagSheetState.recipeTags)
            )
        }
    }

    /**
     * Creates a nutritional Tag, by calling RecipeActions.
     */
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

    /**
     * Deletes a nutritional Tag, by calling RecipeActions.
     */
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

    /**
     * Closes the tag creation area. Purely for updating the UI state - does not use any shared
     * RecipeAction.
     */
    fun closeTagCreation() {
        setFilterDisplayState(FilterState.APPLY) // go back to apply filter menu
        setCreatedTagColour(null)
        setCreatedTagName("")
        setTagCreationError("")
    }

    /**
     * Organises the actions for the Tag sheet, forwarding them to their relevant handler.
     * @param action - the TagSheetAction to handle.
     */
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

    /**
     * To update the number of servings of a recipe.
     * Much less complex than for a recipe card, as this doesn't scale the ingredients -
     * it's simply what the user enters.
     * @param increase - Whether to increase (true) or decrease (false) the serving size.
     */
    fun alterServingSize(
        increase: Boolean
    ) {
        if (increase) {
            setNumberOfServings(mutableUiState.value.numberOfServings+1)
        } else {
            // ensure it doesn't go negative or 0
            setNumberOfServings(
                if (mutableUiState.value.numberOfServings > 1) {
                    mutableUiState.value.numberOfServings - 1
                } else {
                    mutableUiState.value.numberOfServings
                }
            )
        }
    }

    /**
     * Adds a user-inputted Ingredient to a Recipe.
     * Design choice: ingredients, if the same, will not be merged. this may be for if a user wants
     *                to separate amounts (e.g. 5g butter for the batter, 5g of butter for frying) etc.
     *                So they will be kept separate.
     */
    fun addIngredientToRecipe() {
        viewModelScope.launch() {
            runCatching {
                // Validate:
                val amount = mutableUiState.value.ingredientToAdd.amount.replace(',', '.').toDoubleOrNull()
                val name = mutableUiState.value.ingredientToAdd.name.trim()
                val unit = mutableUiState.value.ingredientToAdd.unit
                if (name.trim() == "") {
                    setIngredientToAddError("The name cannot be blank.")
                    return@launch
                }
                if (amount == null) {
                    setIngredientToAddError("The amount cannot be blank.")
                    return@launch
                }
                // then simply update the ingredients list such that it is populated correctly
                // (with name in the plural field) for use in recipe repo createRecipe()
                val ingredient = LlmIngredient(
                    ingredientAmount = amount, // if user inputted invalid, it just gets set to 1.0
                    ingredientPlural = name,
                    ingredientAmountUnit = unit.name, // will always be safe as user must select
                    // user inputted ingredients value is always set to the same as plural so that
                    // user does not need to enter both singular and plural.
                    ingredientSingular = name
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
    }

    /**
     * Resets the UI after Ingredient creation is complete.
     */
    fun resetIngredientAdditionUI() {
        setIngredientToAddAmount("")
        setIngredientToAddUnit(IngredientUnit.UNIT)
        setIngredientToAddName("")
        setShowAddIngredientSheet(false)
        setIngredientToAddError("")
    }

    /**
     * Organises the actions for the Ingredient sheet, forwarding them to their relevant handler.
     * @param action - the IngredientSheetAction to handle.
     */
    fun onIngredientSheetAction(action: IngredientSheetAction) {
        when (action) {
            is IngredientSheetAction.OnDismissRequest -> { setShowAddIngredientSheet(false) }
            is IngredientSheetAction.OnComplete -> { addIngredientToRecipe() }
            is IngredientSheetAction.SetIngredientAmount -> { setIngredientToAddAmount(action.amount) }
            is IngredientSheetAction.SetIngredientName -> { setIngredientToAddName(action.name) }
            is IngredientSheetAction.SetIngredientUnit -> { setIngredientToAddUnit(action.unit) }
        }
    }

    /**
     * Persists an ingredient amount update.
     */
    fun updateIngredientAmount() {
        viewModelScope.launch() {
            runCatching {
                val ingredients = mutableUiState.value.ingredients
                val index = mutableUiState.value.ingredientToEdit.index
                if (ingredients == null) {
                    return@launch
                }
                val amount = mutableUiState.value.ingredientToEdit.amount
                    .replace(',','.').toDoubleOrNull() ?: 1.0 // 1.0 if the input is invalid.
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

    /**
     * Exposes the formatting method from RecipeActions:
     */
    fun formatIngredientAmount(
        ingredientAmount: Double,
        roundToNearestQuarter: Boolean = true
    ): String {
        return recipeActions.formatIngredientAmount(ingredientAmount, roundToNearestQuarter)
    }

    /**
     * Deletes an Ingredient from the database.
     */
    fun deleteIngredient() {
        viewModelScope.launch() {
            runCatching {
                val ingredients = mutableUiState.value.ingredients
                val index = mutableUiState.value.ingredientIndexToDelete
                if (ingredients == null) {
                    return@launch
                }
                // Update the ingredients list by removing the ingredient
                setIngredients(
                    ingredients.toMutableList().apply{ removeAt(index) }
                )
                // Then reset in the ui
                setIngredientIndexToDelete(-1)
            } .onSuccess {
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Ingredient removed."
                ))
            }
                .onFailure { e->
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Couldn't remove the ingredient. Please try again later."
                ))
            }
        }
    }

    /**
     * Adds a RecipeStep to the database.
     */
    fun addRecipeStep() {
        viewModelScope.launch() {
            runCatching {
                val step = mutableUiState.value.stepToAdd.trim()
                if (step.trim() == "") {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                        "The step cannot be blank."
                    ))
                    return@launch
                }

                setMethod(
                    // Adds the step and makes an empty list with the step if the method has not
                    // yet been initialised (null)
                    mutableUiState.value.method?.toMutableList().apply {
                        this?.add(RecipeStep(step, UUID.randomUUID()))
                    } ?: listOf(RecipeStep(step, UUID.randomUUID()))
                )
            }
                .onSuccess {
                    // After it is successfully added, clear the ui of any of the addition
                    resetRecipeStepAddition()
                }
        }
    }

    /**
     * Resets the UI after RecipeStep creation is complete.
     */
    fun resetRecipeStepAddition() {
        setShowAddMethodSheet(false)
        setStepToAdd("")
    }

    /**
     * Updates a RecipeStep; this can be either its order in the list, or its step description.
     */
    fun updateStep() {
        viewModelScope.launch() {
            runCatching {
                val newStep = mutableUiState.value.stepToEdit.trim()
                val index = mutableUiState.value.stepIndexToEdit
                val method = mutableUiState.value.method
                if (newStep.trim() == "") {
                    return@launch
                }
                if (method == null) {
                    return@launch
                }
                // Update the steps list:
                setMethod(
                    method.toMutableList().apply{
                        this[index] = this[index].copy(
                            step = newStep
                        )
                    }
                )
                // reset in UI:
                setStepIndexToEdit(-1)
                setStepToEdit("")
            } .onFailure {
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Couldn't update the step. Please try again later."
                ))
            }
        }
    }

    /**
     * Deletes a RecipeStep from the database.
     */
    fun deleteStep() {
        viewModelScope.launch() {
            runCatching {
                val index = mutableUiState.value.stepIndexToDelete
                val method = mutableUiState.value.method
                if (method == null) {
                    return@launch
                }
                // Update the RecipeSteps list, by removing the RecipeStep
                setMethod(
                    method.toMutableList().apply{ removeAt(index) }
                )
                // Then reset in the ui
                setStepIndexToDelete(-1)
                setStepIndexToEdit(-1)
            } .onSuccess {
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Step removed."
                ))
            }
                .onFailure { e->
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                        "Couldn't remove the step. Please try again later."
                    ))
                }
        }
    }

    /**
     * Displays an image in the RecipeCreation view.
     * @param uri - The uri of the image to display.
     * @param context - The local context, required to load the image.
     */
    fun handleImage(
        uri: Uri?,
        context: Context
    ) {
        viewModelScope.launch() {
            runCatching {
                saveMipmap(
                    uri,
                    context
                ) { _, highQualityFilename -> // only using high quality here
                    setImageFile(highQualityFilename)
                }
            }
        }

    }

    /**
     * Adds the user-created Recipe to the database, using RecipeRepository.
     */
    fun addRecipe() {
        viewModelScope.launch() {
            runCatching {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == "") {
                    throw RecipeCreationException("You must be logged in.")
                } // user is null; throw to be handled appropriately
                val llmRecipe = parseIntoLlmRecipe()
                // Add the user created recipe to the database:
                // ingredients are not scaled here, because they are scaled in parseToLlmRecipe
                // instead. as with editing ingredients of a recipe.
                recipeRepository.createRecipe(
                    // recipe is saved with the higher quality image, which is stored in second place
                    uid, llmRecipe, imagePath = mutableUiState.value.imageFile,
                    // not an alteration; it is a new recipe. and it is NOT llm generated.
                    shouldNotScaleIngredients = true, llmGenerated = false
                )
            }
                .onSuccess {
                    setRecipeCreated(true)
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "Your recipe has been saved successfully."
                        )
                    )
                }
                .onFailure { e ->
                    when (e) {
                        is RecipeCreationException -> {
                            setRecipeCreationError(e.message ?: "Could not save recipe.")
                        }
                    }
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "Your recipe could not be saved."
                        )
                    )
                }
        }
    }

    /**
     * Recipe editing:
     * Updates a Recipe, by deleting the old Recipe and replacing it with the new one.
     * @param context - The local context, required to delete the Recipe's image file from local storage.
     */
    fun updateRecipe(
        context: Context // required for image deletion
    ) {
        viewModelScope.launch() {
            runCatching {
                // Check that user is logged in validly
                val uid = authenticationManager.getUid()
                if (uid == "") {
                    throw NullPointerException()
                } // user is null; throw to be handled appropriately

                val originalRecipe = recipeRepository.getRecipeById(
                    mutableUiState.value.recipeEditId
                ).first()
                if (originalRecipe == null) {
                    return@launch
                }
                val updatedRecipe = parseIntoLlmRecipe()

                // check if the image should be deleted or not:
                if (originalRecipe.imagePath != "" && mutableUiState.value.imageFile == "") {
                    File(
                        context.filesDir,
                        originalRecipe.imagePath // delete the image file
                    ).delete()
                }

                // Add the recipe to the database:
                recipeRepository.replaceRecipe(
                    originalRecipe,
                    updatedRecipe,
                    mutableUiState.value.imageFile,
                    llmGenerated=false // now non-LLM generated, because user edited it.
                )
            }
                .onSuccess {
                    setRecipeCreated(true) // so the user gets forced back
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "Your recipe has been updated."
                        )
                    )
                }
                .onFailure { e ->
                    when (e) {
                        is RecipeCreationException -> {
                            setRecipeCreationError(e.message ?: "Could not update recipe.")
                        }
                    }
                    uiEventBus.enqueueEvent(
                        UiEvent.ShowSnackbar(
                            "Your recipe could not be updated."
                        )
                    )
                }
        }
    }

    /**
     * Validates the Recipe Creation / Edit fields which are not optional, to ensure they are present.
     * @param title - The user-inputted title of the Recipe to be created.
     * @param ingredients - The user-inputted ingredients list of the Recipe to be created.
     * @param recipeType - The user-inputted Recipe Type of the Recipe to be created.
     * @param title - The user-inputted method of the Recipe to be created.
     */
    fun validateRecipeRequiredFields(
        title: String,
        ingredients: List<LlmIngredient>?,
        recipeType: RecipeType?,
        method: List<RecipeStep>?
    ) {
        if (title.trim() == "") {
            throw RecipeCreationException("You must enter a title.")
        }
        if (title.length > 54) {
            throw RecipeCreationException("The title is too long. It must be less than 54 characters.")
        }
        if (recipeType == null) {
            throw RecipeCreationException("You must select a recipe type.")
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw RecipeCreationException("You must enter at least 1 ingredient.")
        }
        if (method == null || method.isEmpty()) {
            throw RecipeCreationException("You must enter at least 1 method step.")
        }
    }

    /**
     * Parses a user-inputted Recipe into an LlmRecipe.
     * This is so that actions can be performed on the Recipe.
     */
    fun parseIntoLlmRecipe(): LlmRecipe {
        val ingredients = mutableUiState.value.ingredients
        val recipeType = mutableUiState.value.recipeType
        val method = mutableUiState.value.method
        val title = mutableUiState.value.title

        // validate that the required fields have been filled:
        validateRecipeRequiredFields(title, ingredients, recipeType, method)

        val servings = mutableUiState.value.numberOfServings
        return LlmRecipe(
            title = mutableUiState.value.title,
            sectionIngredients = listOf(LlmIngredientSection(
                "For the recipe",
                ingredients!!.map {
                    it.copy(ingredientAmount = (it.ingredientAmount / servings))
                })),
            servings = servings,
            mealType = recipeType!!.title,
            method = method!!.map { it.step }, // only get the steps, remove the uuids
            // finds all selected tags, then gets their identifiers. tags are not mandatory so ""
            // is ok if it is null:
            tags = mutableUiState.value.tagSheetState.recipeTags?.filter {
                it.selected
            }?.map { it.tag.identifier } ?: listOf(""),
            storage = mutableUiState.value.storageGuidance,
            recipeNotPossible = false,
            emoji = mutableUiState.value.emoji,
            colour = mutableUiState.value.colour?.name ?: "PINK" // default is pink if none given.
        )
    }

    /**
     * Populates the screen with an existing recipe, so that it can be edited.
     * @param recipeId - The id of the recipe to populate the state with, to be displayed.
     */
    fun populateFromId(recipeId: Long) {
        viewModelScope.launch() {
            runCatching {
                setCreationMode(CreationMode.EDIT)
                setRecipeEditId(recipeId)
                val recipe = recipeRepository.getRecipeById(recipeId).first()
                if (recipe == null) {
                    return@launch
                }
                val recipeType = recipeRepository.getRecipeTypeById(recipe.recipeTypeId).first()

                // Get all tags and the selected tags for this recipe
                val allTags = tagRepository.getAllForUser(recipe.authorUid).first()
                val selectedTags = recipeRepository.getRecipeWithRecipeTags(recipe.id).first()
                    .recipeTags.map { tag ->
                        val tag = tagRepository.getById(tag.tagId).first()
                        TagSelection(tag!!, true)
                }.toMutableList()

                // Populate the selected tags such that the non-selected ones are added, set to false
                allTags.forEach { tag ->
                    if (!selectedTags.contains(TagSelection(tag, true))) {
                        // if not selected, add it as non selected
                        selectedTags.add(TagSelection(tag, false))
                    }
                }

                // Build ingredients
                val llmIngredients: MutableList<LlmIngredient> = mutableListOf()
                val allIngredients = recipeRepository.getRecipeWithRecipeIngredients(recipe.id).first().ingredients

                // Sections get just put together. in future, could improve this to keep the
                // sections. but for now leaving it as this, not a big issue.
                allIngredients.forEach { recipeIngredient ->
                    val ingredient = ingredientRepository.getById(recipeIngredient.ingredientId).first()
                    llmIngredients.add(LlmIngredient(
                        ingredientSingular = ingredient?.singular ?: "",
                        ingredientPlural = ingredient?.plural ?: "",
                        // gets multiplied by no. servings so that the output looks correct to user
                        // (as it is stored in db for 1 serving). then when the user saves,
                        // it will be divided back down again by the servings.
                        ingredientAmount = round(recipeIngredient.amount * recipe.numberOfServings * 100) / 100,
                        ingredientAmountUnit = recipeIngredient.ingredientUnit.name
                    ))
                }

                val method = recipeRepository.getRecipeWithRecipeSteps(recipe.id).first()
                    .recipeSteps.map { step ->
                        // random UUID to use as the key in the draggable list:
                        RecipeStep( step.instruction, UUID.randomUUID() )
                }

                // do checks for null etc
                if (recipeType == null) {
                    return@launch
                }
                // then populate the ui:
                setTitle(recipe.title)
                setNumberOfServings(recipe.numberOfServings)
                setRecipeType(recipeType)
                setStorageGuidance(recipe.storageInstructions)
                setRecipeTags(selectedTags)
                setIngredients(llmIngredients)
                setMethod(method)
                setImageFile(recipe.imagePath)
                setEmoji(recipe.emoji)
                setColour(enumValueOf<NourishColour>(recipe.colour))
            }
        }
    }
}