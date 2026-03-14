package uk.co.imoqen.nourish.shoppinglist

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntry
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val db: NourishDatabase,
    private val authenticationManager: AuthenticationManager,
    private val uiEventBus: UiEventBus,
    private val recipeRepository: RecipeRepository,
    private val recipeActions: RecipeActions,
    private val shoppingListEntryRepository: ShoppingListEntryRepository,
    private val ingredientRepository: IngredientRepository
): ViewModel() {

    private val mutableUiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = mutableUiState.asStateFlow()

    // Setters for Ui State values:

    fun setShoppingListEntries(shoppingListEntries: List<ShoppingListUiEntry>) {
        mutableUiState.update { currentState ->
            currentState.copy(
                shoppingListEntries = shoppingListEntries
            )
        }
    }

    /*fun updateShoppingListEntry(index: Int, ordinal: Int) {
        val updatedList = mutableUiState.value.shoppingListEntries.toMutableList().apply {
            this[index] = this[index].copy(ordinal = ordinal)
        }
    }*/


    fun populateShoppingList() {
        viewModelScope.launch() {
            runCatching {
                val uid = authenticationManager.getUid()
                if (uid == null) {
                    return@launch
                }

                val shoppingListEntries = shoppingListEntryRepository.getAll(uid).first() // gets them sorted

                // required for the ui, to get the ingredients for each from the ingredient id.
                val shoppingListUiEntries: MutableList<ShoppingListUiEntry> = mutableListOf()
                shoppingListEntries.forEach { shoppingListEntry ->
                    shoppingListUiEntries.add(
                        ShoppingListUiEntry(
                            id = shoppingListEntry.id, // maybe need to store ingredient id in here in future but maybe not.
                            plural = ingredientRepository.getById(shoppingListEntry.ingredientId).first()?.plural ?: "",
                            unit = shoppingListEntry.unit,
                            amount = shoppingListEntry.amount,
                            purchased = shoppingListEntry.purchased,
                        )
                    )
                }

                setShoppingListEntries(shoppingListUiEntries)
                //sortShoppingListByOrdinal()
            }
                .onFailure { e ->
                    Log.i("SHOPPING LIST ERROR", "$e")
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Shopping list could not be obtained. Please try again later."))
                }
        }
    }

    fun reorderShoppingList(fromKey: Long?, toKey: Long?) {
        viewModelScope.launch() {
            runCatching {
                if (fromKey == null || toKey == null) {
                    return@launch
                }
                db.withTransaction {
                    // get the ordinals
                    val fromOrdinal = shoppingListEntryRepository.getById(fromKey).first()?.ordinal
                    val toOrdinal = shoppingListEntryRepository.getById(toKey).first()?.ordinal
                    // swap the ordinals only if they are valid
                    if (fromOrdinal == null || toOrdinal == null) {
                        return@withTransaction
                    }
                    // update DB to persist the change:
                    shoppingListEntryRepository.setOrdinal(fromKey, toOrdinal)
                    shoppingListEntryRepository.setOrdinal(toKey, fromOrdinal)

                    // update UI:
                    /*val fromIndex = mutableUiState.value.shoppingListEntries.indexOfFirst { it.id == fromKey }
                    val toIndex = mutableUiState.value.shoppingListEntries.indexOfFirst { it.id == toKey }
                    updateShoppingListEntry(fromIndex, toOrdinal)
                    updateShoppingListEntry(toIndex, fromOrdinal)
                    sortShoppingListByOrdinal()*/
                }
            }
                .onSuccess {
                    Log.i("successfully swapped", "success")
                    // order if successfully swapped
                }
        }
    }


   /* fun sortShoppingListByOrdinal() {
        viewModelScope.launch() {
            runCatching {
                setShoppingListEntries(mutableUiState.value.shoppingListEntries.sortedBy {
                    it.ordinal
                })
            }
        }
    }*/

    fun formatIngredientAmount(
        ingredientAmount: Double,
    ): String {
        // scale the ingredient amount by the desired number of servings set by user:
        var amount = ingredientAmount
        if (amount < 0.25) {
            amount = 0.25 // cant go less than 0.25
        }
        return recipeActions.formatIngredientAmount(amount)
    }

}