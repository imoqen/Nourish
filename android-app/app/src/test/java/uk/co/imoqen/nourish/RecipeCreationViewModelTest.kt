package uk.co.imoqen.nourish

import android.content.Context
import co.uk.imoqen.testfixtures.common.FakeUiEventBus
import co.uk.imoqen.testfixtures.common.TestData
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.network.FakeApiManager
import co.uk.imoqen.testfixtures.repository.FakeIngredientRepository
import co.uk.imoqen.testfixtures.repository.FakeRecipeRepository
import co.uk.imoqen.testfixtures.repository.FakeTagRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.ingredient.IngredientSheetAction
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.common.tag.TagSheetAction
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import uk.co.imoqen.nourish.network.response.LlmIngredient
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipecreation.CreationMode
import uk.co.imoqen.nourish.recipecreation.RecipeCreationViewModel
import uk.co.imoqen.nourish.recipecreation.RecipeStep
import uk.co.imoqen.nourish.setup.MainDispatcherRule
import java.io.File
import java.util.UUID
import kotlin.collections.listOf

/**
 * Unit tests for the RecipeCreation ViewModel
 *
 * @author Imogen Pemberton, 2026
 */
class RecipeCreationViewModelTest {

    val fakeAuthenticationManager = FakeAuthenticationManager()
    val fakeApiManager = FakeApiManager()
    lateinit var fakeUiEventBus: FakeUiEventBus
    val fakeRecipeRepository = FakeRecipeRepository()
    val fakeTagRepository = FakeTagRepository()
    val fakeIngredientRepository = FakeIngredientRepository()

    lateinit var vm: RecipeCreationViewModel

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
        vm = RecipeCreationViewModel(
            fakeAuthenticationManager, fakeUiEventBus, fakeRecipeRepository, recipeActions,
            fakeTagRepository, fakeIngredientRepository
        )
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Trivial tests for setters, still required for good coverage:

    @Test
    fun `should set showBackConfirmation correctly`() {
        Assert.assertEquals(vm.uiState.value.showBackConfirmation, false)
        vm.setShowBackConfirmation(true)
        Assert.assertEquals(vm.uiState.value.showBackConfirmation, true)
    }

    @Test
    fun `should set creationMode correctly`() {
        Assert.assertEquals(vm.uiState.value.creationMode, CreationMode.ADD)
        vm.setCreationMode(CreationMode.EDIT)
        Assert.assertEquals(vm.uiState.value.creationMode, CreationMode.EDIT)
    }

