package uk.co.imoqen.nourish

import co.uk.imoqen.testfixtures.common.FakeUiEventBus
import co.uk.imoqen.testfixtures.common.TestData
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.repository.FakeIngredientRepository
import co.uk.imoqen.testfixtures.repository.FakeRecipeRepository
import co.uk.imoqen.testfixtures.repository.FakeShoppingListEntryRepository
import co.uk.imoqen.testfixtures.repository.FakeTagRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetState
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.recipebook.RecipeBookViewModel
import uk.co.imoqen.nourish.setup.MainDispatcherRule
import uk.co.imoqen.nourish.shoppinglist.IngredientToEdit
import uk.co.imoqen.nourish.shoppinglist.ShoppingListUiEntry
import uk.co.imoqen.nourish.shoppinglist.ShoppingListViewModel
import uk.co.imoqen.nourish.shoppinglist.UserInteractionStatus

class ShoppingListViewModelTest {
    val fakeAuthenticationManager = FakeAuthenticationManager()
    val fakeUiEventBus = FakeUiEventBus()
    val fakeShoppingListEntryRepository = FakeShoppingListEntryRepository()
    val fakeIngredientRepository = FakeIngredientRepository()
    val fakeTagRepository = FakeTagRepository()
    val fakeRecipeRepository = FakeRecipeRepository()
    val recipeActions = RecipeActions(
        fakeUiEventBus, fakeAuthenticationManager, fakeTagRepository, fakeRecipeRepository
    )

    lateinit var vm: ShoppingListViewModel

