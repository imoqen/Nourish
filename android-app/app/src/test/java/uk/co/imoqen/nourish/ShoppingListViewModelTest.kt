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
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
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
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries)
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
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        vm.deleteShoppingListItem()
        // now check the item has been deleted:
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf(TestData.shoppingListUiEntries[0])) // second index deleted; only first index remains.
    }

    @Test
    fun `deleteShoppingListItem should not delete an item from the shopping list if the item is not null (does not exist in the list)`() {
        vm.setItemIndexToDelete(0) // first item triggers null return for this test (in the fake shopping list repository)
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        vm.deleteShoppingListItem()
        // now check the item has been deleted:
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries) // nothing deleted
    }

    @Test
    fun `clearShoppingList should not clear the list if the user is not logged in`() {
        fakeAuthenticationManager.setLoginState(false, "") // not logged in
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        vm.clearShoppingList()
        // shopping list will not have been cleared:
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries) // not cleared
    }

    @Test
    fun `clearShoppingList clears only purchased items when onlyDeletePurchased is true`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user_shoppinglist_clearPurchased") // not logged in
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        vm.clearShoppingList(onlyDeletePurchased = true)
        // nothing cleared as none have been purchased:
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf( // plurals are determined by the faked ingr.getById hence return non-senselical values. however, since this isn't the focus of the test here, it isn't a problem - its simply caused by the mock.
            ShoppingListUiEntry(0, "cherries", IngredientUnit.G, 500.0, false, 0),
            ShoppingListUiEntry(1, "cherries", IngredientUnit.L, 1.0, false, 1)
        ))
    }

    @Test
    fun `clearShoppingList clears all items when onlyDeletePurchased is false (this is by default)`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user_shoppinglist_clearAll") // not logged in
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        vm.clearShoppingList()
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf<ShoppingListUiEntry>()) // then it stays same because none have been purchased.
    }

    @Test
    fun `populateShoppingList does not populate when the user is not logged in)`() {
        fakeAuthenticationManager.setLoginState(false, "") // not logged in
        vm.populateShoppingList()
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf<ShoppingListUiEntry>()) // not populated
    }

    @Test
    fun `populateShoppingList populates the shopping list when the user is logged in)`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.populateShoppingList()
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf(
            ShoppingListUiEntry(1, "cherries", IngredientUnit.L, 1.0, false, 1),
            ShoppingListUiEntry(0, "cherries", IngredientUnit.G, 500.0, false, 0)
        ))
    }

    @Test
    fun `populateShoppingList populates the shopping list and handles converting G to KG and ML to L if their amounts are over 1000`() {
        fakeAuthenticationManager.setLoginState(true, "user_with_ingr_to_convert_l_and_kg") // logged in
        vm.populateShoppingList()
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf( // plurals are determined by the faked ingr.getById hence return non-senselical values. however, since this isn't the focus of the test here, it isn't a problem - its simply caused by the mock.
            ShoppingListUiEntry(2, "cherries", IngredientUnit.L, 2.0, false, 2),
            ShoppingListUiEntry(3, "water", IngredientUnit.KG, 5.0, false, 3)
        ))
    }

    @Test
    fun `populateShoppingList handles errors correctly, as a result not populating the shopping list`() {
        fakeAuthenticationManager.setLoginState(true, "user_who_throws_error") // logged in
        vm.populateShoppingList()
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, listOf<ShoppingListUiEntry>()) // shopping list not populated
    }

    @Test
    fun `updatePurchasedState updates the purchased status from false to true`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        fakeAuthenticationManager.setLoginState(true, "user_who_throws_error") // logged in
        vm.updatePurchasedState(0, TestData.shoppingListUiEntry_cherries.copy(purchased = true))
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries.map{ item -> if (item.plural == "cherries") { TestData.shoppingListUiEntries.first().copy(purchased = true)} else item }) // shopping list not populated
    }

}