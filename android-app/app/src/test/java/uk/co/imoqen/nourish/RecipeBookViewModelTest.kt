package uk.co.imoqen.nourish

import androidx.compose.runtime.collectAsState
import co.uk.imoqen.testfixtures.common.FakeUiEventBus
import co.uk.imoqen.testfixtures.common.TestData
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.network.FakeApiManager
import co.uk.imoqen.testfixtures.repository.FakeIngredientRepository
import co.uk.imoqen.testfixtures.repository.FakeRecipeRepository
import co.uk.imoqen.testfixtures.repository.FakeShoppingListEntryRepository
import co.uk.imoqen.testfixtures.repository.FakeTagRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.Recipe
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.network.request.LlmActionType
import uk.co.imoqen.nourish.recipebook.ActionChatStage
import uk.co.imoqen.nourish.recipebook.RecipeBookViewModel
import uk.co.imoqen.nourish.recipebook.RecipeCardExpanded
import uk.co.imoqen.nourish.recipebook.RecipeCardUnexpanded
import uk.co.imoqen.nourish.recipebook.ShoppingListIngredient
import uk.co.imoqen.nourish.setup.MainDispatcherRule
import kotlin.collections.emptyList
import kotlin.collections.listOf
import kotlin.collections.mutableListOf
import kotlin.test.assertFailsWith

class RecipeBookViewModelTest {
    val fakeAuthenticationManager = FakeAuthenticationManager()
    val fakeApiManager = FakeApiManager()
    lateinit var fakeUiEventBus: FakeUiEventBus
    val fakeRecipeRepository = FakeRecipeRepository()
    val fakeTagRepository = FakeTagRepository()
    val fakeIngredientRepository = FakeIngredientRepository()
    val fakeShoppingListEntryRepository = FakeShoppingListEntryRepository()

    lateinit var vm: RecipeBookViewModel