    @Test
    fun `should set recipeEditId correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeEditId, -1)
        vm.setRecipeEditId(2)
        Assert.assertEquals(vm.uiState.value.recipeEditId, 2)
    }

    @Test
    fun `should set recipeCreated correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeCreated, false)
        vm.setRecipeCreated(true)
        Assert.assertEquals(vm.uiState.value.recipeCreated, true)
    }

    @Test
    fun `should set recipeCreationError correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "")
        vm.setRecipeCreationError("An error occurred")
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "An error occurred")
    }

    @Test
    fun `should set imageFile correctly`() {
        Assert.assertEquals(vm.uiState.value.imageFile, "")
        vm.setImageFile("image.png")
        Assert.assertEquals(vm.uiState.value.imageFile, "image.png")
    }

    @Test
    fun `should set emoji correctly`() {
        Assert.assertEquals(vm.uiState.value.emoji, "\uD83E\uDD57") // default emoji is salad emoji
        vm.setEmoji("\uD83C\uDF5D")
        Assert.assertEquals(vm.uiState.value.emoji, "\uD83C\uDF5D")
    }

    @Test
    fun `should set showEmojiSelector correctly`() {
        Assert.assertEquals(vm.uiState.value.showEmojiSelector, false)
        vm.setShowEmojiSelector(true)
        Assert.assertEquals(vm.uiState.value.showEmojiSelector, true)
    }

    @Test
    fun `should set colour correctly`() {
        Assert.assertNull(vm.uiState.value.colour)
        vm.setColour(NourishColour.PINK)
        Assert.assertEquals(vm.uiState.value.colour, NourishColour.PINK)
    }

    @Test
    fun `should set showColourSelector correctly`() {
        Assert.assertEquals(vm.uiState.value.showColourSelector, false)
        vm.setShowColourSelector(true)
        Assert.assertEquals(vm.uiState.value.showColourSelector, true)
    }

    @Test
    fun `should set method correctly`() {
        Assert.assertEquals(vm.uiState.value.method, null)
        val randomUUID = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("First step", randomUUID)))
        Assert.assertEquals(vm.uiState.value.method, listOf(RecipeStep("First step", randomUUID)))
    }

    @Test
    fun `should set showAddMethodSheet correctly`() {
        Assert.assertEquals(vm.uiState.value.showAddMethodSheet, false)
        vm.setShowAddMethodSheet(true)
        Assert.assertEquals(vm.uiState.value.showAddMethodSheet, true)
    }

    @Test
    fun `should set stepToAdd correctly`() {
        Assert.assertEquals(vm.uiState.value.stepToAdd, "")
        vm.setStepToAdd("Another step")
        Assert.assertEquals(vm.uiState.value.stepToAdd, "Another step")
    }

    @Test
    fun `should set stepToEdit correctly`() {
        Assert.assertEquals(vm.uiState.value.stepToEdit, "")
        vm.setStepToEdit("editing a step")
        Assert.assertEquals(vm.uiState.value.stepToEdit, "editing a step")
    }

    @Test
    fun `should set stepIndexToEdit correctly`() {
        Assert.assertEquals(vm.uiState.value.stepIndexToEdit, -1)
        vm.setStepIndexToEdit(2)
        Assert.assertEquals(vm.uiState.value.stepIndexToEdit, 2)
    }

    @Test
    fun `should set stepIndexToDelete correctly`() {
        Assert.assertEquals(vm.uiState.value.stepIndexToDelete, -1)
        vm.setStepIndexToDelete(2)
        Assert.assertEquals(vm.uiState.value.stepIndexToDelete, 2)
    }

    @Test
    fun `should set title correctly`() {
        Assert.assertEquals(vm.uiState.value.title, "")
        vm.setTitle("My title")
        Assert.assertEquals(vm.uiState.value.title, "My title")
    }

    @Test
    fun `should set numberOfServings correctly`() {
        Assert.assertEquals(vm.uiState.value.numberOfServings, 1)
        vm.setNumberOfServings(5)
        Assert.assertEquals(vm.uiState.value.numberOfServings, 5)
    }

    @Test
    fun `should set recipeType correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeType, null)
        vm.setRecipeType(TestData.recipeType_dessert)
        Assert.assertEquals(vm.uiState.value.recipeType, TestData.recipeType_dessert)
    }

    @Test
    fun `should set storageGuidance correctly`() {
        Assert.assertEquals(vm.uiState.value.storageGuidance, "")
        vm.setStorageGuidance("Eat immediately")
        Assert.assertEquals(vm.uiState.value.storageGuidance, "Eat immediately")
    }

    @Test
    fun `should set displayRecipeTypeSelector correctly`() {
        Assert.assertEquals(vm.uiState.value.displayRecipeTypeSelector, false)
        vm.setDisplayRecipeTypeSelector(true)
        Assert.assertEquals(vm.uiState.value.displayRecipeTypeSelector, true)
    }

    @Test
    fun `should set ingredients correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredients, null)
        vm.setIngredients(listOf(TestData.llmIngredient_tomato, TestData.llmIngredient_lemonjuice))
        Assert.assertEquals(vm.uiState.value.ingredients, listOf(TestData.llmIngredient_tomato, TestData.llmIngredient_lemonjuice))
    }

    @Test
    fun `should set recipeTypes correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeTypes, null)
        vm.setRecipeTypes(listOf(TestData.recipeType_breakfast, TestData.recipeType_dinner))
        Assert.assertEquals(vm.uiState.value.recipeTypes, listOf(TestData.recipeType_breakfast, TestData.recipeType_dinner))
    }

    @Test
    fun `should set showAddIngredientsSheet correctly`() {
        Assert.assertEquals(vm.uiState.value.showAddIngredientSheet, false)
        vm.setShowAddIngredientSheet(true)
        Assert.assertEquals(vm.uiState.value.showAddIngredientSheet, true)
    }

    @Test
    fun `should set ingredientIndexToDelete correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientIndexToDelete, -1)
        vm.setIngredientIndexToDelete(3)
        Assert.assertEquals(vm.uiState.value.ingredientIndexToDelete, 3)
    }

    @Test
    fun `should set filter correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientIndexToDelete, -1)
        vm.setIngredientIndexToDelete(3)
        Assert.assertEquals(vm.uiState.value.ingredientIndexToDelete, 3)
    }

    @Test
    fun `should set filterDisplayState in tagSheetState correctly`() {
        Assert.assertEquals(vm.uiState.value.tagSheetState.filterDisplayState, FilterState.NONE)
        vm.setFilterDisplayState(FilterState.APPLY)
        Assert.assertEquals(vm.uiState.value.tagSheetState.filterDisplayState, FilterState.APPLY)
    }

    @Test
    fun `should set recipeTags in tagSheetState correctly`() {
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, null)
        vm.setRecipeTags(TestData.recipeTags)
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, TestData.recipeTags)
    }

    @Test
    fun `should set createdTagName in tagSheetState correctly`() {
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagName, "")
        vm.setCreatedTagName("Low Processed")
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagName, "Low Processed")
    }

    @Test
    fun `should set createdTagColour in tagSheetState correctly`() {
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagColour, null)
        vm.setCreatedTagColour(NourishColour.PINK)
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagColour, NourishColour.PINK)
    }

    @Test
    fun `should set tagCreationError in tagSheetState correctly`() {
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagCreationError, "")
        vm.setTagCreationError("The tag name cannot be blank.")
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagCreationError, "The tag name cannot be blank.")
    }

    @Test
    fun `should set showTagConfirmationDialog in tagSheetState correctly`() {
        Assert.assertEquals(vm.uiState.value.tagSheetState.showTagConfirmationDialog, false)
        vm.setShowTagConfirmationDialog(true)
        Assert.assertEquals(vm.uiState.value.tagSheetState.showTagConfirmationDialog, true)
    }

    @Test
    fun `should set tagToDelete in tagSheetState correctly`() {
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagToDelete, null)
        vm.setTagToDelete(TestData.tag_vegan)
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagToDelete, TestData.tag_vegan)
    }

    @Test
    fun `should set name in the ingredient object correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.name, "")
        vm.setIngredientToAddName("Carrot")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.name, "Carrot")
    }

    @Test
    fun `should set unit in the ingredient object correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.unit, IngredientUnit.UNIT) // unit by default
        vm.setIngredientToAddUnit(IngredientUnit.G)
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.unit, IngredientUnit.G)
    }

    @Test
    fun `should set amount in the ingredient object correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.amount, "")
        vm.setIngredientToAddAmount("500")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.amount, "500")
    }

    @Test
    fun `should set error in the ingredient object correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.error, "")
        vm.setIngredientToAddError("An error occurred")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.error, "An error occurred")
    }

    @Test
    fun `should set index in the ingredient object to edit correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientToEdit.index, -1)
        vm.setEditIngredientIndex(5)
        Assert.assertEquals(vm.uiState.value.ingredientToEdit.index, 5)
    }

    @Test
    fun `should set amount in the ingredient object to edit correctly`() {
        Assert.assertEquals(vm.uiState.value.ingredientToEdit.amount, "")
        vm.setEditIngredientAmount("25")
        Assert.assertEquals(vm.uiState.value.ingredientToEdit.amount, "25")
    }

    // Testing the exposed tag methods
    @Test
    fun `getAllTagsForUser returns all tags authored by a user`() {
        Assert.assertNull(vm.uiState.value.tagSheetState.recipeTags)
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.getAllTagsForUser()
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, listOf(
            TestData.tagSelection_lowcalorie, TestData.tagSelection_vegan
        ))
    }

    @Test
    fun `getAllRecipeTypes returns all recipe types, user independent`() {
        Assert.assertNull(vm.uiState.value.recipeTypes)
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.getAllRecipeTypes()
        Assert.assertEquals(vm.uiState.value.recipeTypes, TestData.recipeTypes)
    }

    @Test
    fun `updateSelected updates a tag's selected status`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.getAllTagsForUser()
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags?.get(1), TagSelection(TestData.tag_vegan, false))
        vm.updateSelected(TestData.tag_vegan, true)
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags?.get(1), TagSelection(TestData.tag_vegan, true))
    }

    @Test
    fun `createTag creates a tag when name and colour are valid`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setCreatedTagName("Vegetarian")
        vm.setCreatedTagColour(NourishColour.PINK)
        vm.createTag()
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagCreationError, "")
    }

    @Test
    fun `createTag does not create a tag when name is invalid`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setCreatedTagName("")
        vm.setCreatedTagColour(NourishColour.PINK)
        vm.createTag()
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagCreationError, "Please enter a description.")
    }

    @Test
    fun `createTag does not create a tag when the user is not logged in`() {
        fakeAuthenticationManager.setLoginState(false, "")
        vm.setCreatedTagName("Vegetarian")
        vm.setCreatedTagColour(NourishColour.PINK)
        vm.createTag()
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagCreationError, "The tag could not be created.")
    }

    @Test
    fun `deleteTag successfully deletes a tag`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setTagToDelete(TestData.tag_lowcalorie)
        vm.deleteTag()
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagToDelete, TestData.tag_lowcalorie)
    }

    @Test
    fun `closeTagCreation resets the UI values regarding tag creation`() {
        vm.closeTagCreation()
        Assert.assertEquals(vm.uiState.value.tagSheetState.filterDisplayState, FilterState.APPLY)
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagColour, null)
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagName, "")
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagCreationError, "")
    }

    // Tests for non-trivial (not setters), RecipeCreation-specific methods:
    @Test
    fun `alterServingSize can increase the serving size`() {
        vm.setNumberOfServings(1)
        vm.alterServingSize(true) // true for increase
        Assert.assertEquals(vm.uiState.value.numberOfServings, 2)
    }

    @Test
    fun `alterServingSize can decrease the serving size`() {
        vm.setNumberOfServings(3)
        vm.alterServingSize(false) // false for decrease
        Assert.assertEquals(vm.uiState.value.numberOfServings, 2)
    }

    @Test
    fun `alterServingSize cannot decrease the serving size below 1`() {
        vm.setNumberOfServings(1)
        vm.alterServingSize(false) // false for decrease
        Assert.assertEquals(vm.uiState.value.numberOfServings, 1) // unchanged - cant be 0 or less.
    }

    @Test
    fun `addIngredientToRecipe does not add the ingredient if the name is blank`() {
        vm.setIngredientToAddName("")
        vm.addIngredientToRecipe()
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.error, "The name cannot be blank.")
    }

    @Test
    fun `addIngredientToRecipe does not add the ingredient if the amount is blank`() {
        vm.setIngredientToAddName("Cherry")
        vm.setIngredientToAddAmount("")
        vm.addIngredientToRecipe()
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.error, "The amount cannot be blank.")
    }

    @Test
    fun `addIngredientToRecipe adds a valid ingredient and updates the ingredient state`() {
        vm.setIngredientToAddName("Cherry")
        vm.setIngredientToAddAmount("300")
        vm.setIngredientToAddUnit(IngredientUnit.G)
        vm.addIngredientToRecipe()
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.error, "") // no error
        Assert.assertEquals(vm.uiState.value.ingredients, listOf(LlmIngredient("Cherry", "Cherry", 300.0, "G")))
    }

    @Test
    fun `resetIngredientAdditionUI resets the UI state related to ingredients back to default values`() {
        vm.setIngredientToAddName("Cherry")
        vm.setIngredientToAddAmount("300")
        vm.setIngredientToAddName("G")
        vm.setIngredientToAddError("An error occurred")
        vm.setShowAddIngredientSheet(true)
        vm.resetIngredientAdditionUI()
        // then all the values get reset as a result:
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.error, "")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.name, "")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.amount, "")
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.unit, IngredientUnit.UNIT)
        Assert.assertEquals(vm.uiState.value.showAddIngredientSheet, false)
    }

    @Test
    fun `updateIngredientAmount does not update if the ingredients list has not yet been initialised`() {
        vm.setIngredients(null)
        vm.updateIngredientAmount()
        Assert.assertEquals(vm.uiState.value.ingredients, null) // no change made
    }

    @Test
    fun `updateIngredientAmount sets the amount to 1 if it is empty`() {
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber))
        vm.setEditIngredientIndex(0)
        vm.setEditIngredientAmount("")
        vm.updateIngredientAmount()
        Assert.assertEquals(vm.uiState.value.ingredients, listOf(TestData.llmIngredient_cucumber.copy(ingredientAmount = 1.0))) // no change made
    }

    @Test
    fun `updateIngredientAmount handles an invalid index`() {
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber))
        vm.setEditIngredientIndex(-10)
        vm.updateIngredientAmount()
        // makes no change:
        Assert.assertEquals(vm.uiState.value.ingredients, listOf(TestData.llmIngredient_cucumber))
    }

    @Test
    fun `updateIngredientAmount sets the amount and resets the UI if it is valid`() {
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber))
        vm.setEditIngredientIndex(0)
        vm.setEditIngredientAmount("500")
        vm.updateIngredientAmount()
        Assert.assertEquals(vm.uiState.value.ingredients, listOf(TestData.llmIngredient_cucumber.copy(ingredientAmount = 500.0))) // no change made
    }

    @Test
    fun `formatIngredientAmount rounds to the nearest quarter`() {
        Assert.assertEquals(
            vm.formatIngredientAmount(5.2),
            "5.25"
        )
    }

    @Test
    fun `formatIngredientAmount rounds to the nearest 2 decimal place`() {
        Assert.assertEquals(
            vm.formatIngredientAmount(5.135, roundToNearestQuarter = false),
            "5.14"
        )
    }

    @Test
    fun `deleteIngredient does not delete if the ingredients list has not been initialised`() {
        vm.setIngredients(null)
        vm.deleteIngredient()
        Assert.assertEquals(vm.uiState.value.ingredients, null) // no change made
    }

    @Test
    fun `deleteIngredient handles an invalid index to delete`() {
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber))
        vm.setIngredientIndexToDelete(50) // doesnt exist in the list
        vm.deleteIngredient()
        Assert.assertEquals(vm.uiState.value.ingredients, listOf(TestData.llmIngredient_cucumber)) // no change made
    }

    @Test
    fun `deleteIngredient deletes an ingredient if it is valid in the list`() {
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber))
        vm.setIngredientIndexToDelete(0)
        vm.deleteIngredient()
        Assert.assertEquals(vm.uiState.value.ingredients, listOf<LlmIngredient>()) // removed the ingredient
    }

    @Test
    fun `addRecipeStep does not add the step if it is blank`() {
        vm.setStepToAdd("")
        vm.addRecipeStep()
        Assert.assertEquals(vm.uiState.value.method, null) // no step added
    }

    @Test
    fun `addRecipeStep adds a step to an empty list`() {
        vm.setStepToAdd("Wash the cherries")
        vm.addRecipeStep()
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, listOf("Wash the cherries")) // no step added
    }

    @Test
    fun `addRecipeStep adds a step to an existing list`() {
        vm.setMethod(listOf(RecipeStep("Wash the cherries", UUID.randomUUID())))
        vm.setStepToAdd("Dry the cherries")
        vm.addRecipeStep()
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, listOf("Wash the cherries", "Dry the cherries")) // no step added
    }

    @Test
    fun `updateStep does not update the step if it is blank`() {
        vm.setMethod(listOf(RecipeStep("Wash the cherries", UUID.randomUUID())))
        vm.setStepToEdit("")
        vm.updateStep()
        // unchanged:
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, listOf("Wash the cherries"))
    }

    @Test
    fun `updateStep does not update the step if there are no steps`() {
        vm.setStepToEdit("Eat the cherries")
        vm.updateStep()
        // unchanged:
        Assert.assertEquals(vm.uiState.value.method, null) // no step added
    }

    @Test
    fun `updateStep does not update the step if the index is invalid`() {
        vm.setMethod(listOf(RecipeStep("Wash the cherries", UUID.randomUUID())))
        vm.setStepIndexToEdit(20) // doesnt exist
        vm.setStepToEdit("Eat the cherries")
        vm.updateStep()
        // unchanged:
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, listOf("Wash the cherries"))
    }

    @Test
    fun `updateStep updates the step if valid`() {
        vm.setMethod(listOf(RecipeStep("Wash the cherries", UUID.randomUUID())))
        vm.setStepIndexToEdit(0)
        vm.setStepToEdit("Eat the cherries")
        vm.updateStep()
        // changes:
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, listOf("Eat the cherries"))
    }

    @Test
    fun `deleteStep doesn't delete the step if there is no method`() {
        vm.deleteStep()
        // unchanged:
        Assert.assertEquals(vm.uiState.value.method, null)
    }

    @Test
    fun `deleteStep doesn't delete the step if it does not exist`() {
        vm.setMethod(listOf(RecipeStep("Eat the cherries", UUID.randomUUID())))
        vm.setStepIndexToDelete(10) // doesnt exist
        vm.deleteStep()
        // unchanged:
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, listOf("Eat the cherries"))
    }

    @Test
    fun `deleteStep deletes a valid step`() {
        vm.setMethod(listOf(RecipeStep("Eat the cherries", UUID.randomUUID())))
        vm.setStepIndexToDelete(0) // exists
        vm.deleteStep()
        // deletes the step:
        Assert.assertEquals(vm.uiState.value.method, listOf<RecipeStep>())
    }

    @Test
    fun `addRecipe does not add a recipe if the user is not logged in`() {
        fakeAuthenticationManager.setLoginState(false, "")
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "You must be logged in.")
    }

    @Test
    fun `addRecipe adds a recipe if the user is logged in`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber, TestData.llmIngredient_tomato))
        vm.setRecipeType(TestData.recipeType_side)
        val stepUuid = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("Wash the cherries", stepUuid)))
        vm.setTitle("Cherry Pie")
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "") // no error
    }

    @Test
    fun `addRecipe adds a recipe if the user is logged in with tags provided`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber, TestData.llmIngredient_tomato))
        vm.setRecipeType(TestData.recipeType_side)
        val stepUuid = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("Wash the cherries", stepUuid)))
        vm.setTitle("Cherry Pie")
        vm.setRecipeTags(TestData.recipeTags)
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "") // no error
    }

    @Test
    fun `addRecipe adds a recipe if the user is logged in with a colour provided`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber, TestData.llmIngredient_tomato))
        vm.setRecipeType(TestData.recipeType_side)
        val stepUuid = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("Wash the cherries", stepUuid)))
        vm.setTitle("Cherry Pie")
        vm.setColour(NourishColour.GREY)
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "") // no error
    }

    @Test
    fun `addRecipe does not add a recipe if the title is blank`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber, TestData.llmIngredient_tomato))
        vm.setRecipeType(TestData.recipeType_side)
        val stepUuid = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("Wash the cherries", stepUuid)))
        vm.setTitle("")
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "You must enter a title.")
    }

    @Test
    fun `addRecipe does not add a recipe if the title is too long`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber, TestData.llmIngredient_tomato))
        vm.setRecipeType(TestData.recipeType_side)
        val stepUuid = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("Wash the cherries", stepUuid)))
        vm.setTitle("This is a very long title which is more than 54 characters long and is therefore not allowed")
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "The title is too long. It must be less than 54 characters.")
    }

    @Test
    fun `addRecipe does not add a recipe if no ingredients are provided`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setRecipeType(TestData.recipeType_side)
        val stepUuid = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("Wash the cherries", stepUuid)))
        vm.setTitle("Cherry pie")
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "You must enter at least 1 ingredient.")
        // also if the ingredients list is set to empty:
        vm.setIngredients(listOf())
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "You must enter at least 1 ingredient.")
    }

    @Test
    fun `addRecipe does not add a recipe if the recipe type is not selected`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber, TestData.llmIngredient_tomato))
        val stepUuid = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("Wash the cherries", stepUuid)))
        vm.setTitle("Cherry pie")
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "You must select a recipe type.")
    }

    @Test
    fun `addRecipe does not add a recipe if no steps have been provided`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber, TestData.llmIngredient_tomato))
        vm.setRecipeType(TestData.recipeType_side)
        vm.setTitle("Cherry pie")
        vm.addRecipe()
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "You must enter at least 1 method step.")
        // also if the steps are is set to empty:
        vm.setMethod(listOf())
        Assert.assertEquals(vm.uiState.value.recipeCreationError, "You must enter at least 1 method step.")
    }

    @Test
    fun `updateRecipe does not update a recipe if the user is not logged in`() {
        fakeAuthenticationManager.setLoginState(false, "")
        // Mockito to mock the context:
        val mockContext = mockk<Context>()
        every { mockContext.filesDir } returns File("testDir")
        vm.updateRecipe(mockContext)
        Assert.assertEquals(vm.uiState.value.recipeCreated, false) // the recipe was not created (updated)
    }

    @Test
    fun `updateRecipe does not update a recipe if the original recipe does not exist`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        // Mockito to mock the context:
        val mockContext = mockk<Context>()
        every { mockContext.filesDir } returns File("testDir")
        vm.setRecipeEditId(-1) // to simulate recipe which does not exist
        vm.updateRecipe(mockContext)
        Assert.assertEquals(vm.uiState.value.recipeCreated, false) // the recipe was not created (updated)
    }

    @Test
    fun `updateRecipe updates a recipe successfully when all details are provided`() {
        // recipe details to update:
        vm.setIngredients(listOf(TestData.llmIngredient_cucumber, TestData.llmIngredient_tomato))
        vm.setRecipeType(TestData.recipeType_side)
        val stepUuid = UUID.randomUUID()
        vm.setMethod(listOf(RecipeStep("Wash the cherries", stepUuid)))
        vm.setTitle("Cherry Pie")
        vm.setImageFile("image.png") // to simulate image not deleted.
        // previous recipe to update:
        vm.setRecipeEditId(1) // recipe which exists

        fakeAuthenticationManager.setLoginState(true, "valid_user")
        // Mockito to mock the context:
        val mockContext = mockk<Context>()
        every { mockContext.filesDir } returns File("testDir")
        vm.updateRecipe(mockContext)
        Assert.assertEquals(vm.uiState.value.recipeCreated, true) // the recipe was not created (updated)
    }

    @Test
    fun `populateFromId fills in the UI state with a recipe's details successfully, for editing`() {
        Assert.assertEquals(vm.uiState.value.title, "")
        Assert.assertEquals(vm.uiState.value.numberOfServings, 1)
        Assert.assertEquals(vm.uiState.value.recipeType, null)
        Assert.assertEquals(vm.uiState.value.storageGuidance, "")
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, null)
        Assert.assertEquals(vm.uiState.value.ingredients, null)
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, null)
        Assert.assertEquals(vm.uiState.value.imageFile, "")
        Assert.assertEquals(vm.uiState.value.emoji, "\uD83E\uDD57") // default emoji is salad
        Assert.assertEquals(vm.uiState.value.colour, null)
        vm.populateFromId(1) // will give salad recipe
        // the recipe details are now populated:
        Assert.assertEquals(vm.uiState.value.title, "Salad Recipe") // the recipe was not created (updated)
        Assert.assertEquals(vm.uiState.value.numberOfServings, 2) // the recipe was not created (updated)
        Assert.assertEquals(vm.uiState.value.recipeType, RecipeType(3, "Dessert", 3)) // the recipe was not created (updated)
        Assert.assertEquals(vm.uiState.value.storageGuidance, "Eat immediately.")
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, TestData.recipeTagsFirstSelected)
        Assert.assertEquals(vm.uiState.value.ingredients, listOf(TestData.llmIngredient_cherry))
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, listOf("Wash the cherries"))
        Assert.assertEquals(vm.uiState.value.imageFile, "image.png")
        Assert.assertEquals(vm.uiState.value.emoji, "\uD83C\uDF5C")
        Assert.assertEquals(vm.uiState.value.colour, NourishColour.PINK)
    }

    @Test
    fun `populateFromId does not fill in the UI state if the provided recipe id is invalid`() {
        Assert.assertEquals(vm.uiState.value.title, "")
        Assert.assertEquals(vm.uiState.value.numberOfServings, 1)
        Assert.assertEquals(vm.uiState.value.recipeType, null)
        Assert.assertEquals(vm.uiState.value.storageGuidance, "")
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, null)
        Assert.assertEquals(vm.uiState.value.ingredients, null)
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, null)
        Assert.assertEquals(vm.uiState.value.imageFile, "")
        Assert.assertEquals(vm.uiState.value.emoji, "\uD83E\uDD57") // default emoji is salad
        Assert.assertEquals(vm.uiState.value.colour, null)
        vm.populateFromId(-1) // will not give valid recipe.
        // the recipe details are not populated, because the recipe id was invalid:
        Assert.assertEquals(vm.uiState.value.title, "")
        Assert.assertEquals(vm.uiState.value.numberOfServings, 1)
        Assert.assertEquals(vm.uiState.value.recipeType, null)
        Assert.assertEquals(vm.uiState.value.storageGuidance, "")
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, null)
        Assert.assertEquals(vm.uiState.value.ingredients, null)
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, null)
        Assert.assertEquals(vm.uiState.value.imageFile, "")
        Assert.assertEquals(vm.uiState.value.emoji, "\uD83E\uDD57") // default emoji is salad
        Assert.assertEquals(vm.uiState.value.colour, null)
    }

    @Test
    fun `populateFromId does not fill in the UI state if the provided recipe id has a null recipe type`() {
        Assert.assertEquals(vm.uiState.value.title, "")
        Assert.assertEquals(vm.uiState.value.numberOfServings, 1)
        Assert.assertEquals(vm.uiState.value.recipeType, null)
        Assert.assertEquals(vm.uiState.value.storageGuidance, "")
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, null)
        Assert.assertEquals(vm.uiState.value.ingredients, null)
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, null)
        Assert.assertEquals(vm.uiState.value.imageFile, "")
        Assert.assertEquals(vm.uiState.value.emoji, "\uD83E\uDD57") // default emoji is salad
        Assert.assertEquals(vm.uiState.value.colour, null)
        vm.populateFromId(50) // will give valid recipe, but invalid (null) recipe type.
        // the recipe details are not populated, because the recipe id was invalid:
        Assert.assertEquals(vm.uiState.value.title, "")
        Assert.assertEquals(vm.uiState.value.numberOfServings, 1)
        Assert.assertEquals(vm.uiState.value.recipeType, null)
        Assert.assertEquals(vm.uiState.value.storageGuidance, "")
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags, null)
        Assert.assertEquals(vm.uiState.value.ingredients, null)
        Assert.assertEquals(vm.uiState.value.method?.map { it.step }, null)
        Assert.assertEquals(vm.uiState.value.imageFile, "")
        Assert.assertEquals(vm.uiState.value.emoji, "\uD83E\uDD57") // default emoji is salad
        Assert.assertEquals(vm.uiState.value.colour, null)
    }

    @Test
    fun `onIngredientSheetAction handles ingredient sheet actions appropriately`() {
        // OnDismissRequest:
        vm.onIngredientSheetAction(IngredientSheetAction.OnDismissRequest())
        Assert.assertEquals(vm.uiState.value.showAddIngredientSheet,false)

        // OnComplete:
        vm.setIngredientToAddName("Cherry")
        vm.setIngredientToAddAmount("300")
        vm.setIngredientToAddUnit(IngredientUnit.G)
        vm.onIngredientSheetAction(IngredientSheetAction.OnComplete())
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.error, "") // no error
        Assert.assertEquals(vm.uiState.value.ingredients, listOf(LlmIngredient("Cherry", "Cherry", 300.0, "G")))

        // SetIngredientAmount:
        vm.onIngredientSheetAction(IngredientSheetAction.SetIngredientAmount("25"))
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.amount,"25")

        // SetIngredientName:
        vm.onIngredientSheetAction(IngredientSheetAction.SetIngredientName("Beef"))
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.name,"Beef")

        // SetIngredientUnit:
        vm.onIngredientSheetAction(IngredientSheetAction.SetIngredientUnit(IngredientUnit.KG))
        Assert.assertEquals(vm.uiState.value.ingredientToAdd.unit,IngredientUnit.KG)

    }

    @Test
    fun `onTagSheetAction handles actions for the tag sheet appropriately`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")

        // SetFilterDisplayState:
        vm.onTagSheetAction(TagSheetAction.SetFilterDisplayState(FilterState.APPLY))
        Assert.assertEquals(vm.uiState.value.tagSheetState.filterDisplayState, FilterState.APPLY)

        // CloseTagCreation:
        vm.onTagSheetAction(TagSheetAction.CloseTagCreation())
        Assert.assertEquals(vm.uiState.value.tagSheetState.filterDisplayState, FilterState.APPLY)
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagColour, null)
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagName, "")
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagCreationError, "")

        // CreateTag:
        vm.setCreatedTagName("Vegetarian")
        vm.setCreatedTagColour(NourishColour.PINK)
        vm.onTagSheetAction(TagSheetAction.CreateTag())
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagCreationError, "")

        // DeleteTag:
        vm.setTagToDelete(TestData.tag_lowcalorie)
        vm.onTagSheetAction(TagSheetAction.DeleteTag())
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagToDelete, TestData.tag_lowcalorie)

        // SetCreatedTagColour:
        vm.onTagSheetAction(TagSheetAction.SetCreatedTagColour(NourishColour.PURPLE))
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagColour, NourishColour.PURPLE)

        // SetCreatedTagName:
        vm.onTagSheetAction(TagSheetAction.SetCreatedTagName("High Protein"))
        Assert.assertEquals(vm.uiState.value.tagSheetState.createdTagName,"High Protein")

        // SetTagToDelete:
        vm.onTagSheetAction(TagSheetAction.SetTagToDelete(TestData.tag_vegan))
        Assert.assertEquals(vm.uiState.value.tagSheetState.tagToDelete,TestData.tag_vegan)

        // ShowTagConfirmationDialog:
        vm.onTagSheetAction(TagSheetAction.ShowTagConfirmationDialog(true))
        Assert.assertEquals(vm.uiState.value.tagSheetState.showTagConfirmationDialog,true)

        // UpdateSelected:
        vm.getAllTagsForUser()
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags?.get(0), TagSelection(TestData.tag_lowcalorie, false))
        vm.onTagSheetAction(TagSheetAction.UpdateSelected(TestData.tag_lowcalorie, true))
        Assert.assertEquals(vm.uiState.value.tagSheetState.recipeTags?.get(0), TagSelection(TestData.tag_lowcalorie, true))
    }

}