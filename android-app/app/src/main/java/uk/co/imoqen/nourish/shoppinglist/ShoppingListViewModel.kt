package uk.co.imoqen.nourish.shoppinglist

import android.util.Log
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
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
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

    fun setSearchTerm(searchTerm: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                searchTerm = searchTerm
            )
        }
    }

    fun setUserInteractionStatus(userInteractionStatus: UserInteractionStatus) {
        mutableUiState.update { currentState ->
            currentState.copy(
                userInteractionStatus = userInteractionStatus
            )
        }
    }

    fun deleteShoppingListItem(id: Long, index: Int) {
        viewModelScope.launch() {
            runCatching {
                // first, update the DB:
                val item = shoppingListEntryRepository.getById(id).first()
                if (item == null) {
                    return@launch
                }
                shoppingListEntryRepository.delete(item)
            }
                .onSuccess {
                    // if the DB update was successful, update the UI
                    val updatedList = mutableUiState.value.shoppingListEntries
                        updatedList.toMutableList().removeAt(index)
                    // update the ui state:
                    setShoppingListEntries(updatedList) // NOT UPDATING
                }
        }
    }

    fun updatePurchasedState(index: Int, newItem: ShoppingListUiEntry) {
        viewModelScope.launch() {
            runCatching {
                // first, update the DB:
                shoppingListEntryRepository.updatePurchaseStatus(newItem.id, newItem.purchased)
            }
                .onSuccess {
                    // if the DB update was successful, update the UI
                    val updatedList = mutableUiState.value.shoppingListEntries.toMutableList().apply {
                        this[index] = newItem
                    }
                    // update the ui state:
                    setShoppingListEntries(updatedList)
                }
        }
    }

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
                            ordinal = shoppingListEntry.ordinal
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
                    // update DB to persist the change. UI is simply updated in the view.
                    shoppingListEntryRepository.setOrdinal(fromKey, toOrdinal)
                    shoppingListEntryRepository.setOrdinal(toKey, fromOrdinal)

                    // Update the ordinals in the ui so that it persists when others are moved
                    val fromIndex = mutableUiState.value.shoppingListEntries.indexOfFirst{ it.id == fromKey }
                    val toIndex = mutableUiState.value.shoppingListEntries.indexOfFirst{ it.id == toKey }
                    val updatedList = mutableUiState.value.shoppingListEntries.toMutableList().apply {
                        this[fromIndex] = this[fromIndex].copy(ordinal = toOrdinal)
                        this[toIndex] = this[toIndex].copy(ordinal = fromOrdinal)
                    }
                    setShoppingListEntries(updatedList)
                }
            }
                .onSuccess {
                    Log.i("successfully swapped", "success")
                }
        }
    }

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