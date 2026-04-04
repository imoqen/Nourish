package uk.co.imoqen.nourish

import android.util.Log
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.firebase.FakeFirestoreManager
import co.uk.imoqen.testfixtures.network.FakeApiManager
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
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
class ShoppingListViewTest {

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Ingredients of a recipe can be selectively added to the shopping list`() = runTest {
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
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        // add the ingredients to the shopping list:
        composeRule
            .onNodeWithTag("recipeBookColumn")
            .performScrollToNode(
                hasText("Add to Shopping List", substring = true)
            )
        composeRule.onNode(
            hasText("Add to Shopping List")
        ).performClick()
        // wait for the selection to load:
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodes(hasTestTag("addIngredientCheckbox"))
                .fetchSemanticsNodes().size == 8
        }
        // then selectively deselect salt and pepper:
        composeRule.onAllNodesWithTag("addIngredientCheckbox")[6].performClick() // deselect salt
        composeRule.onAllNodesWithTag("addIngredientCheckbox")[6].performClick() // reselect salt
        composeRule.onAllNodesWithTag("addIngredientCheckbox")[6].performClick() // deselect salt
        composeRule.onAllNodesWithTag("addIngredientCheckbox")[7].performClick() // deselect pepper
        composeRule.onAllNodesWithText("Add to Shopping List")[2].performClick()
        // wait 10 seconds so that db reliably updates correctly
        Thread.sleep(10_000)
        composeRule.waitForIdle()

        // then go to the shopping list:
        composeRule
            .onNodeWithContentDescription("Go to shopping list", useUnmergedTree = true)
            .performClick()
        // wait for the ingredients to load:
        advanceUntilIdle()
        composeRule.waitForIdle()
        // check the ingredients are present:
        composeRule
            .onNodeWithText("cucumbers")
            .assertExists()
        composeRule
            .onNodeWithText("1")
            .assertExists()
        composeRule
            .onNodeWithText("tomatoes")
            .assertExists()
        composeRule
            .onNodeWithText("100 g")
            .assertExists()
        composeRule
            .onNodeWithText("lettuce")
            .assertExists()
        composeRule
            .onNodeWithText("honey")
            .assertExists()
        composeRule
            .onNodeWithText("vinegar")
            .assertExists()
        composeRule
            .onNodeWithText("lemon juice")
            .assertExists()
        // salt and pepper not added:
        composeRule
            .onNodeWithText("salt")
            .assertDoesNotExist()
        composeRule
            .onNodeWithText("pepper")
            .assertDoesNotExist()
    }
}