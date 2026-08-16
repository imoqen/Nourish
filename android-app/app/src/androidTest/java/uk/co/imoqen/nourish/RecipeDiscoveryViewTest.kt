package uk.co.imoqen.nourish

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.firebase.FakeFirestoreManager
import co.uk.imoqen.testfixtures.network.FakeApiManager
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.ingredient.IngredientDao
import uk.co.imoqen.nourish.db.recipe.RecipeDao
import uk.co.imoqen.nourish.db.recipe.recipeingredient.RecipeIngredientDao
import uk.co.imoqen.nourish.db.recipe.recipeingredientsection.RecipeIngredientSectionDao
import uk.co.imoqen.nourish.db.recipe.recipenutrition.RecipeNutritionDao
import uk.co.imoqen.nourish.db.recipe.recipequery.RecipeQueryDao
import uk.co.imoqen.nourish.db.recipe.recipestep.RecipeStepDao
import uk.co.imoqen.nourish.db.recipe.recipetag.RecipeTagDao
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeTypeDao
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryDao
import uk.co.imoqen.nourish.db.tag.TagDao
import uk.co.imoqen.nourish.db.user.UserDao
import uk.co.imoqen.nourish.firebase.FirebaseModule
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationModule
import uk.co.imoqen.nourish.firebase.firestore.FirestoreManager
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.apimanager.ApiManagerModule
import javax.inject.Inject

/**
 * Instrumented tests for the RecipeDiscovery View
 */
