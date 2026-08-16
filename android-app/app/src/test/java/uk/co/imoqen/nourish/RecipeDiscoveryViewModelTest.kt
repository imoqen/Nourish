package uk.co.imoqen.nourish

import co.uk.imoqen.testfixtures.common.FakeUiEventBus
import co.uk.imoqen.testfixtures.common.TestData
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.network.FakeApiManager
import co.uk.imoqen.testfixtures.repository.FakeRecipeRepository
import co.uk.imoqen.testfixtures.repository.FakeTagRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.imoqen.nourish.common.NourishColour
import uk.co.imoqen.nourish.common.RecipeActions
import uk.co.imoqen.nourish.common.tag.FilterState
import uk.co.imoqen.nourish.common.tag.TagSheetAction
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.recipebook.TagSelection
import uk.co.imoqen.nourish.recipediscovery.LlmChatStage
import uk.co.imoqen.nourish.recipediscovery.LlmMode
import uk.co.imoqen.nourish.recipediscovery.Mipmap
import uk.co.imoqen.nourish.recipediscovery.RecipeDiscoveryViewModel
import uk.co.imoqen.nourish.setup.MainDispatcherRule

/**
 * Unit tests for the RecipeDiscovery ViewModel
 */
class RecipeDiscoveryViewModelTest {

    val fakeAuthenticationManager = FakeAuthenticationManager()
    val fakeApiManager = FakeApiManager()
    lateinit var fakeUiEventBus: FakeUiEventBus
    val fakeRecipeRepository = FakeRecipeRepository()
    val fakeTagRepository = FakeTagRepository()


    lateinit var vm: RecipeDiscoveryViewModel

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
        vm = RecipeDiscoveryViewModel(
            fakeAuthenticationManager, fakeApiManager, fakeUiEventBus,
            fakeRecipeRepository, recipeActions
        )
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Trivial tests for setters, still required for good coverage:

