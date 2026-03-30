package uk.co.imoqen.nourish

import androidx.room.withTransaction
import co.uk.imoqen.testfixtures.common.FakeUiEventBus
import co.uk.imoqen.testfixtures.common.TestData
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.repository.FakeIngredientRepository
import co.uk.imoqen.testfixtures.repository.FakeRecipeRepository
import co.uk.imoqen.testfixtures.repository.FakeShoppingListEntryRepository
import co.uk.imoqen.testfixtures.repository.FakeTagRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.ingredient.AddIngredientSheetState
import uk.co.imoqen.nourish.db.NourishDatabase
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

    // using Mockito to mock the database, as a Fake database is not possible due to its complexity.
    @MockK
    private lateinit var mockDb: NourishDatabase

    lateinit var vm: ShoppingListViewModel

    @Before
    fun init() {
        // First, initialise the mocks: (the database mock)
        // Approach taken from https://stackoverflow.com/questions/56500576/how-to-mock-android-room-withtransaction-method-with-mockk

        MockKAnnotations.init(this)

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { mockDb.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        // Then create the objects to use in testing:
        // uses the true recipeActions so it is testable, using the fake managers and repositories
        val recipeActions = RecipeActions(
            uiEventBus = fakeUiEventBus,
            authenticationManager = fakeAuthenticationManager,
            tagRepository = fakeTagRepository,
            recipeRepository = fakeRecipeRepository
        )
        vm = ShoppingListViewModel(
            mockDb, fakeAuthenticationManager, fakeUiEventBus, recipeActions,
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
        vm.setItemIndexToDelete(2) // out of bounds of the entries (as there are only 2 entries in the test data)
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        vm.deleteShoppingListItem()
        // now check the item has not been deleted:
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
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.updatePurchasedState(0, TestData.shoppingListUiEntry_cherries.copy(purchased = true))
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries.map{ item -> if (item.plural == "cherries") { TestData.shoppingListUiEntries.first().copy(purchased = true)} else item })
    }

    @Test
    fun `updatePurchasedState updates the purchased status from true to false`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries.map{ item -> if (item.plural == "cherries") { TestData.shoppingListUiEntries.first().copy(purchased = true)} else item })
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.updatePurchasedState(0, TestData.shoppingListUiEntry_cherries.copy(purchased = false))
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries)
    }

    @Test
    fun `updatePurchasedState does not update the purchased status if an error occurs`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.updatePurchasedState(0, TestData.shoppingListUiEntry_bananas)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries) // no change made as that ingredient is not in the list
    }

    @Test
    fun `reorderShoppingList swaps two unpurchased items`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.reorderShoppingList(0, 1) // swaps the first and second elements
        // change is made: ingredients are the same, but with the ordinals swapped.
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries.map { if (it.ordinal == 0) { it.copy(ordinal = 1) } else { it.copy(ordinal = 0) } })
    }

    @Test
    fun `reorderShoppingList does not reorder if the first item's key is null`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.reorderShoppingList(null, 1)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries) // no change made, as a key was null
    }

    @Test
    fun `reorderShoppingList does not reorder if the second item's key is null`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.reorderShoppingList(1, null)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries) // no change made, as a key was null
    }

    @Test
    fun `reorderShoppingList does not reorder if both item's keys are null`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.reorderShoppingList(null, null)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries) // no change made, as a key was null
    }

    @Test
    fun `reorderShoppingList does not reorder if the ordinal of the items are null`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.reorderShoppingList(3, 4)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries) // no change made, as a key was null
    }

    @Test
    fun `formatIngredientAmount`() {
        vm.setShoppingListEntries(TestData.shoppingListUiEntries)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.reorderShoppingList(3, 4)
        Assert.assertEquals(vm.uiState.value.shoppingListEntries, TestData.shoppingListUiEntries) // no change made, as a key was null
    }

}