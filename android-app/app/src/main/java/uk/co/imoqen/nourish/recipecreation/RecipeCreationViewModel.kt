package uk.co.imoqen.nourish.recipecreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.RecipeActions
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

    fun setRecipeTypes(recipeTypes: List<RecipeType>?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                recipeTypes = recipeTypes
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

}