    @Test
    fun `should set llmGeneratedRecipe correctly`() {
        Assert.assertNull(vm.uiState.value.llmGeneratedRecipe)
        vm.setLlmGeneratedRecipe(TestData.llmRecipe_salad)
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, TestData.llmRecipe_salad)
    }

    @Test
    fun `should set userRequest correctly`() {
        Assert.assertEquals(vm.uiState.value.userRequest, "")
        vm.setUserRequest("a healthy salad")
        Assert.assertEquals(vm.uiState.value.userRequest, "a healthy salad")
    }

    @Test
    fun `should set savedImageBitmap correctly`() {
        Assert.assertEquals(vm.uiState.value.savedImageMipmap, Mipmap("", ""))
        vm.setSavedImageMipmap(Mipmap("lowquality.png", "highquality.png"))
        Assert.assertEquals(vm.uiState.value.savedImageMipmap, Mipmap("lowquality.png", "highquality.png"))
    }

    @Test
    fun `should set userChoice correctly`() {
        Assert.assertNull(vm.uiState.value.userChoice)
        vm.setUserChoice(LlmMode.FROM_IMAGE)
        Assert.assertEquals(vm.uiState.value.userChoice, LlmMode.FROM_IMAGE)
        vm.setUserChoice(LlmMode.FROM_DESCRIPTION)
        Assert.assertEquals(vm.uiState.value.userChoice, LlmMode.FROM_DESCRIPTION)
        vm.setUserChoice(LlmMode.FROM_INGREDIENTS)
        Assert.assertEquals(vm.uiState.value.userChoice, LlmMode.FROM_INGREDIENTS)
    }

    @Test
    fun `should set requestType correctly`() {
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.RECIPE) // always starts as RECIPE as this is the default
        vm.setRequestType(LlmRequestType.DISHES_FROM_IMAGE)
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.DISHES_FROM_IMAGE)
        vm.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS)
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.DISHES_FROM_INGREDIENTS)
        vm.setRequestType(LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE)
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE)
        vm.setRequestType(LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH)
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH)
    }

    @Test
    fun `should set placeholderText correctly`() {
        Assert.assertEquals(vm.uiState.value.placeholderText, "")
        vm.setPlaceholderText("this is my placeholder text")
        Assert.assertEquals(vm.uiState.value.placeholderText, "this is my placeholder text")
    }

    @Test
    fun `should set dishes correctly`() {
        Assert.assertNull(vm.uiState.value.dishes)
        vm.setDishes(TestData.llmDishesProposed_cherry)
        Assert.assertEquals(vm.uiState.value.dishes, TestData.llmDishesProposed_cherry)
    }

    @Test
    fun `should set displayImage correctly`() {
        Assert.assertEquals(vm.uiState.value.displayImage, false)
        vm.setDisplayImage(true)
        Assert.assertEquals(vm.uiState.value.displayImage, true)
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
    fun `should set recipeTypes correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeTypes, null)
        vm.setRecipeTypes(TestData.recipeTypes)
        Assert.assertEquals(vm.uiState.value.recipeTypes, TestData.recipeTypes)
    }

    @Test
    fun `should set showRestartConfirmationDialog correctly`() {
        Assert.assertEquals(vm.uiState.value.showRestartConfirmationDialog, false)
        vm.setShowRestartConfirmationDialog(true)
        Assert.assertEquals(vm.uiState.value.showRestartConfirmationDialog, true)
    }

    @Test
    fun `should set recipeSaved correctly`() {
        Assert.assertEquals(vm.uiState.value.recipeSaved, false)
        vm.setRecipeSaved(true)
        Assert.assertEquals(vm.uiState.value.recipeSaved, true)
    }

    @Test
    fun `should set selectedRecipeType correctly`() {
        Assert.assertNull(vm.uiState.value.selectedRecipeType)
        vm.setSelectedRecipeType(TestData.recipeType_breakfast)
        Assert.assertEquals(vm.uiState.value.selectedRecipeType, TestData.recipeType_breakfast)
    }

    @Test
    fun `should set displayRecipeTypeSelector correctly`() {
        Assert.assertEquals(vm.uiState.value.displayRecipeTypeSelector, false)
        vm.setDisplayRecipeTypeSelector(true)
        Assert.assertEquals(vm.uiState.value.displayRecipeTypeSelector, true)
    }

    @Test
    fun `should set visible correctly`() {
        Assert.assertEquals(vm.uiState.value.visible, true) // visible is for smooth transition when restart occurs. so it is visible by default.
        vm.setVisible(false)
        Assert.assertEquals(vm.uiState.value.visible, false)
    }

    @Test
    fun `should set chosenDish correctly`() {
        Assert.assertEquals(vm.uiState.value.chosenDish, "")
        vm.setChosenDish("Spaghetti Carbonara")
        Assert.assertEquals(vm.uiState.value.chosenDish, "Spaghetti Carbonara")
    }

    @Test
    fun `should set llmChatStage correctly`() {
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_SELECT_OPTION) // initially, user has to choose which to do (from image, from ingredients, or just recipe)
        vm.setLlmChatStage(LlmChatStage.USER_INPUT)
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_INPUT)
    }

    @Test
    fun `updateToInputStageGivenOptions should update state correctly when no image can be uploaded (FROM_DESCRIPTION)`() {
        vm.updateToInputStageGivenOptions(
            LlmMode.FROM_DESCRIPTION, "Enter a dish", LlmRequestType.RECIPE
        )
        Assert.assertEquals(vm.uiState.value.userChoice, LlmMode.FROM_DESCRIPTION)
        Assert.assertEquals(vm.uiState.value.placeholderText, "Enter a dish")
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.RECIPE)
        // no image can be uploaded:
        Assert.assertEquals(vm.uiState.value.savedImageMipmap, Mipmap("", ""))
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_INPUT)
    }

    @Test
    fun `updateToInputStageGivenOptions should update state correctly when an image can be uploaded (FROM_DESCRIPTION)`() {
        // Image upload:
        vm.updateToInputStageGivenOptions(
            LlmMode.FROM_IMAGE, "Upload an image", LlmRequestType.DISHES_FROM_IMAGE
        )
        Assert.assertEquals(vm.uiState.value.userChoice, LlmMode.FROM_IMAGE)
        Assert.assertEquals(vm.uiState.value.placeholderText, "Upload an image")
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.DISHES_FROM_IMAGE)
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_INPUT)

        vm.updateToInputStageGivenOptions(
            LlmMode.FROM_INGREDIENTS, "Enter ingredient(s) or image", LlmRequestType.DISHES_FROM_INGREDIENTS
        )
        Assert.assertEquals(vm.uiState.value.userChoice, LlmMode.FROM_INGREDIENTS)
        Assert.assertEquals(vm.uiState.value.placeholderText, "Enter ingredient(s) or image")
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.DISHES_FROM_INGREDIENTS)
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_INPUT)
    }

    @Test
    fun `makeLlmApiCall should handle the possible dishes generation case and empty selected tags`() {
        Assert.assertEquals(vm.uiState.value.dishes, null)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // for valid token
        vm.makeLlmApiCall(LlmRequestType.DISHES_FROM_IMAGE, imageFilename = "image.png")
        Assert.assertEquals(vm.uiState.value.dishes, TestData.llmDishesProposed_pasta)
    }

    @Test
    fun `makeLlmApiCall should not update dishes if an image was not uploaded, but image option chosen`() {
        Assert.assertEquals(vm.uiState.value.dishes, null)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // for valid token
        vm.makeLlmApiCall(LlmRequestType.DISHES_FROM_IMAGE) // no image filename specified
        Assert.assertEquals(vm.uiState.value.dishes, null) // the dishes are not updated; error occurred.
    }

    @Test
    fun `makeLlmApiCall should not make the call if the token is null`() {
        vm.setChosenDish("Salad")
        fakeAuthenticationManager.setLoginState(false) // for invalid token
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
        vm.makeLlmApiCall(LlmRequestType.RECIPE, imageFilename = "image.png")
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null) // no recipe is generated.
    }

    @Test
    fun `makeLlmApiCall should not make the call if an image is not provided, and recipe from image of dish is requested`() {
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // for valid token
        vm.setRecipeTags(TestData.recipeTags) // populated with some recipe tags
        vm.makeLlmApiCall(LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH)
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
    }

    @Test
    fun `makeLlmApiCall should not make the call in recipe from description if no description is given`() {
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // for valid token
        vm.setRecipeTags(TestData.recipeTags) // populated with some recipe tags
        vm.setUserChoice(LlmMode.FROM_DESCRIPTION)
        vm.makeLlmApiCall(LlmRequestType.RECIPE)
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
    }

    @Test
    fun `makeLlmApiCall should handle the recipe from description if description is given`() {
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // for valid token
        vm.setRecipeTags(TestData.recipeTags) // populated with some recipe tags
        vm.setUserChoice(LlmMode.FROM_DESCRIPTION)
        vm.setUserRequest("Salad")
        vm.makeLlmApiCall(LlmRequestType.RECIPE)
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, TestData.llmRecipe_salad)
    }

    @Test
    fun `makeLlmApiCall should not make the call if the token is invalid`() {
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
        vm.setRecipeTags(TestData.recipeTags) // populated with some recipe tags
        vm.setUserChoice(LlmMode.FROM_DESCRIPTION)
        vm.setSelectedRecipeType(TestData.recipeType_breakfast)
        vm.setUserRequest("Eggs, Milk, Butter, Flour")
        fakeAuthenticationManager.setLoginState(true, "invalid") // invalid token
        vm.makeLlmApiCall(LlmRequestType.DISHES_FROM_INGREDIENTS)
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
    }

    @Test
    fun `makeLlmApiCall handles dishesNotPossible appropriately`() {
        Assert.assertEquals(vm.uiState.value.dishes, null)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // for valid token
        vm.setRecipeTags(TestData.recipeTags) // populated with some recipe tags
        vm.setUserChoice(LlmMode.FROM_DESCRIPTION)
        vm.setSelectedRecipeType(TestData.recipeType_breakfast)
        vm.setChosenDish("socks")
        // these will generate "dishes not possible" for the dishes
        vm.makeLlmApiCall(LlmRequestType.DISHES_FROM_INGREDIENTS)
        Assert.assertEquals(vm.uiState.value.dishes, null)
        vm.makeLlmApiCall(LlmRequestType.DISHES_FROM_INGREDIENTS_IMAGE, "image.png")
        Assert.assertEquals(vm.uiState.value.dishes, null)
    }

    @Test
    fun `makeLlmApiCall handles recipeNotPossible appropriately`() {
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
        fakeAuthenticationManager.setLoginState(true, "valid_user") // for valid token
        vm.setChosenDish("Salad")
        vm.setRecipeTags(TestData.recipeTags) // populated with some recipe tags
        vm.makeLlmApiCall(LlmRequestType.RECIPE_FROM_IMAGE_AND_DISH, imageFilename = "image.png")
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null) // simulates recipe not possible
    }

    @Test
    fun `resetAfterError sets the values correctly`() {
        vm.resetAfterError()
        Assert.assertEquals(vm.uiState.value.dishes, null)
        Assert.assertEquals(vm.uiState.value.chosenDish, "")
    }

    @Test
    fun `displayBlankErrorInput displays error message and sets the llm chat stage back to USER_INPUT`() {
        vm.displayBlankInputError(LlmRequestType.RECIPE)
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_INPUT)
        vm.displayBlankInputError(LlmRequestType.DISHES_FROM_INGREDIENTS)
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_INPUT)
        vm.displayBlankInputError(LlmRequestType.DISHES_FROM_IMAGE)
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_INPUT)
    }

    @Test
    fun `addRecipe does not add the recipe to the database if the user is not logged in`() {
        fakeAuthenticationManager.setLoginState(false, "")
        Assert.assertEquals(vm.uiState.value.recipeSaved, false)
        vm.addRecipe(TestData.llmRecipe_salad)
        Assert.assertEquals(vm.uiState.value.recipeSaved, false) // not saved
    }

    @Test
    fun `addRecipe adds a llmRecipe to the database if the recipe is valid`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        Assert.assertEquals(vm.uiState.value.recipeSaved, false)
        vm.addRecipe(TestData.llmRecipe_salad)
        Assert.assertEquals(vm.uiState.value.recipeSaved, true) // not saved
    }

    @Test
    fun `restart resets all of the relevant UI values for recipe generation`() {
        vm.restart()
        Assert.assertEquals(vm.uiState.value.userRequest, "")
        Assert.assertEquals(vm.uiState.value.chosenDish, "")
        Assert.assertEquals(vm.uiState.value.userChoice, null)
        Assert.assertEquals(vm.uiState.value.dishes, null)
        Assert.assertEquals(vm.uiState.value.selectedRecipeType, null)
        Assert.assertEquals(vm.uiState.value.llmChatStage, LlmChatStage.USER_SELECT_OPTION)
        Assert.assertEquals(vm.uiState.value.requestType, LlmRequestType.RECIPE)
        Assert.assertEquals(vm.uiState.value.savedImageMipmap, Mipmap("",""))
        Assert.assertEquals(vm.uiState.value.placeholderText, "")
        Assert.assertEquals(vm.uiState.value.llmGeneratedRecipe, null)
        Assert.assertEquals(vm.uiState.value.recipeSaved, false)
    }

    @Test
    fun `formatIngredientText returns empty string for invalid ingredient`() {
        Assert.assertEquals(vm.formatIngredientText(TestData.llmIngredient_invalidUnit), "")
    }

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