    @Before
    fun init() {
        // uses the true recipeActions so it is testable, using the fake managers and repositories
        val recipeActions = RecipeActions(
            uiEventBus = fakeUiEventBus,
            authenticationManager = fakeAuthenticationManager,
            tagRepository = fakeTagRepository,
            recipeRepository = fakeRecipeRepository
        )
        vm = ShoppingListViewModel(
            fakeAuthenticationManager, fakeUiEventBus, recipeActions,
            fakeShoppingListEntryRepository, fakeIngredientRepository
        )
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Trivial tests for setters, still required for good coverage:

    @Test
    fun `should set shoppingListEntries correctly`() {
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, emptyList<ShoppingListUiEntry>())
        vm.setShoppingListEntries(TestData.shoppingListEntries)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListEntries)
    }

    @Test
    fun `should set searchTerm correctly`() {
        Assert.assertEquals(vm.uiState.value.searchTerm, "")
        vm.setSearchTerm("Cherries")
        Assert.assertEquals(vm.uiState.value.searchTerm, "Cherries")
    }

    @Test
    fun `should set userInteractionStatus correctly`() {
        Assert.assertEquals(vm.uiState.value.userInteractionStatus, UserInteractionStatus.BROWSE) // default is set to  browse; user browse shopping list.
        vm.setUserInteractionStatus(UserInteractionStatus.ADD)
        Assert.assertEquals(vm.uiState.value.userInteractionStatus, UserInteractionStatus.ADD)
        vm.setUserInteractionStatus(UserInteractionStatus.EDIT)
        Assert.assertEquals(vm.uiState.value.userInteractionStatus, UserInteractionStatus.EDIT)
    }

    @Test
    fun `should set showDeleteAllDialog correctly`() {
        Assert.assertEquals(vm.uiState.value.showDeleteAllDialog, false)
        vm.setShowDeleteAllDialog(true)
        Assert.assertEquals(vm.uiState.value.showDeleteAllDialog, true)
    }

    @Test
    fun `should set showDeletePurchasedDialog correctly`() {
        Assert.assertEquals(vm.uiState.value.showDeletePurchasedDialog, false)
        vm.setShowDeletePurchasedDialog(true)
        Assert.assertEquals(vm.uiState.value.showDeletePurchasedDialog, true)
    }

    @Test
    fun `should set ingredientToAddName correctly`() {
        Assert.assertEquals(
            vm.uiState.value.ingredientToAdd,
            AddIngredientSheetState("", IngredientUnit.SINGLE, "", "")
        )
        vm.setIngredientToAddName("Oat milk")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.name, "Oat milk")
    }

    @Test
    fun `should set ingredientToAddUnit correctly`() {
        Assert.assertEquals(
            vm.uiState.value.ingredientToAdd,
            AddIngredientSheetState("", IngredientUnit.SINGLE, "", "")
        )
        vm.setIngredientToAddUnit(IngredientUnit.ML)
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.unit, IngredientUnit.ML)
    }

    @Test
    fun `should set ingredientToAddAmount correctly`() {
        Assert.assertEquals(
            vm.uiState.value.ingredientToAdd,
            AddIngredientSheetState("", IngredientUnit.SINGLE, "", "")
        )
        vm.setIngredientToAddAmount("2000")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.amount, "2000")
    }

    @Test
    fun `should set ingredientToAddError correctly`() {
        Assert.assertEquals(
            vm.uiState.value.ingredientToAdd,
            AddIngredientSheetState("", IngredientUnit.SINGLE, "", "")
        )
        vm.setIngredientToAddError("The ingredient is invalid.")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.error, "The ingredient is invalid.")
    }

    @Test
    fun `should set ingredientToEditAmount correctly`() {
        Assert.assertEquals(
            vm.uiState.value.ingredientToEdit,
            IngredientToEdit(-1, "")
        )
        vm.setEditIngredientAmount("75")
        Assert.assertEquals(vm.uiState.value.ingredientToEdit.amount, "75")
    }

    @Test
    fun `should set ingredientToEditIndex correctly`() {
        Assert.assertEquals(
            vm.uiState.value.ingredientToEdit,
            IngredientToEdit(-1, "")
        )
        vm.setIngredientIndexToEdit(2)
        Assert.assertEquals(vm.uiState.value.ingredientToEdit.index, 2)
    }

    @Test
    fun `should set itemIndexToDelete correctly`() {
        Assert.assertEquals(
            vm.uiState.value.itemIndexToDelete, -1
        )
        vm.setItemIndexToDelete(2)
        Assert.assertEquals(vm.uiState.value.itemIndexToDelete, 2)
    }

    @Test
    fun `deleteShoppingListItem should delete an item from the shopping list if the item is not null`() {
        vm.setItemIndexToDelete(1) // will delete second item,  this is oatMilk in the TestData
        vm.setShoppingListEntries(TestData.shoppingListEntries)
        vm.deleteShoppingListItem()
        // now check the item has been deleted:
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf(TestData.shoppingListEntries[0])) // second index deleted; only first index remains.
    }

    @Test
    fun `deleteShoppingListItem should not delete an item from the shopping list if the item is not null (does not exist in the list)`() {
        vm.setItemIndexToDelete(0) // first item triggers null return for this test (in the fake shopping list repository)
        vm.setShoppingListEntries(TestData.shoppingListEntries)
        vm.deleteShoppingListItem()
        // now check the item has been deleted:
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListEntries) // nothing deleted
    }

    @Test
    fun `clearShoppingList should not clear the list if the user is not logged in`() {
        fakeAuthenticationManager.setLoginState(false, "") // not logged in
        vm.setShoppingListEntries(TestData.shoppingListEntries)
        vm.clearShoppingList()
        // shopping list will not have been cleared:
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListEntries) // not cleared
    }

    @Test
    fun `clearShoppingList clears only purchased items when onlyDeletePurchased is true`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user") // not logged in
        vm.setShoppingListEntries(TestData.shoppingListEntries)
        vm.clearShoppingList(onlyDeletePurchased = true)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListEntries) // then it stays same because none have been purchased.
    }

    @Test
    fun `clearShoppingList clears all items when onlyDeletePurchased is false (this is by default)`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user") // not logged in
        vm.setShoppingListEntries(TestData.shoppingListEntries)
        vm.clearShoppingList()
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf<ShoppingListUiEntry>()) // then it stays same because none have been purchased.
    }

}