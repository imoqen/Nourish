package uk.co.imoqen.nourish

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
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
 * Instrumented tests for the RecipeCreation View
 */
@UninstallModules(AuthenticationModule::class, FirebaseModule::class, ApiManagerModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecipeCreationViewTest {

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

    @Inject
    lateinit var db: NourishDatabase // in-memory Room database

    // set up all of the DAOs:
    @Inject
    lateinit var recipeDao: RecipeDao
    @Inject
    lateinit var recipeIngredientDao: RecipeIngredientDao
    @Inject
    lateinit var recipeTypeDao: RecipeTypeDao
    @Inject
    lateinit var userDao: UserDao
    @Inject
    lateinit var ingredientDao: IngredientDao
    @Inject
    lateinit var recipeIngredientSectionDao: RecipeIngredientSectionDao
    @Inject
    lateinit var recipeTagDao: RecipeTagDao
    @Inject
    lateinit var tagDao: TagDao
    @Inject
    lateinit var recipeStepDao: RecipeStepDao
    @Inject
    lateinit var recipeNutritionDao: RecipeNutritionDao
    @Inject
    lateinit var recipeQueryDao: RecipeQueryDao
    @Inject
    lateinit var shoppingListEntryDao: ShoppingListEntryDao

    @Before
    fun init() {
        hiltRule.inject()
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

    // To add generated salad recipe before each test:
    fun generateSaladRecipe() {
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

        // the recipe has been saved:
        composeRule
            .onNodeWithText("Healthy Salad")
            .assertExists()
    }

    @Test
    fun `Recipe can be manually edited and updated`() {
        // generate the salad recipe to edit:
        generateSaladRecipe()

        // go to the healthy salad recipe:
        composeRule.onNode(
            hasText("Healthy Salad")
        ).performClick()

        // wait for the data to load from the DB:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Healthy Salad"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // scroll to and click on the edit button:
        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Edit Manually", substring = true)
            )

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN1")
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithTag("Edit Manually", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick)

        // edit all of the fields:

        // change photo, then remove photo:
        composeRule
            .onNodeWithContentDescription("Update image")
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

        // then remove it:
        composeRule
            .onNodeWithContentDescription("Delete image")
            .performClick()

        // now choose a colour and update the emoji:
        composeRule
            .onNodeWithContentDescription("Choose colour")
            .performClick()
        composeRule
            .onNodeWithText("Brown")
            .performClick()
        composeRule
            .onNodeWithText("Done", useUnmergedTree = true)
            .performClick()
        // emoji:
        composeRule
            .onNodeWithText("\uD83E\uDD57")
            .performClick()

        // scroll to burger emoji:
        composeRule
            .onNodeWithTag("emojiSelectorColumn")
            .performScrollToNode(
                hasText("\uD83C\uDF54", substring = true)
            )
        composeRule
            .onNodeWithText("\uD83C\uDF54") // select burger emoji
            .performClick()
        composeRule
            .onNodeWithText("Done", useUnmergedTree = true)
            .performClick()

        // name, type and tags:
        // to identify model bottom sheet, to close it when required:
        val hasDismissAction =
            SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)

        composeRule
            .onNodeWithText("Healthy Salad")
            .performTextClearance()
        composeRule
            .onNodeWithText("Enter Title*")
            .performTextInput("Unhealthy Burger")
        composeRule
            .onNodeWithText("Lunch")
            .performClick()
        composeRule
            .onNodeWithText("Dinner")
            .performClick()
        // dismiss the modal bottom sheet:
        composeRule
            .onNode(hasDismissAction, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.Dismiss)
        composeRule
            .onNodeWithText("Vegan")
            .performClick()
        // remove vegan and low calorie tags and add freezer-friendly:
        composeRule.onAllNodes(
            hasText("Vegan") and hasClickAction()
        )[1].performClick()
        composeRule.onAllNodes(
            hasText("Low calorie") and hasClickAction()
        )[1].performClick()
        composeRule
            .onNodeWithText("Freezer-friendly")
            .performClick()
        // close the modal bottom sheet:
        composeRule
            .onNode(hasDismissAction, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.Dismiss)
        composeRule
            .onNodeWithText("Freezer-friendly")
            .assertExists()
        // delete, edit, and add ingredients:

        // scroll to the final ingredient so all ingredients are displayed:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("pepper", substring = true)
            )

        // delete cucumber (also testing dismiss functionality of confirmation):
        composeRule
            .onAllNodesWithContentDescription("Delete ingredient")[0].performClick()
        composeRule
            .onNodeWithText("No")
            .performClick()
        composeRule
            .onAllNodesWithContentDescription("Delete ingredient")[0].performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()
        // edit tomato and lettuce:
        composeRule
            .onAllNodesWithContentDescription("Edit Ingredient Amount")[0].performClick()
        composeRule
            .onNodeWithText("100.0")
            .performTextClearance()
        composeRule
            .onNodeWithText("Enter new amount")
            .performTextInput("25")
        composeRule
            .onNodeWithText("Update Amount")
            .performClick()
        // test cancelling edit and goes to 1 if nothing entered:
        composeRule
            .onAllNodesWithContentDescription("Edit Ingredient Amount")[1].performClick()
        // close the modal bottom sheet:
        composeRule
            .onAllNodes(hasDismissAction, useUnmergedTree = true)[1]
            .performSemanticsAction(SemanticsActions.Dismiss)
        composeRule
            .onAllNodesWithContentDescription("Edit Ingredient Amount")[1].performClick()
        composeRule
            .onNodeWithText("4.0")
            .performTextClearance()
        composeRule
            .onNodeWithText("Update Amount")
            .performClick()

        // delete all of the other ingredients (dressing):
        composeRule
            .onAllNodesWithContentDescription("Delete ingredient")[2].performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()
        composeRule
            .onAllNodesWithContentDescription("Delete ingredient")[2].performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()
        composeRule
            .onAllNodesWithContentDescription("Delete ingredient")[2].performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()
        composeRule
            .onAllNodesWithContentDescription("Delete ingredient")[2].performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()
        composeRule
            .onAllNodesWithContentDescription("Delete ingredient")[2].performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()

        // add 2 ingredients and check blank inputs:
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithText("Add Ingredient", useUnmergedTree = true)
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        // error displayed:
        composeRule
            .onNodeWithText("The name cannot be blank.")
            .assertExists()
        // now fill everything in:
        composeRule
            .onNodeWithText("Enter an ingredient to add")
            .performTextInput("Burger bun")
        composeRule
            .onNodeWithText("Burger bun")
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        // error displayed:
        composeRule.onAllNodes(isRoot())[1].printToLog("BEFOER")
        composeRule
            .onNodeWithText("The amount cannot be blank.")
            .assertExists()
        composeRule
            .onAllNodesWithText("Enter amount")[1]
            .performTextInput("1")
        composeRule
            .onAllNodesWithText("1")[1]
            .performImeAction() // to press "done" on the keyboard so closes keyboard

        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()

        // add burger:
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithText("Add Ingredient", useUnmergedTree = true)
            .performClick()
        // now fill everything in:
        composeRule
            .onNodeWithText("Enter an ingredient to add")
            .performTextInput("Beef burger")
        composeRule
            .onNodeWithText("Beef burger")
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onAllNodesWithText("Enter amount")[1]
            .performTextInput("200")
        composeRule
            .onAllNodesWithText("200")[0]
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("g")
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()

        // edit the method:
        // scroll to the method first
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Add Step", substring = true)
            )
        // delete the first two
        composeRule
            .onAllNodesWithContentDescription("Edit step")[0].performClick()
        composeRule.onRoot(useUnmergedTree = true).printToLog("EDITSTEP")
        composeRule
            .onNodeWithContentDescription("Delete step").performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule
            .onAllNodesWithContentDescription("Edit step")[0].performClick()
        composeRule.onRoot(useUnmergedTree = true).printToLog("editSTEP2")
        composeRule
            .onNodeWithContentDescription("Delete step").performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        // edit the new first step:
        composeRule
            .onAllNodesWithContentDescription("Edit step")[0].performClick()
        composeRule
            .onNodeWithText("Make the dressing.")
            .performTextClearance()
        composeRule
            .onNodeWithTag("editStep")
            .performTextInput("Cook the burger")
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()

        // add a new step:
        composeRule
            .onNodeWithText("Add Step", useUnmergedTree = true)
            .performClick()
        // discard:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()
        // add again without discarding this time:
        composeRule
            .onNodeWithText("Add Step", useUnmergedTree = true)
            .performClick()
        // fill in and submit:
        composeRule
            .onNodeWithText("Enter the step")
            .performTextInput("Eat the burger")
        composeRule
            .onNodeWithText("Save Step")
            .performClick()

        // edit the storage guidance: by making it empty:
        composeRule
            .onNodeWithText("Eat immediately.")
            .performTextClearance()

        // then save the recipe:
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithText("Update Recipe", useUnmergedTree = true)
            .performClick()

        // recipe has been updated successfully:
        composeRule
            .onNodeWithText("Healthy Salad")
            .assertDoesNotExist()
        composeRule
            .onNodeWithText("Unhealthy Burger")
            .assertExists()
    }

    @Test
    fun `Recipe can be manually added`() {
        composeRule
            .onNodeWithContentDescription("Go to view recipes", useUnmergedTree = true)
            .performClick()
        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("You have not saved any recipes!"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        // go to add a recipe manually
        composeRule.onNode(
            hasTestTag("addManuallyButton")
        ).performClick()

        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Add a Recipe"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // change photo, then remove photo:
        composeRule
            .onNodeWithContentDescription("Update image")
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

        // then remove it:
        composeRule
            .onNodeWithContentDescription("Delete image")
            .performClick()

        // now choose a colour and update the emoji:
        composeRule
            .onNodeWithContentDescription("Choose colour")
            .performClick()
        composeRule
            .onNodeWithText("Pink")
            .performClick()
        composeRule
            .onNodeWithText("Done", useUnmergedTree = true)
            .performClick()
        // emoji:
        composeRule
            .onNodeWithText("\uD83E\uDD57")
            .performClick()

        // scroll to strawberry emoji:
        composeRule
            .onNodeWithTag("emojiSelectorColumn")
            .performScrollToNode(
                hasText("\uD83C\uDF53", substring = true)
            )
        composeRule
            .onNodeWithText("\uD83C\uDF53") // select burger emoji
            .performClick()
        composeRule
            .onNodeWithText("Done", useUnmergedTree = true)
            .performClick()

        // name, type and tags:
        // to identify model bottom sheet, to close it when required:
        val hasDismissAction =
            SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)

        composeRule
            .onNodeWithText("Enter Title*")
            .performTextInput("Strawberry Milkshake")
        composeRule
            .onAllNodesWithText("None")[0]
            .performClick()
        composeRule
            .onNodeWithText("Drink")
            .performClick()
        // dismiss the modal bottom sheet:
        composeRule
            .onNode(hasDismissAction, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.Dismiss)

        composeRule
            .onAllNodesWithText("None")[0]
            .performClick()
        composeRule
            .onAllNodesWithText("Freezer-friendly")[0]
            .performClick()
        composeRule
            .onAllNodesWithText("Soy-free")[0]
            .performClick()
        composeRule
            .onAllNodesWithText("Dairy-free")[0]
            .performClick()
        composeRule
            .onAllNodesWithText("Vegetarian")[0]
            .performClick()
        // close the modal bottom sheet:
        composeRule
            .onNode(hasDismissAction, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.Dismiss)
        composeRule
            .onNodeWithText("Freezer-friendly")
            .assertExists()
        composeRule
            .onNodeWithText("Soy-free")
            .assertExists()
        composeRule
            .onNodeWithText("Dairy-free")
            .assertExists()
        composeRule
            .onNodeWithText("Vegetarian")
            .assertExists()

        // add ingredients:

        // scroll to ingredients:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Add Ingredient", substring = true)
            )

        // add strawberry milkshake ingredients and check blank inputs:
        composeRule
            .onNodeWithText("Add Ingredient", useUnmergedTree = true)
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        // error displayed:
        composeRule
            .onNodeWithText("The name cannot be blank.")
            .assertExists()
        // now fill everything in:
        composeRule
            .onNodeWithText("Enter an ingredient to add")
            .performTextInput("Strawberries")
        composeRule
            .onNodeWithText("Strawberries")
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        // error displayed:
        composeRule.onAllNodes(isRoot())[1].printToLog("BEFOER")
        composeRule
            .onNodeWithText("The amount cannot be blank.")
            .assertExists()
        composeRule
            .onAllNodesWithText("Enter amount")[1]
            .performTextInput("250")
        composeRule
            .onAllNodesWithText("250")[0]
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("g")
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()

        composeRule
            .onNodeWithText("Add Ingredient", useUnmergedTree = true)
            .performClick()
        // now fill everything in:
        composeRule
            .onNodeWithText("Enter an ingredient to add")
            .performTextInput("Oat milk")
        composeRule
            .onNodeWithText("Oat milk")
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onAllNodesWithText("Enter amount")[1]
            .performTextInput("200")
        composeRule
            .onAllNodesWithText("200")[0]
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("ml")
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()

        // scroll to add button:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Add Ingredient", substring = true)
            )
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule
            .onNodeWithText("Add Ingredient", useUnmergedTree = true)
            .performClick()
        // now fill everything in:
        composeRule
            .onNodeWithText("Enter an ingredient to add")
            .performTextInput("Honey")
        composeRule
            .onNodeWithText("Honey")
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onAllNodesWithText("Enter amount")[1]
            .performTextInput("10")
        composeRule
            .onAllNodesWithText("10")[0]
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("ml")
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()

        // first scroll down, then add  method:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Create Recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Add Step", useUnmergedTree = true)
            .performClick()
        // discard:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()
        // add again without discarding this time:
        composeRule
            .onNodeWithText("Add Step", useUnmergedTree = true)
            .performClick()
        // fill in and submit:
        composeRule
            .onNodeWithText("Enter the step")
            .performTextInput("Put all ingredients in a blender and blend")
        composeRule
            .onNodeWithText("Save Step")
            .performClick()

        composeRule
            .onNodeWithText("Add Step", useUnmergedTree = true)
            .performClick()
        // fill in and submit:
        composeRule
            .onNodeWithText("Enter the step")
            .performTextInput("Pour into a cup and enjoy!")
        composeRule
            .onNodeWithText("Save Step")
            .performClick()

        // edit the storage guidance: by making it empty:
        composeRule
            .onNodeWithText("Enter Storage Guidance")
            .performTextInput("Drink immediately.")

        // increase serving size:
        composeRule
            .onNodeWithTag("increaseServingSize")
            .performClick()
        // wait for the calculation to complete:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("2"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule
            .onNodeWithText("2") // serving size
            .assertExists()

        // decrease serving size, cannot go below 1:
        composeRule
            .onNodeWithTag("decreaseServingSize")
            .performClick()
        // wait for the calculation to complete:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("1"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        // stays at 1 if decreased further:
        composeRule
            .onNodeWithTag("decreaseServingSize")
            .performClick()
        composeRule
            .onNodeWithText("1") // serving size
            .assertExists()

        composeRule
            .onNodeWithText("1") // serving size
            .assertExists()

        // then save the recipe:
        composeRule
            .onNodeWithText("Create Recipe", useUnmergedTree = true)
            .performClick()

        // recipe has been saved successfully:
        composeRule
            .onNodeWithText("Strawberry Milkshake")
            .assertExists()
    }

    @Test
    fun `Can discard a manually added recipe`() {
        composeRule
            .onNodeWithContentDescription("Go to view recipes", useUnmergedTree = true)
            .performClick()
        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("You have not saved any recipes!"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        // go to add a recipe manually
        composeRule.onNode(
            hasTestTag("addManuallyButton")
        ).performClick()

        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Add a Recipe"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // go back, do not confirm, then go back again and confirm this time.
        composeRule
            .onNodeWithTag("goBack")
            .performClick()
        composeRule
            .onNodeWithText("No")
            .performClick()
        composeRule
            .onNodeWithTag("goBack")
            .performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()

        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("You have not saved any recipes!"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        // go to add a recipe manually
        composeRule.onNode(
            hasText("You have not saved any recipes!")
        ).assertExists()
    }

    @Test
    fun `Can discard changes made to an edited recipe`() {
        // generate the salad recipe to edit:
        generateSaladRecipe()

        // go to the healthy salad recipe:
        composeRule.onNode(
            hasText("Healthy Salad")
        ).performClick()

        // wait for the data to load from the DB:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Healthy Salad"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // scroll to and click on the edit button:
        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Edit Manually", substring = true)
            )

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN1")
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithTag("Edit Manually", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick)

        // change photo:
        composeRule
            .onNodeWithContentDescription("Update image")
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

        // now change the title:
        composeRule
            .onNodeWithText("Healthy Salad")
            .performTextClearance()
        composeRule
            .onNodeWithText("Enter Title*")
            .performTextInput("Unhealthy Burger")

        // go back, do not confirm, then go back again and confirm this time.
        composeRule
            .onNodeWithTag("goBack")
            .performClick()
        composeRule
            .onNodeWithText("No")
            .performClick()
        composeRule
            .onNodeWithTag("goBack")
            .performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()

        // changes reverted:

        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Healthy Salad"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        // no unhealthy burger: changes are lost. healthy salad remains.
        composeRule.onNode(
            hasText("Healthy Salad")
        ).assertExists()
        composeRule.onNode(
            hasText("Unhealthy Burger")
        ).assertDoesNotExist()
    }

    @Test
    fun `Recipe cannot be added or edited if fields are invalid and error message displays instead`() {
        composeRule
            .onNodeWithContentDescription("Go to view recipes", useUnmergedTree = true)
            .performClick()
        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("You have not saved any recipes!"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        // go to add a recipe manually
        composeRule.onNode(
            hasTestTag("addManuallyButton")
        ).performClick()

        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Add a Recipe"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // add photo:
        composeRule
            .onNodeWithContentDescription("Update image")
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

        // emoji:
        composeRule
            .onNodeWithText("\uD83E\uDD57")
            .performClick()

        // scroll to strawberry emoji:
        composeRule
            .onNodeWithTag("emojiSelectorColumn")
            .performScrollToNode(
                hasText("\uD83C\uDF53", substring = true)
            )
        composeRule
            .onNodeWithText("\uD83C\uDF53") // select strawberry emoji
            .performClick()
        composeRule
            .onNodeWithText("Done", useUnmergedTree = true)
            .performClick()

        // name, type and tags:
        // to identify model bottom sheet, to close it when required:
        val hasDismissAction =
            SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)

        // attempt to submit without title:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Create Recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Create Recipe", useUnmergedTree = true)
            .performClick()
        composeRule
            .onNodeWithText("You must enter a title.")
            .assertExists()
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Enter Title*", substring = true)
            )

        composeRule
            .onNodeWithText("Enter Title*")
            .performTextInput("Strawberry Milkshake")

        // attempt to submit without recipe type:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Create Recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Create Recipe", useUnmergedTree = true)
            .performClick()
        composeRule
            .onNodeWithText("Your recipe could not be saved.")
            .assertExists()
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Recipe Type*", substring = true)
            )

        composeRule
            .onAllNodesWithText("None")[0]
            .performClick()
        composeRule
            .onNodeWithText("Drink")
            .performClick()
        // dismiss the modal bottom sheet:
        composeRule
            .onAllNodes(hasDismissAction, useUnmergedTree = true)[1]
            .performSemanticsAction(SemanticsActions.Dismiss)

        composeRule
            .onAllNodesWithText("None")[0]
            .performClick()
        composeRule
            .onAllNodesWithText("Freezer-friendly")[0]
            .performClick()
        composeRule
            .onAllNodesWithText("Soy-free")[0]
            .performClick()
        composeRule
            .onAllNodesWithText("Dairy-free")[0]
            .performClick()
        composeRule
            .onAllNodesWithText("Vegetarian")[0]
            .performClick()
        // close the modal bottom sheet:
        composeRule
            .onNode(hasDismissAction, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.Dismiss)
        composeRule
            .onNodeWithText("Freezer-friendly")
            .assertExists()
        composeRule
            .onNodeWithText("Soy-free")
            .assertExists()
        composeRule
            .onNodeWithText("Dairy-free")
            .assertExists()
        composeRule
            .onNodeWithText("Vegetarian")
            .assertExists()

        // add ingredients:
        // attempt to submit without ingredients:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Create Recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Create Recipe", useUnmergedTree = true)
            .performClick()
        composeRule
            .onNodeWithText("You must enter at least 1 ingredient.")
            .assertExists()
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Add Ingredient", substring = true)
            )

        // scroll to ingredients:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Add Ingredient", substring = true)
            )

        // add strawberry milkshake ingredients and check blank inputs:
        composeRule
            .onNodeWithText("Add Ingredient", useUnmergedTree = true)
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        // error displayed:
        composeRule
            .onNodeWithText("The name cannot be blank.")
            .assertExists()
        // now fill everything in:
        composeRule
            .onNodeWithText("Enter an ingredient to add")
            .performTextInput("Strawberries")
        composeRule
            .onNodeWithText("Strawberries")
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        // error displayed:
        composeRule.onAllNodes(isRoot())[1].printToLog("BEFOER")
        composeRule
            .onNodeWithText("The amount cannot be blank.")
            .assertExists()
        composeRule
            .onAllNodesWithText("Enter amount")[1]
            .performTextInput("250")
        composeRule
            .onAllNodesWithText("250")[0]
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("g")
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()

        composeRule
            .onNodeWithText("Add Ingredient", useUnmergedTree = true)
            .performClick()
        // now fill everything in:
        composeRule
            .onNodeWithText("Enter an ingredient to add")
            .performTextInput("Oat milk")
        composeRule
            .onNodeWithText("Oat milk")
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onAllNodesWithText("Enter amount")[1]
            .performTextInput("200")
        composeRule
            .onAllNodesWithText("200")[0]
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("ml")
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()

        // scroll to add button:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Add Ingredient", substring = true)
            )
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule
            .onNodeWithText("Add Ingredient", useUnmergedTree = true)
            .performClick()
        // now fill everything in:
        composeRule
            .onNodeWithText("Enter an ingredient to add")
            .performTextInput("Honey")
        composeRule
            .onNodeWithText("Honey")
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onAllNodesWithText("Enter amount")[1]
            .performTextInput("10")
        composeRule
            .onAllNodesWithText("10")[0]
            .performImeAction() // to press "done" on the keyboard so closes keyboard
        composeRule
            .onNodeWithText("ml")
            .performClick()
        composeRule
            .onNodeWithText("Add to Recipe")
            .performClick()
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()

        // attempt to submit without method:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Create Recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Create Recipe", useUnmergedTree = true)
            .performClick()
        composeRule
            .onNodeWithText("You must enter at least 1 method step.")
            .assertExists()
        // first scroll down, then add  method:
        composeRule
            .onNodeWithTag("recipeCreationColumn")
            .performScrollToNode(
                hasText("Create Recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Add Step", useUnmergedTree = true)
            .performClick()
        // discard:
        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick()
        // add again without discarding this time:
        composeRule
            .onNodeWithText("Add Step", useUnmergedTree = true)
            .performClick()
        // fill in and submit:
        composeRule
            .onNodeWithText("Enter the step")
            .performTextInput("Put all ingredients in a blender and blend")
        composeRule
            .onNodeWithText("Save Step")
            .performClick()

        composeRule
            .onNodeWithText("Add Step", useUnmergedTree = true)
            .performClick()
        // fill in and submit:
        composeRule
            .onNodeWithText("Enter the step")
            .performTextInput("Pour into a cup and enjoy!")
        composeRule
            .onNodeWithText("Save Step")
            .performClick()


        // then save the recipe:
        composeRule
            .onNodeWithText("Create Recipe", useUnmergedTree = true)
            .performClick()

        // recipe has been saved successfully:
        composeRule
            .onNodeWithText("Strawberry Milkshake")
            .assertExists()
    }
}