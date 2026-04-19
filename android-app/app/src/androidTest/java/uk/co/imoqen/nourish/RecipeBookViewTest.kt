package uk.co.imoqen.nourish

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import co.uk.imoqen.testfixtures.common.TestData
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

@UninstallModules(AuthenticationModule::class, FirebaseModule::class, ApiManagerModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecipeBookViewTest {

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
        // add a salad recipe before each test:
        generateSaladRecipe()
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
    fun `All ingredients of a recipe are visible in its recipe book page`() {
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

        // check that all ingredients are present:
        composeRule
            .onNodeWithText("Ingredients")
            .assertExists()
        composeRule
            .onNodeWithText("For the salad")
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
        composeRule
            .onNodeWithText("For the dressing")
            .assertExists()
        composeRule
            .onNodeWithText("5 ml honey")
            .assertExists()
        composeRule
            .onNodeWithText("1 dash of vinegar")
            .assertExists()
        composeRule
            .onNodeWithText("1 drop of lemon juice")
            .assertExists()
        composeRule
            .onNodeWithText("1 sprinkle of salt")
            .assertExists()
        composeRule
            .onNodeWithText("1 dash of pepper")
            .assertExists()
        // option to add to shopping list:
        composeRule
            .onNodeWithText("Add to Shopping List")
            .assertExists()
    }

    @Test
    fun `All additional data regarding a recipe is visible in its recipe book page`() {
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

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN")

        // check that all additional data is present:
        composeRule
            .onNodeWithText("Serving")
            .assertExists()
        composeRule
            .onNodeWithText("1") // serving size
            .assertExists()
        composeRule
            .onNodeWithText("Generated by Gemini.")
            .assertExists()
        composeRule
            .onNodeWithText("Lunch")
            .assertExists()
        composeRule
            .onNodeWithText("Vegan")
            .assertExists()
        composeRule
            .onNodeWithText("Low calorie")
            .assertExists()
    }

    @Test
    fun `Recipe method and storage guidance of a recipe are visible in its recipe book page`() {
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

        // check that the method is present:
        composeRule
            .onNodeWithText("Method")
            .assertExists()
        composeRule
            .onNodeWithText("Step 1.")
            .assertExists()
        composeRule
            .onNodeWithText("First, wash the vegetables.")
            .assertExists()
        composeRule
            .onNodeWithText("Step 2.")
            .assertExists()
        composeRule
            .onNodeWithText("Cut the vegetables.")
            .assertExists()
        composeRule
            .onNodeWithText("Step 3.")
            .assertExists()
        composeRule
            .onNodeWithText("Make the dressing.")
            .assertExists()
        composeRule
            .onNodeWithText("Step 4.")
            .assertExists()
        composeRule
            .onNodeWithText("Assemble and mix everything together in a big bowl.")
            .assertExists()

        // check that can check off a step, by checking off the first step:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule
            .onAllNodesWithTag("stepCheckbox")
            .onLast()
            .performScrollTo()
        composeRule
            .onAllNodesWithTag("stepCheckbox").onFirst()
            .assertIsToggleable()
            .performClick()
        // then wait for the update to complete, step to be checked off:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule
                    .onAllNodesWithTag("stepCheckbox").onFirst()
                    .assertIsOn()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        // check that storage guidance is visible:
        composeRule
            .onNodeWithText("Storage Guidance")
            .assertExists()
        composeRule
            .onNodeWithText("Eat immediately.")
            .assertExists()
    }

    @Test
    fun `Recipe Alteration can be generated and saved as new recipe`() {
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

        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Make Alteration", substring = true)
            )

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN1")

        // make the alteration:
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithTag("Make Alteration", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule
            .onNodeWithText("Enter an alteration")
            .performTextInput("Make it healthier")

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick() // send it

        // check altered recipe is made:
        composeRule
            .onAllNodesWithText("Healthy Salad")
            .assertCountEquals(2)

        // and check can save recipe:
        composeRule
            .onNodeWithTag("alterationColumn")
            .performScrollToNode(
                hasText("Save as new recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Save as new recipe")
            .performClick()
        // and get sent to the new recipe:
        composeRule
            .onNodeWithText("Healthy Salad")
            .assertExists()
    }

    @Test
    fun `Recipe Alteration can be generated and replace the original recipe`() {
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

        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Make Alteration", substring = true)
            )

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN1")

        // make the alteration:
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithTag("Make Alteration", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule
            .onNodeWithText("Enter an alteration")
            .performTextInput("Make it healthier")

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick() // send it

        // check altered recipe is made:
        composeRule
            .onAllNodesWithText("Healthy Salad")
            .assertCountEquals(2)

        // and check can save recipe:
        composeRule
            .onNodeWithTag("alterationColumn")
            .performScrollToNode(
                hasText("Replace recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Replace recipe")
            .performClick()
        // and get sent to the new recipe:
        composeRule
            .onNodeWithText("Healthy Salad")
            .assertExists()
    }

    @Test
    fun `Recipe Alteration can be generated and the alteration can be discarded`() {
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

        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Make Alteration", substring = true)
            )

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN1")

        // make the alteration:
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithTag("Make Alteration", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule
            .onNodeWithText("Enter an alteration")
            .performTextInput("Make it healthier")

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick() // send it

        // check altered recipe is made:
        composeRule
            .onAllNodesWithText("Healthy Salad")
            .assertCountEquals(2)

        // and check can save recipe:
        composeRule
            .onNodeWithTag("alterationColumn")
            .performScrollToNode(
                hasText("Discard recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Discard recipe")
            .performClick()

        // not sent to the new recipe, remain on the recipe alteration view:
        composeRule
            .onNodeWithText("Recipe Alteration")
            .assertExists()
    }

    @Test
    fun `Question can be asked about the recipe`() {
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

        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Ask Question", substring = true)
            )

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN1")

        // ask the question:
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithTag("Ask Question", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule
            .onNodeWithText("Enter a question")
            .performTextInput("Is this healthy?")

        composeRule
            .onNodeWithContentDescription("Trailing Icon")
            .performClick() // send it

        // check response is received:
        composeRule
            .onNodeWithText("This recipe is healthy.")
            .assertExists()
    }

    @Test
    fun `Nutrition can be generated about a recipe`() {
        // go to the healthy salad recipe:
        composeRule.onNode(
            hasText("Healthy Salad")
        ).performClick()

        // dismiss previous snackbar:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()

        // wait for the data to load from the DB:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Healthy Salad"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Nutrition", substring = true)
            )

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN1")

        // generate the nutrition:
        composeRule
            // have to use test tag as the text is embedded within the button so not clickable itself
            // and semantics action rather than .performClick() otherwise not clicked
            .onNodeWithTag("Nutrition", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick)

        composeRule.onAllNodes(isRoot())[0].printToLog("ROOT_0")
        composeRule.onAllNodes(isRoot())[1].printToLog("ROOT_1")

        composeRule
            .onNodeWithText("Estimate Nutritional Values")
            .performClick() // send request

        // wait for the data to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Estimated Nutritional Values"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // check nutrition is received:
        composeRule
            .onNodeWithText("Estimated Nutritional Values")
            .assertExists()
        composeRule
            .onNodeWithText("180 kcal")
            .assertExists()
        composeRule
            .onNodeWithText("Fat")
            .assertExists()
        composeRule
            .onNodeWithText("4.0g")
            .assertExists()
        composeRule
            .onNodeWithText("Saturates")
            .assertExists()
        composeRule
            .onNodeWithText("5.0g")
            .assertExists()
        composeRule
            .onNodeWithText("Carbohydrate")
            .assertExists()
        composeRule
            .onNodeWithText("10.0g")
            .assertExists()
        composeRule
            .onNodeWithText("Sugars")
            .assertExists()
        composeRule
            .onNodeWithText("7.0g")
            .assertExists()
        composeRule
            .onNodeWithText("Protein")
            .assertExists()
        composeRule
            .onNodeWithText("1.0g")
            .assertExists()
        composeRule
            .onNodeWithText("Salt")
            .assertExists()
        composeRule
            .onNodeWithText("0.5g")
            .assertExists()
        composeRule
            .onNodeWithText("Fibre")
            .assertExists()
        composeRule
            .onNodeWithText("20.0g")
            .assertExists()
    }

    @Test
    fun `Recipes can be searched`() {
        composeRule.onNode(
            hasText("Search for a recipe")
        ).performTextInput("unhealthy")
        composeRule.onNode(
            hasText("Healthy Salad")
        ).assertDoesNotExist()

        composeRule.onNode(
            hasText("unhealthy")
        ).performTextClearance()
        composeRule.onNode(
            hasText("Search for a recipe")
        ).performTextInput("healthy")
        composeRule.onNode(
            hasText("Healthy Salad")
        ).assertExists()

        composeRule.onNode(
            hasText("healthy")
        ).performTextClearance()
        composeRule.onNode(
            hasText("Healthy Salad")
        ).assertExists()
    }

    @Test
    fun `Recipes can be filtered by tags`() {
        // add freezer friendly tag
        composeRule.onNodeWithContentDescription("Filter")
            .performClick()
        composeRule.onNode(
            hasText("Freezer-friendly")
        ).performClick()
        // salad recipe not displayed:
        composeRule.onNode(
            hasText("Healthy Salad")
        ).assertDoesNotExist()

        // remove freezer friendly tag and add low calorie and vegan tag
        composeRule.onNode(
            hasText("Freezer-friendly") and hasClickAction()
        ).performClick()
        // salad recipe displayed:
        composeRule.onNode(
            hasText("Healthy Salad")
        ).assertExists()
        composeRule.onAllNodes(
            hasText("Vegan") and hasClickAction()
        )[1].performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Healthy Salad"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodes(
            hasText("Low calorie") and hasClickAction()
        )[1].performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Healthy Salad"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `Recipes can be filtered by type`() {
        // breakfast:
        composeRule.onNodeWithContentDescription("Filter")
            .performClick()
        composeRule.onNode(
            hasText("Breakfast")
        ).performClick()

        // salad recipe not displayed:
        composeRule.onNode(
            hasText("Healthy Salad")
        ).assertDoesNotExist()
        composeRule.onNode(
            hasText("You do not have any saved Breakfast recipes.")
        ).assertExists()

        // lunch:
        composeRule.onNode(
            hasText("Lunch")
        ).performClick()

        // salad recipe not displayed:
        composeRule.onNode(
            hasText("Healthy Salad")
        ).assertExists()
        composeRule.onNode(
            hasText("You do not have any saved Breakfast or Lunch recipes.")
        ).assertDoesNotExist()

        // dinner:
        composeRule.onNode(
            hasText("Dinner")
        ).performClick()
        composeRule.onNode(
            hasText("You do not have any saved Breakfast or Dinner recipes.")
        ).assertExists()

        // dessert:
        composeRule.onNode(
            hasText("Dessert")
        ).performClick()
        composeRule.onNode(
            hasText("You do not have any saved Breakfast or Dinner or Dessert recipes.")
        ).assertExists()

        // side:
        composeRule.onNode(
            hasText("Side")
        ).performClick()
        composeRule.onNode(
            hasText("You do not have any saved Breakfast or Dinner or Dessert or Side recipes.")
        ).assertExists()

        // drink:
        composeRule.onNode(
            hasText("Drink")
        ).performClick()
        composeRule.onNode(
            hasText("You do not have any saved Breakfast or Dinner or Dessert or Side or Drink recipes.")
        ).assertExists()
    }

    @Test
    fun `Ingredients can be scaled by serving size and the serving size cannot go below one`() {
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

        composeRule.onRoot(useUnmergedTree = true).printToLog("RECIPEBOOKSCREEN")

        // check that all additional data is present:
        composeRule
            .onNodeWithText("Serving")
            .assertExists()
        composeRule
            .onNodeWithText("1") // serving size
            .assertExists()
        // check initial ingredient values for serving size of 1:
        composeRule
            .onNodeWithText("Ingredients")
            .assertExists()
        composeRule
            .onNodeWithText("For the salad")
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
        composeRule
            .onNodeWithText("For the dressing")
            .assertExists()
        composeRule
            .onNodeWithText("5 ml honey")
            .assertExists()
        composeRule
            .onNodeWithText("1 dash of vinegar")
            .assertExists()
        composeRule
            .onNodeWithText("1 drop of lemon juice")
            .assertExists()
        composeRule
            .onNodeWithText("1 sprinkle of salt")
            .assertExists()
        composeRule
            .onNodeWithText("1 dash of pepper")
            .assertExists()

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

        composeRule
            .onNodeWithText("Ingredients")
            .assertExists()
        composeRule
            .onNodeWithText("For the salad")
            .assertExists()
        composeRule
            .onNodeWithText("0.5 cucumber")
            .assertExists()
        composeRule
            .onNodeWithText("200 g tomatoes")
            .assertExists()
        composeRule
            .onNodeWithText("8 leaves of lettuce")
            .assertExists()
        composeRule
            .onNodeWithText("For the dressing")
            .assertExists()
        composeRule
            .onNodeWithText("10 ml honey")
            .assertExists()
        composeRule
            .onNodeWithText("2 dashes of vinegar")
            .assertExists()
        composeRule
            .onNodeWithText("2 drops of lemon juice")
            .assertExists()
        composeRule
            .onNodeWithText("2 sprinkles of salt")
            .assertExists()
        composeRule
            .onNodeWithText("2 dashes of pepper")
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

        composeRule
            .onNodeWithText("1") // serving size
            .assertExists()
        composeRule
            .onNodeWithText("Ingredients")
            .assertExists()
        composeRule
            .onNodeWithText("For the salad")
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
        composeRule
            .onNodeWithText("For the dressing")
            .assertExists()
        composeRule
            .onNodeWithText("5 ml honey")
            .assertExists()
        composeRule
            .onNodeWithText("1 dash of vinegar")
            .assertExists()
        composeRule
            .onNodeWithText("1 drop of lemon juice")
            .assertExists()
        composeRule
            .onNodeWithText("1 sprinkle of salt")
            .assertExists()
        composeRule
            .onNodeWithText("1 dash of pepper")
            .assertExists()

        // stays at 1 if decreased further:
        composeRule
            .onNodeWithTag("decreaseServingSize")
            .performClick()
        composeRule
            .onNodeWithText("1") // serving size
            .assertExists()

        composeRule
            .onNodeWithText("Ingredients")
            .assertExists()
        composeRule
            .onNodeWithText("For the salad")
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
        composeRule
            .onNodeWithText("For the dressing")
            .assertExists()
        composeRule
            .onNodeWithText("5 ml honey")
            .assertExists()
        composeRule
            .onNodeWithText("1 dash of vinegar")
            .assertExists()
        composeRule
            .onNodeWithText("1 drop of lemon juice")
            .assertExists()
        composeRule
            .onNodeWithText("1 sprinkle of salt")
            .assertExists()
        composeRule
            .onNodeWithText("1 dash of pepper")
            .assertExists()
    }

    @Test
    fun `Recipe can be deleted`() {
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

        composeRule
            .onNodeWithText("Dismiss")
            .performClick()

        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Delete Recipe", substring = true)
            )
        composeRule
            .onNodeWithText("Delete Recipe")
            .performClick()
        composeRule
            .onNodeWithText("Yes")
            .performClick()

        // wait for recipes to update:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasText("Healthy Salad"))
                .fetchSemanticsNodes().isEmpty()
        }

        // healthy salad recipe is now gone:
        composeRule
            .onNodeWithText("Healthy Salad")
            .assertDoesNotExist()
    }
}