    @Before
    fun init() {
        fakeUiEventBus = FakeUiEventBus()
        // uses the true recipeActions so it is testable, using the fake managers and repositories
        val recipeActions = RecipeActions(
            uiEventBus = fakeUiEventBus,
            authenticationManager = fakeAuthenticationManager,
            tagRepository = fakeTagRepository,
            recipeRepository = fakeRecipeRepository
        )
        vm = RecipeBookViewModel(
            fakeAuthenticationManager, fakeApiManager, fakeUiEventBus, fakeRecipeRepository, fakeIngredientRepository,
            fakeTagRepository, recipeActions, fakeShoppingListEntryRepository
        )
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Trivial tests for setters, still required for good coverage:

    @Test
    fun `should set recipeCard correctly`() {
        Assert.assertEquals(vm.uiState.value.selectedRecipeCard, null)
        vm.setRecipeCard(TestData.recipeCardExpanded_cherry)
        Assert.assertEquals(vm.uiState.value.selectedRecipeCard, TestData.recipeCardExpanded_cherry)
    }

    @Test
    fun `should set selectedRecipe correctly`() {
        Assert.assertEquals(vm.uiState.value.selectedRecipe, null)
        vm.setSelectedRecipe(TestData.recipe_salad)
        Assert.assertEquals(vm.uiState.value.selectedRecipe, TestData.recipe_salad)
    }

    @Test
    fun `should set recipeBookMap correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeBookMap, null)
        vm.setRecipeBookMap(TestData.recipeBookMap_dessert)
        Assert.assertEquals(vm.uiState.value.recipeBookMap, TestData.recipeBookMap_dessert)
    }

    @Test
    fun `should set searchTerm correctly`() {
        Assert.assertEquals(vm.uiState.value.searchTerm, "")
        vm.setSearchTerm("cherry")
        Assert.assertEquals(vm.uiState.value.searchTerm, "cherry")
    }

    @Test
    fun `should set displayDropdownFilter correctly`() {
        Assert.assertEquals(vm.uiState.value.displayDropdownFilter, false)
        vm.setDisplayDropdownFilter(true)
        Assert.assertEquals(vm.uiState.value.displayDropdownFilter, true)
    }

    @Test
    fun `should set recipeTags correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeTags, null)
        vm.setRecipeTags(TestData.recipeTags)
        Assert.assertEquals(vm.uiState.value.recipeTags, TestData.recipeTags)
    }

    @Test
    fun `should set recipeTypes correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeTypes, null)
        vm.setRecipeTypes(TestData.typeSelection)
        Assert.assertEquals(vm.uiState.value.recipeTypes, TestData.typeSelection)
    }

    @Test
    fun `should set llmActionType correctly`() {
        Assert.assertEquals(vm.uiState.value.llmActionType, LlmActionType.NONE)
        vm.setLlmActionType(LlmActionType.NUTRITIONAL_VALUES)
        Assert.assertEquals(vm.uiState.value.llmActionType, LlmActionType.NUTRITIONAL_VALUES)
    }

    @Test
    fun `should set llmAwaitActionType correctly`() {
        Assert.assertEquals(vm.uiState.value.llmAwaitActionType, LlmActionType.NONE)
        vm.setLlmAwaitActionType(LlmActionType.NUTRITIONAL_VALUES)
        Assert.assertEquals(vm.uiState.value.llmAwaitActionType, LlmActionType.NUTRITIONAL_VALUES)
    }

    @Test
    fun `should set userActionRequest correctly`() {
        Assert.assertEquals(vm.uiState.value.userActionRequest, "")
        vm.setUserActionRequest("Is this healthy?")
        Assert.assertEquals(vm.uiState.value.userActionRequest, "Is this healthy?")
    }

    @Test
    fun `should set actionChatStage correctly`() {
        Assert.assertEquals(vm.uiState.value.actionChatStage, ActionChatStage.USER_INPUT)
        vm.setActionChatStage(ActionChatStage.AWAITING_LLM_RESPONSE)
        Assert.assertEquals(vm.uiState.value.actionChatStage, ActionChatStage.AWAITING_LLM_RESPONSE)
    }

    @Test
    fun `should set alteredRecipe correctly`() {
        Assert.assertEquals(vm.uiState.value.alteredRecipe, null)
        vm.setAlteredRecipe(TestData.llmRecipe_salad)
        Assert.assertEquals(vm.uiState.value.alteredRecipe, TestData.llmRecipe_salad)
    }

    @Test
    fun `should set visible correctly`() {
        Assert.assertEquals(vm.uiState.value.visible, true)
        vm.setVisible(false)
        Assert.assertEquals(vm.uiState.value.visible, false)
    }

    @Test
    fun `should set ingredientsToAddToShoppingList correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, null)
        vm.setIngredientsToAddToShoppingList(TestData.ingredientsToAddToShoppingList)
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, TestData.ingredientsToAddToShoppingList)
    }

    @Test
    fun `should set deleteConfirmation correctly`() {
        Assert.assertEquals(vm.uiState.value.deleteConfirmation, false)
        vm.setDeleteConfirmation(true)
        Assert.assertEquals(vm.uiState.value.deleteConfirmation, true)
    }

    @Test
    fun `alterIngredientShoppingListStatus alters the status of the shopping list correctly`() {
        vm.setIngredientsToAddToShoppingList(TestData.ingredientsToAddToShoppingList)
        vm.alterIngredientShoppingListStatus(TestData.shoppingListIngredient_oatMilk, 1, false) // sets the second item (oat milk) to be not selected
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, TestData.ingredientsToAddToShoppingList.map{ if (it.first.plural == "oat milk") { it.copy(second = false) } else it })
    }

    @Test
    fun `alterIngredientShoppingListStatus does not alter the status if the index is invalid`() {
        vm.setIngredientsToAddToShoppingList(TestData.ingredientsToAddToShoppingList)
        vm.alterIngredientShoppingListStatus(TestData.shoppingListIngredient_oatMilk, -1, false) // invalid index
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, TestData.ingredientsToAddToShoppingList) // unchanged
    }

    @Test
    fun `formatIngredientForShoppingList formats the shopping list correctly`() {
        vm.setSelectedRecipe(TestData.recipe_salad)
        vm.formatIngredientsForShoppingList()
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, mutableListOf(Pair(ShoppingListIngredient(ingredientId=0, plural="cherries", unit= IngredientUnit.G, amount=1000.0),true)))
    }

    @Test
    fun `formatIngredientForShoppingList doesn't format if the recipe is null`() {
        vm.formatIngredientsForShoppingList()
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, null)
    }

    @Test
    fun `formatIngredientForShoppingList doesn't format if an obtained ingredient is null`() {
        vm.setSelectedRecipe(TestData.recipe_withInvalidIngredients)
        vm.formatIngredientsForShoppingList()
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, null)
    }

    @Test
    fun `formatIngredientForShoppingList doesn't format water, as it doesn't need to be purchased in uk`() {
        vm.setSelectedRecipe(TestData.recipe_withWaterIngredients)
        vm.formatIngredientsForShoppingList()
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, listOf<Pair<ShoppingListIngredient, Boolean>>())
    }

    @Test
    fun `formatIngredientForShoppingList formats ingredients together when there are multiple of the same ingredients in the list`() {
        vm.setSelectedRecipe(TestData.recipe_withDuplicateIngredients)
        vm.formatIngredientsForShoppingList()
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, listOf(Pair(ShoppingListIngredient(ingredientId=5, plural="lettuce", unit=IngredientUnit.ML, amount=400.0), true))) // adds lettuce twice: 200 + 200 = 400
    }

    @Test
    fun `formatIngredientForShoppingList formats ingredient with a unit to convert, into UNIT`() {
        vm.setSelectedRecipe(TestData.recipe_withUnitToConvert)
        vm.formatIngredientsForShoppingList()
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, listOf(Pair(ShoppingListIngredient(ingredientId=0, plural="cherries", unit=IngredientUnit.UNIT, amount=1000.0), true))) // converts from HEAD to UNIT
    }

    @Test
    fun `formatIngredientForShoppingList formats ingredient with a unit to not keep in shopping list eg SINGLE`() {
        vm.setSelectedRecipe(TestData.recipe_withUnitToNotKeepInShoppingList)
        vm.formatIngredientsForShoppingList()
        Assert.assertEquals(vm.uiState.value.ingredientsToAddToShoppingList, listOf(Pair((ShoppingListIngredient(ingredientId=0, plural="cherries", unit= IngredientUnit.SINGLE, amount=1.0)), true))) // converts from HEAD to UNIT
    }

    @Test
    fun `addIngredientToShoppingList does not add the ingredients if the user is not logged in`() {
        vm.setIngredientsToAddToShoppingList(TestData.ingredientsToAddToShoppingList)
        fakeAuthenticationManager.setLoginState(false, "") // not logged in
        vm.addIngredientsToShoppingList()
        // the ingredients do not get added because the user is not logged in, so tbey remain in the state:
        Assert.assertEquals(
            vm.uiState.value.ingredientsToAddToShoppingList,
            TestData.ingredientsToAddToShoppingList
        )
    }

    @Test
    fun `addIngredientToShoppingList adds the ingredient if selected, and doesn't add the ingredient if not selected`() {
        vm.setIngredientsToAddToShoppingList(TestData.ingredientsToAddToShoppingList_oneSelected_oneNotSelected)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // not logged in
        vm.addIngredientsToShoppingList()
        // the ingredients do not get added because the user is not logged in, so tbey remain in the state:
        Assert.assertEquals(
            vm.uiState.value.ingredientsToAddToShoppingList, null // items got persisted to the DB, hence no longer in the UI state.
        )
    }

    @Test
    fun `addIngredientToShoppingList handles errors appropriately`() {
        vm.setIngredientsToAddToShoppingList(TestData.ingredientsToAddToShoppingList_triggerError)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // not logged in
        vm.addIngredientsToShoppingList()
        // the ingredients do not get added because the user is not logged in, so tbey remain in the state:
        Assert.assertEquals(
            vm.uiState.value.ingredientsToAddToShoppingList, TestData.ingredientsToAddToShoppingList_triggerError // items do not get added as error occurs, so remain in state.
        )
    }

    // runTest() can be used to call suspend functions in a test. otherwise, things like flow collection (via .first()) will not work.

    @Test
    fun `getAllRecipes returns empty list if the user is not logged in`() = runTest {
        vm.setIngredientsToAddToShoppingList(TestData.ingredientsToAddToShoppingList_triggerError)
        fakeAuthenticationManager.setLoginState(false, "") // not logged in
        Assert.assertEquals(
            vm.getAllRecipes().first(),  listOf<Recipe>()
        )
    }

    @Test
    fun `getAllRecipes returns the list of a user's recipes if they are logged in`() = runTest {
        fakeAuthenticationManager.setLoginState(true, "valid_user") // not logged in
        Assert.assertEquals(
            vm.getAllRecipes().first(),
            listOf(
                TestData.recipe_salad,
                TestData.recipe_withWaterIngredients
            )
        )
    }

    @Test
    fun `getRecipeIngredientsForRecipe returns a recipe's list of recipe ingredients`() = runTest {
        Assert.assertEquals(
            vm.getRecipeIngredientsForRecipe(2L).first(),
            TestData.recipeWithRecipeIngredient_salad
        )
    }

    @Test
    fun `getRecipeTagsForRecipe returns a recipe's list of tags`() = runTest {
        Assert.assertEquals(
            vm.getRecipeTagsForRecipe(2L).first(),
            TestData.recipeWithRecipeTags_salad
        )
    }

    @Test
    fun `getIngredientById returns the ingredient under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getIngredientById(2L).first(),
            TestData.ingredient_cherry
        )
    }

    @Test
    fun `getRecipeTypeById returns the recipe type under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getRecipeTypeById(2L).first(),
            TestData.recipeType_dessert
        )
    }

    @Test
    fun `getTagByTagId returns the tag under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getTagByTagId(1L).first(),
            TestData.tag_vegan
        )
    }

    @Test
    fun `getRecipeById returns the recipe under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getRecipeById(1L).first(),
            TestData.recipe_salad
        )
    }

    @Test
    fun `getSectionById returns the recipeIngredientSection under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getSectionById(1L).first(),
            TestData.recipeIngredientSection_salad
        )
    }

    @Test
    fun `getRecipeStepsForRecipe returns the recipeWithRecipeSteps under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getRecipeById(1L).first(),
            TestData.recipe_salad
        )
    }

    @Test
    fun `getNutritionForRecipe returns the recipeNutrition under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getNutritionForRecipe(1L).first(),
            TestData.recipeNutrition_salad
        )
    }

    @Test
    fun `getAllQueriesForRecipe returns the list of recipeQuery under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getAllQueriesForRecipe(1L).first(),
            TestData.recipeQueriesList_salad
        )
    }

    @Test
    fun `getRecipeStepsForRecipe returns the list of recipeSteps under the provided id`() = runTest {
        Assert.assertEquals(
            vm.getRecipeStepsForRecipe(1L).first(),
            TestData.recipeWithRecipeSteps_salad
        )
    }

    @Test
    fun `markStepAsComplete updates the step to complete if was previously incomplete`() = runTest {
        fakeAuthenticationManager.setLoginState(true, "valid_user") // not logged in
        vm.setSelectedRecipe(TestData.recipe_salad)
        vm.markStepAsComplete(1, true)
        Assert.assertNotNull(vm.uiState.value.selectedRecipeCard)
    }

    @Test
    fun `markStepAsComplete handles invalid stepId`() = runTest {
        fakeAuthenticationManager.setLoginState(true, "valid_user") // not logged in
        vm.setSelectedRecipe(TestData.recipe_salad)
        vm.markStepAsComplete(-1, true) // invalid step
        Assert.assertNull(vm.uiState.value.selectedRecipeCard)
    }

    // also calls: build ingredients map, build recipe tags. need to do them first then:
    @Test
    fun `buildRecipeCard sets the recipe card with the UI state values if valid`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_salad)
        vm.buildRecipeCard()
        Assert.assertEquals(
            vm.uiState.value.selectedRecipeCard,
            TestData.recipeCardExpanded_salad
        )
    }

    @Test
    fun `buildRecipeCard does not set the recipe card with the UI state if it is null`() = runTest {
        // selected recipe is not set, therefore it is null.
        vm.buildRecipeCard()
        Assert.assertEquals(
            vm.uiState.value.selectedRecipeCard,
            null
        )
    }

    @Test
    fun `buildRecipeCard does not set the recipe card with the UI state if the recipeType does not exist`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_withInvalidRecipeTypeId)
        // selected recipe is not set, therefore it is null.
        vm.buildRecipeCard()
        Assert.assertEquals(
            vm.uiState.value.selectedRecipeCard,
            null
        )
    }

    // test build recipe card:
    @Test
    fun `buildIngredientsMap builds the map of ingredients from the provided recipeWithRecipeIngredients`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_salad)

        Assert.assertEquals(
            vm.buildIngredientsMap(TestData.recipeWithRecipeIngredient_salad),
            TestData.sectionsWithIngredients_salad
        )
    }

    @Test
    fun `buildIngredientsMap builds the map of ingredients from the provided recipeWithRecipeIngredients and handles unit conversion`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_withUnitToConvert)

        Assert.assertEquals(
            vm.buildIngredientsMap(TestData.recipeWithRecipeIngredient_containsML),
            TestData.sectionsWithIngredients_withML
        )
    }

    @Test
    fun `buildIngredientsMap builds the map of ingredients from the provided recipeWithRecipeIngredients and handles plurality requirement`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_salad)

        Assert.assertEquals(
            vm.buildIngredientsMap(TestData.recipeWithRecipeIngredient_shouldConvertToPlural),
            TestData.sectionsWithIngredients_afterPluralConversion
        )
    }

    @Test
    fun `buildIngredientsMap builds the map of ingredients from the provided recipeWithRecipeIngredients and handles singular unit requirement`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_salad)

        Assert.assertEquals(
            vm.buildIngredientsMap(TestData.recipeWithRecipeIngredient_shouldHaveSingularUnit),
            TestData.sectionsWithIngredients_singleUnit
        )
    }

    @Test
    fun `buildIngredientsMap builds the map of ingredients from the provided recipeWithRecipeIngredients and handles blank unit (UNIT unit)`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_salad)

        Assert.assertEquals(
            vm.buildIngredientsMap(TestData.recipeWithRecipeIngredient_withUnitUnit),
            TestData.sectionsWithIngredients_unitUnit
        )
    }

    @Test
    fun `buildIngredientsMap returns null if the recipeIngredients provided is null`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_salad)
        Assert.assertEquals(
            vm.buildIngredientsMap(null),
            null
        )
    }

    @Test
    fun `buildIngredientsMap returns null if the selectedRecipe is null`() = runTest {
        Assert.assertEquals(
            vm.buildIngredientsMap(TestData.recipeWithRecipeIngredient_salad),
            null
        )
    }

    @Test
    fun `buildRecipeTags builds the map of tags given a recipe id`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_salad)
        Assert.assertEquals(
            vm.buildRecipeTags(0),
            TestData.recipeTags_salad
        )
    }

    @Test
    fun `buildRecipeTags throws an exception if an identified tag does not exist (is null)`() = runTest {
        vm.setSelectedRecipe(TestData.recipe_salad)
        // have to use kotlin testing for this, not JUnit, because cant call suspend function from assert throws in JUnit
        assertFailsWith<NullPointerException> {
            vm.buildRecipeTags(10)
        }
    }

    @Test
    fun `getRecipeBookMap builds the recipe book map`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user") // logged in
        vm.getRecipeBookMap()
        Assert.assertEquals(vm.uiState.value.recipeBookMap, TestData.recipeBookMap_dessertSalad)
    }

    @Test
    fun `getRecipeBookMap does not build the recipe book map if the user is not logged in`() {
        fakeAuthenticationManager.setLoginState(false, "") // not logged in
        vm.getRecipeBookMap()
        Assert.assertEquals(vm.uiState.value.recipeBookMap, emptyMap<RecipeType, MutableList<RecipeCardUnexpanded>?>())
    }

    @Test
    fun `getRecipeBookMap does not build the recipe book map if one or more of the recipe types is invalid`() {
        fakeAuthenticationManager.setLoginState(true, "has_recipe_with_invalid_recipeTypeId") // not logged in
        vm.getRecipeBookMap()
        Assert.assertEquals(vm.uiState.value.recipeBookMap, null)
    }
}