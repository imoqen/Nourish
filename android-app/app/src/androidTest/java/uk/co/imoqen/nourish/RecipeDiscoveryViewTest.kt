package uk.co.imoqen.nourish

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import uk.co.imoqen.nourish.db.tag.Tag
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
    fun `Sign out flow works correctly`() {
        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        // have to dismiss the previous snackbar or new one wont show:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule.onNode(
            hasText("Sign out") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText("Sign out successful.").assertExists()
    }

}