@UninstallModules(AuthenticationModule::class, FirebaseModule::class, ApiManagerModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecipeDiscoveryViewTest {

    @Inject
    lateinit var uiEventBus: UiEventBus

    // Fakes of the managers / services which call outside APIs thus must be faked:
    @BindValue
    @JvmField
    val authenticationManager: AuthenticationManager = FakeAuthenticationManager()

    @BindValue
    @JvmField
    val firestoreManager: FirestoreManager = FakeFirestoreManager()

    @BindValue
    @JvmField
    val apiManager: ApiManager = FakeApiManager()

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var db: NourishDatabase // in-memory Room database
    // set up all of the DAOs:
    @Inject lateinit var recipeDao: RecipeDao
    @Inject lateinit var recipeIngredientDao: RecipeIngredientDao
    @Inject lateinit var recipeTypeDao: RecipeTypeDao
    @Inject lateinit var userDao: UserDao
    @Inject lateinit var ingredientDao: IngredientDao
    @Inject lateinit var recipeIngredientSectionDao: RecipeIngredientSectionDao
    @Inject lateinit var recipeTagDao: RecipeTagDao
    @Inject lateinit var tagDao: TagDao
    @Inject lateinit var recipeStepDao: RecipeStepDao
    @Inject lateinit var recipeNutritionDao: RecipeNutritionDao
    @Inject lateinit var recipeQueryDao: RecipeQueryDao
    @Inject lateinit var shoppingListEntryDao: ShoppingListEntryDao

    @Before
    fun init() {
        hiltRule.inject()

        runBlocking {
            // can run setup things here like to run dao commands to insert sample data etc
        }

        // log in before each test:
        signIn()
    }

    @After
    fun teardownDb() {
        db.close() // close the in-memory Room database, to tear it down between each test.
    }

    // To sign in before each test:
    fun signIn() {
        composeRule
            .onNodeWithText("Enter email address")
            .performTextInput("test@gmail.com")
        composeRule
            .onNodeWithText("Enter password")
            .performTextInput("secure_password:D")
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).performClick()

        // Sign in success and user can now see the app contents:
        composeRule.onNodeWithText("Sign in successful.").assertExists()
    }

    @Test
    fun `Recipe generation by Dish or Description works correctly`() {
        composeRule.onNode(
            hasText("Dish or Description")
        ).performClick()

        // Will not send if the input is blank:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // Will send if the user enters something:
        composeRule
            .onNodeWithText("Enter dish or description")
            .performTextInput("Healthy salad")
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        composeRule.onRoot(useUnmergedTree = true).printToLog("TEST")

        composeRule
            .onNodeWithTag("recipeDiscoveryLazyColumn")
            .performScrollToNode(
                hasText("Healthy Salad", substring = true)
            )

        composeRule
            .onNodeWithText("Healthy Salad", substring = true, useUnmergedTree = true)
            .assertExists()

        // Ingredients are generated
        composeRule
            .onNodeWithText("For the salad", useUnmergedTree = true)
            .assertExists()
        composeRule
            .onNodeWithText("0.25 cucumber")
            .assertExists()
        composeRule
            .onNodeWithText("100 g tomatoes")
            .assertExists()
        composeRule
            .onNodeWithText("4 leaves of lettuce")
            .assertExists()

        // Method is generated
        composeRule
            .onNodeWithText("First, wash the vegetables.")
            .assertExists()
        composeRule
            .onNodeWithText("Cut the vegetables.")
            .assertExists()

        composeRule
            .onNodeWithTag("recipeDiscoveryLazyColumn")
            .performScrollToNode(
                hasText("Save Recipe", substring = true)
            )

        // User can then save the recipe and view it in their saved recipes
        composeRule
            .onNodeWithText("Save Recipe")
            .performClick()

        composeRule
            .onNodeWithContentDescription("Go to view recipes", useUnmergedTree = true)
            .performClick()

        // As it is the first time loading the recipes in this instance, the loading bar appears.
        // This lasts 5 seconds, hence a 6 second delay is provided to ensure it has finished loading.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithTag("loadingBar")
                .fetchSemanticsNodes().isEmpty()
        }

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN")

        // the recipe has been saved:
        composeRule
            .onNodeWithText("Healthy Salad")
            .assertExists()
    }

    @Test
    fun `Recipe generation by Photo of a Dish works correctly and then handles recipeNotPossible case`() {
        composeRule.onNode(
            hasText("Photo of a Dish")
        ).performClick()

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        composeRule.onNodeWithTag("selectImage")
            .assertIsDisplayed()
            .performClick()

        // to select an image
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        device.waitForIdle()

        // Pick the photo and wait for it to be updated in the UI:
        device.wait(Until.hasObject(By.pkg("com.google.android.providers.media.module")), 5_000)
        val image = device.findObject(
            By.descContains("Photo taken on") // uses the accessibility tag to identify the photo. chooses the first.
        )
        val clickableParent = image?.parent
        clickableParent?.click()
        device.waitForIdle()
        device.wait(
            Until.hasObject(By.pkg(composeRule.activity.packageName)),
            5_000
        )
        composeRule.waitForIdle()

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        composeRule
            .onNodeWithTag("recipeDiscoveryLazyColumn")
            .performScrollToNode(
                hasText("Pasta Pesto", substring = true)
            )

        composeRule.onNode(
            hasText("Pasta Pesto")
        ).performClick()

        composeRule
            .onNodeWithText("A valid recipe could not be generated. Please try another dish.")
            .assertExists()

    }

    @Test
    fun `Recipe generation by List of Ingredients works correctly and handles recipeNotPossible case`() {
        composeRule.onNode(
            hasText("List or Photo of Ingredients")
        ).performClick()

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // Enter ingredients:
        composeRule
            .onNodeWithText("Enter ingredients")
            .performTextInput("Pasta, pesto, tomatoes, bacon, cheese, eggs, parmesan")
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        composeRule
            .onNodeWithTag("recipeDiscoveryLazyColumn")
            .performScrollToNode(
                hasText("Pasta Pesto", substring = true)
            )

        composeRule.onNode(
            hasText("Pasta Pesto")
        ).performClick()

        composeRule
            .onNodeWithText("Generate a recipe for")
            .assertExists()

        composeRule
            .onNode(hasText("Using ingredients: Pasta, pesto, tomatoes, bacon, cheese, eggs, parmesan", substring = true))
            .assertExists()
    }

    @Test
    fun `Recipe generation by Photo of Ingredients works correctly and handles recipeNotPossible case for potential dishes`() {
        composeRule.onNode(
            hasText("List or Photo of Ingredients")
        ).performClick()

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        composeRule.onNodeWithTag("selectImage")
            .assertIsDisplayed()
            .performClick()

        // to select an image
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        device.waitForIdle()

        // Pick the photo and wait for it to be updated in the UI:
        device.wait(Until.hasObject(By.pkg("com.google.android.providers.media.module")), 5_000)
        val image = device.findObject(
            By.descContains("Photo taken on") // uses the accessibility tag to identify the photo. chooses the first.
        )
        val clickableParent = image?.parent
        clickableParent?.click()
        device.waitForIdle()
        device.wait(
            Until.hasObject(By.pkg(composeRule.activity.packageName)),
            5_000
        )
        composeRule.waitForIdle()

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        composeRule
            .onNodeWithText("Valid dishes could not be generated. Please enter something different.")
            .assertExists()

    }

    @Test
    fun `Recipe generation works with tags and meal type applied`() {

        // to identify model bottom sheet, to close it when required:
        val hasDismissAction =
            SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)

        composeRule.onNode(
            hasText("Dish or Description")
        ).performClick()

        // Will not send if the input is blank:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // Will send if the user enters something:
        composeRule
            .onNodeWithText("Enter dish or description")
            .performTextInput("Healthy salad")

        // Select dish type:
        composeRule
            .onNodeWithText("Dish Type: Any")
            .performClick()
        composeRule
            .onNodeWithText("Lunch")
            .performClick()
        // dismiss the model bottom sheet:
        composeRule
            .onNode(hasDismissAction, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.Dismiss)

        // Filter by nutritional tags: (low calorie and vegetarian)
        composeRule
            .onNodeWithContentDescription("Leading Icon") // the filter icon is the leading icon.
            .performClick()
        composeRule
            .onNodeWithText("Low calorie")
            .performClick()
        composeRule
            .onNodeWithText("Vegan")
            .performClick()

        // dismiss the modal bottom sheet:
        composeRule
            .onNode(hasDismissAction, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.Dismiss)

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // check that the meal type and tags were applied:
        composeRule
            .onNodeWithText("Low calorie")
            .assertExists()
        composeRule
            .onNodeWithText("Vegan")
            .assertExists()
        composeRule
            .onNodeWithText("Dish type: Lunch")
            .assertExists()

        composeRule
            .onNodeWithTag("recipeDiscoveryLazyColumn")
            .performScrollToNode(
                hasText("Healthy Salad", substring = true)
            )

        composeRule
            .onNodeWithText("Healthy Salad", substring = true, useUnmergedTree = true)
            .assertExists()

        // Ingredients are generated
        composeRule
            .onNodeWithText("For the salad", useUnmergedTree = true)
            .assertExists()
        composeRule
            .onNodeWithText("0.25 cucumber")
            .assertExists()
        composeRule
            .onNodeWithText("100 g tomatoes")
            .assertExists()
        composeRule
            .onNodeWithText("4 leaves of lettuce")
            .assertExists()

        // Method is generated
        composeRule
            .onNodeWithText("First, wash the vegetables.")
            .assertExists()
        composeRule
            .onNodeWithText("Cut the vegetables.")
            .assertExists()

        composeRule
            .onNodeWithTag("recipeDiscoveryLazyColumn")
            .performScrollToNode(
                hasText("Save Recipe", substring = true)
            )

        // User can then save the recipe and view it in their saved recipes
        composeRule
            .onNodeWithText("Save Recipe")
            .performClick()

        composeRule
            .onNodeWithContentDescription("Go to view recipes", useUnmergedTree = true)
            .performClick()

        // As it is the first time loading the recipes in this instance, the loading bar appears.
        // This lasts 5 seconds, hence a 6 second delay is provided to ensure it has finished loading.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithTag("loadingBar")
                .fetchSemanticsNodes().isEmpty()
        }

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN")

        // the recipe has been saved:
        composeRule
            .onNodeWithText("Healthy Salad")
            .assertExists()

        // the tags and type has been applied:
        composeRule
            .onNodeWithText("Low calorie")
            .assertExists()
        composeRule
            .onNodeWithText("Vegan")
            .assertExists()
        composeRule
            .onNodeWithText("Lunch")
            .assertExists()
    }

    // tests related to recipe actions:
    @Test
    fun `Tag can be created and added in the recipe discovery view`() {

        // to identify modal bottom sheet, to close it when required:
        val hasDismissAction =
            SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)

        composeRule.onNode(
            hasText("Dish or Description")
        ).performClick()

        // Will not send if the input is blank:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // Make a nutritional tag and add it:
        composeRule
            .onNodeWithContentDescription("Leading Icon") // the filter icon is the leading icon.
            .performClick()
        composeRule
            .onNodeWithText("Add your own")
            .performClick()
        composeRule
            .onNodeWithText("Enter a description, e.g. 'Vegan'")
            .performTextInput("My custom tag")
        composeRule
            .onNodeWithText("Pink")
            .performClick()
        composeRule
            .onNodeWithText("Create tag")
            .performClick()
        // add the new tag:
        composeRule
            .onNodeWithText("My custom tag")
            .performClick()

        // dismiss the modal bottom sheet:
        composeRule
            .onNode(hasDismissAction, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.Dismiss)

        // tag has been added:
        composeRule
            .onNodeWithText("My custom tag")
            .assertExists()
    }

    // tests related to recipe actions:
    @Test
    fun `Tag cannot be created and added in the recipe discovery view if no description is entered`() {

        composeRule.onNode(
            hasText("Dish or Description")
        ).performClick()

        // Will not send if the input is blank:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // Make a nutritional tag and add it:
        composeRule
            .onNodeWithContentDescription("Leading Icon") // the filter icon is the leading icon.
            .performClick()
        composeRule
            .onNodeWithText("Add your own")
            .performClick()
        composeRule
            .onNodeWithText("Enter a description, e.g. 'Vegan'")
            .performTextInput(" ") // blank
        composeRule
            .onNodeWithText("Pink")
            .performClick()
        composeRule
            .onNodeWithText("Create tag")
            .performClick()
        composeRule
            .onNodeWithText("Please enter a description.")
            .performClick()
    }

    @Test
    fun `Tag cannot be created and added in the recipe discovery view if no colour is entered`() {

        composeRule.onNode(
            hasText("Dish or Description")
        ).performClick()

        // Will not send if the input is blank:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // Make a nutritional tag and add it:
        composeRule
            .onNodeWithContentDescription("Leading Icon") // the filter icon is the leading icon.
            .performClick()
        composeRule
            .onNodeWithText("Add your own")
            .performClick()
        composeRule
            .onNodeWithText("Enter a description, e.g. 'Vegan'")
            .performTextInput("My test tag")
        composeRule
            .onNodeWithText("Create tag")
            .performClick()
        composeRule
            .onNodeWithText("Please select a colour.")
            .performClick()
    }

    @Test
    fun `Tag can be discarded when creating in the recipe discovery view`() {

        composeRule.onNode(
            hasText("Dish or Description")
        ).performClick()

        // Will not send if the input is blank:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // Make a nutritional tag and add it:
        composeRule
            .onNodeWithContentDescription("Leading Icon") // the filter icon is the leading icon.
            .performClick()
        composeRule
            .onNodeWithText("Add your own")
            .performClick()
        composeRule
            .onNodeWithText("Enter a description, e.g. 'Vegan'")
            .performTextInput("My test tag")
        composeRule
            .onNodeWithText("Purple")
            .performClick()

        composeRule
            .onNodeWithText("Discard tag")
            .performClick()

        // tag not created:
        composeRule
            .onNodeWithText("My test tag")
            .assertDoesNotExist()
    }

    @Test
    fun `Tag can be deleted in the recipe discovery view`() {

        composeRule.onNode(
            hasText("Dish or Description")
        ).performClick()

        // Will not send if the input is blank:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // delete the first tag:
        composeRule
            .onNodeWithContentDescription("Leading Icon") // the filter icon is the leading icon.
            .performClick()
        composeRule
            .onNodeWithText("Edit")
            .performClick()
        composeRule
            .onAllNodesWithContentDescription("Tag Trailing Icon").onFirst() // delete the first tag: Dairy-free
            .performClick()
        // confirm deletion:
        composeRule
            .onNodeWithText("Yes")
            .performClick()

        // tag deleted:
        composeRule
            .onNodeWithText("Dairy-free")
            .assertDoesNotExist()

        composeRule
            .onNodeWithText("Back")
            .performClick()

        composeRule
            .onNodeWithText("Dairy-free")
            .assertDoesNotExist()
    }

}