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
import uk.co.imoqen.nourish.common.ingredient.IngredientSheetAction
import uk.co.imoqen.nourish.common.tag.TagSheetAction
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient
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

    fun setShowDeleteAllDialog(showDeleteAllDialog: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showDeleteAllDialog = showDeleteAllDialog
            )
        }
    }

    fun setShowDeletePurchasedDialog(showDeletePurchasedDialog: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showDeletePurchasedDialog = showDeletePurchasedDialog
            )
        }
    }

    fun setItemIndexToDelete(itemIndexToDelete: Int) {
        mutableUiState.update { currentState ->
            currentState.copy(
                itemIndexToDelete = itemIndexToDelete
            )
        }
    }

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

    fun setIngredientIndexToEdit(index: Int){
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientToEdit = currentState.ingredientToEdit.copy(
                    index = index
                )
            )
        }
    }

    fun setEditIngredientAmount(amount: String){
        mutableUiState.update { currentState ->
            currentState.copy(
                ingredientToEdit = currentState.ingredientToEdit.copy(
                    amount = amount
                )
            )
        }
    }

    fun deleteShoppingListItem() {
        viewModelScope.launch() {
            val itemIndex = mutableUiState.value.itemIndexToDelete
            runCatching {
                // first, update the DB:
                val itemId = mutableUiState.value.shoppingListEntries[itemIndex].id
                val item = shoppingListEntryRepository.getById(itemId).first()
                if (item == null) {
                    return@launch
                }
                shoppingListEntryRepository.delete(item)
            }
                .onSuccess {
                    // if the DB update was successful, update the UI
                    val updatedList = mutableUiState.value.shoppingListEntries.toMutableList()
                    val removed = updatedList.removeAt(itemIndex)
                    Log.i("deletion list", "$removed, $updatedList")
                    // update the ui state:
                    setShoppingListEntries(updatedList) // NOT UPDATING
                }
        }
    }

    // deletes every shopping list item of that user, or only the purchased ones if specified.
    fun clearShoppingList(
        onlyDeletePurchased: Boolean = false
    ) {
        viewModelScope.launch() {
            runCatching {
                val uid = authenticationManager.getUid()
                if (uid == null) {
                    return@launch
                }
                if (onlyDeletePurchased) {
                    shoppingListEntryRepository.deletePurchasedForUser(uid)
                } else {
                    shoppingListEntryRepository.deleteAllForUser(uid)
                }
            }
                .onSuccess {
                   // now update in the UI:
                    populateShoppingList()
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

                    var amount = shoppingListEntry.amount
                    var unit = shoppingListEntry.unit

                    // convert the amounts in the UI if g and kg and >= 1000
                    // only in the UI - not in the db so everything is still stored as G / ML so easy to compare and bundle together.
                    if (amount >= 1000 && (unit == IngredientUnit.G || unit == IngredientUnit.ML)) {
                        when (unit) {
                            IngredientUnit.G -> { unit = IngredientUnit.KG }
                            IngredientUnit.ML -> { unit = IngredientUnit.L }
                            else -> {} // make no change
                        }
                        amount = amount / 1000
                    }

                    shoppingListUiEntries.add(
                        ShoppingListUiEntry(
                            id = shoppingListEntry.id, // maybe need to store ingredient id in here in future but maybe not.
                            plural = ingredientRepository.getById(shoppingListEntry.ingredientId).first()?.plural ?: "",
                            unit = unit,
                            amount = amount,
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
        roundToNearestQuarter: Boolean = true
    ): String {
        var amount = ingredientAmount
        if (amount < 0.25) {
            amount = 0.25 // cant go less than 0.25
        }
        return recipeActions.formatIngredientAmount(amount, roundToNearestQuarter)
    }

    fun addIngredientToShoppingList() {
        viewModelScope.launch() {
            runCatching {
                val uid = authenticationManager.getUid()
                if (uid == null) {
                    return@launch
                }

                val ingredientName = mutableUiState.value.ingredientToAdd.name.trim() // whitespace si removed
                if (ingredientName.trim() == "") {
                    setIngredientToAddError("The item cannot be blank.")
                    return@launch
                }
                // add it to ingredient table if doesn't already exist in either singular or plurla (as its user input, dont know whether this will be singular or plural, so both are checked)
                var ingredientId = ingredientRepository.getIdByPlural(ingredientName)
                Log.i("plural", "$ingredientId")
                if (ingredientId == null) {
                    // plural search returned null, now look for singular.
                    ingredientId = ingredientRepository.getIdBySingular(ingredientName)
                    Log.i("singular", "$ingredientId")
                    if (ingredientId == null) {
                        // still null means it doesnt exist: add it
                        ingredientId = ingredientRepository.insert(
                            Ingredient(
                                singular = "", // singular is "" and this will be checked when the LLM adds a new ingredient. LLM searches by plurality. if it finds a match where singular is null, it can then replace it.
                                plural = ingredientName
                            )
                        )
                        Log.i("created", "$ingredientId")
                    }
                }

                // dynamically determine when UNIT should be used: if no unit was selected, but an amount was entered, it is UNIT.
                var unit = mutableUiState.value.ingredientToAdd.unit
                val amount = mutableUiState.value.ingredientToAdd.amount
                // overwriting to compensate for different cases:

                // distinguish between SINGLE and UNIT: single is if there is no amount, otherwise it is unit.
                if (unit == IngredientUnit.SINGLE && amount != "") {
                    unit = IngredientUnit.UNIT
                }

                // cast the amount to double and also handle , case (for swiss / german numbers etc)
                var doubleAmount = amount.replace(',', '.').toDoubleOrNull() ?: 1.0

                if (unit == IngredientUnit.KG) {
                    // convert to g and scale
                    unit = IngredientUnit.G
                    doubleAmount = doubleAmount * 1000
                }
                if (unit == IngredientUnit.L) {
                    // convert to ml and scale
                    unit = IngredientUnit.ML
                    doubleAmount = doubleAmount * 1000
                }

                shoppingListEntryRepository.addShoppingListEntry(
                    ShoppingListIngredient(
                        ingredientId = ingredientId,
                        plural = ingredientName,
                        unit = unit,
                        amount = doubleAmount,
                    ),
                    uid = uid,
                    userAdded = true
                )

                // then reset the ui for adding:
                resetIngredientAdditionUI()

                // and repopulate the list with the new ingredient:
                populateShoppingList()
            }
                .onSuccess {

                }
                .onFailure {  e ->
                    Log.i("shopping list add", "error: $e")

                }
        }

    }

    fun updateIngredientAmount() {
        viewModelScope.launch() {
            runCatching {
                val shoppingListUiEntry =
                    mutableUiState.value.shoppingListEntries[mutableUiState.value.ingredientToEdit.index]
                val shoppingListEntry =
                    shoppingListEntryRepository.getById(shoppingListUiEntry.id).first()

                if (shoppingListEntry == null) {
                    throw NullPointerException()
                }
                Log.i("amount: ", mutableUiState.value.ingredientToEdit.amount)

                var amount = mutableUiState.value.ingredientToEdit.amount.replace(',','.').toDoubleOrNull() ?: 1.0 // 1.0 if the input is invalid.
                if (shoppingListUiEntry.unit == IngredientUnit.KG || shoppingListUiEntry.unit == IngredientUnit.L) {
                    // multiply the value by 1000 as it will be stored in the db as G or ML.
                    amount = amount * 1000
                }
                shoppingListEntryRepository.update(
                    shoppingListEntry.copy(
                        amount = amount
                    )
                )
                // and repopulate the list with the newly edited ingredient:
                populateShoppingList()

                // reset in UI:
                setIngredientIndexToEdit(-1)
                setEditIngredientAmount("")
            } .onFailure {
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(
                    "Couldn't update the item amount. Please try again later."
                ))
            }
        }
    }

    fun resetIngredientAdditionUI() {
        setIngredientToAddAmount("")
        setIngredientToAddUnit(IngredientUnit.SINGLE)
        setIngredientToAddName("")
        setUserInteractionStatus(UserInteractionStatus.BROWSE)
        setIngredientToAddError("")
    }

    // the actions for the ingredient addition sheet;
    fun onIngredientSheetAction(action: IngredientSheetAction) {
        when (action) {
            is IngredientSheetAction.OnDismissRequest -> { setUserInteractionStatus(UserInteractionStatus.BROWSE) }
            is IngredientSheetAction.OnComplete -> { addIngredientToShoppingList() }
            is IngredientSheetAction.SetIngredientAmount -> { setIngredientToAddAmount(action.amount) }
            is IngredientSheetAction.SetIngredientName -> { setIngredientToAddName(action.name) }
            is IngredientSheetAction.SetIngredientUnit -> { setIngredientToAddUnit(action.unit) }
        }
    }

}