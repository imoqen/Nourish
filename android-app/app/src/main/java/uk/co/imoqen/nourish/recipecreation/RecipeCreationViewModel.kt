package uk.co.imoqen.nourish.recipecreation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
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
}