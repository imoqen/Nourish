package uk.co.imoqen.nourish.recipecreation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.recipediscovery.RecipeDiscoveryUiState
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

    // expose tag selection method to update all recipe types in this view:
    fun getAllRecipeTypes() {
        viewModelScope.launch() {
            (recipeActions.getAllRecipeTypes())?.let { updatedTypes ->
                setRecipeTypes(updatedTypes) // only sets if not null
            }
        }
    }

}