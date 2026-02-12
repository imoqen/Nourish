package uk.co.imoqen.nourish.authentication

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.imoqen.nourish.MainActivity
import uk.co.imoqen.nourish.common.UiEventBus
import uk.co.imoqen.nourish.network.ApiModule
import uk.co.imoqen.nourish.network.ApiService
import uk.co.imoqen.nourish.network.FakeApiService
import javax.inject.Inject

// https://developer.android.com/training/dependency-injection/hilt-testing
// https://developer.android.com/develop/ui/compose/testing/testing-cheatsheet

// https://developer.android.com/training/testing/ui-tests
// https://dagger.dev/hilt/testing.html
@UninstallModules(AuthenticationModule::class, FirebaseModule::class, ApiModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthenticationViewTest {

    @Inject
    lateinit var uiEventBus: UiEventBus

    @BindValue
    @JvmField
    val authenticationManager: AuthenticationManager = FakeAuthenticationManager()

    @BindValue
    @JvmField
    val apiService: ApiService = FakeApiService()

    /*@BindValue
    @JvmField
    val uiEventBus: UiEventBus = UiEventBus()*/

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    //@Inject
    //lateinit var fakeAuthenticationViewModel: FakeAuthenticationViewModel

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `Sign in flow works correctly for blank input`() {
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).assertExists()
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText("All field(s) must not be blank.").assertExists()
        composeRule.onNodeWithText("Your sign in attempt was unsuccessful.").assertExists()
        // user remains on sign in screen:
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).assertExists()
    }

    @Test
    fun `Sign in flow works correctly for an account which exists`() {
        composeRule
            .onNodeWithText("Enter email address")
            .performTextInput("test@gmail.com")
        composeRule
            .onNodeWithText("Enter password")
            .performTextInput("secure_password:D")
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).assertExists()
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).performClick()

        // Sign in success and user can now see the app contents:
        composeRule.onNodeWithText("Sign in successful.").assertExists()
        composeRule.onNode(
            hasText("Recipe Finder")
        ).assertExists()
        composeRule.onNode(
            hasText("Recipe Book")
        ).assertExists()
        composeRule.onNode(
            hasText("Shopping List")
        ).assertExists()

        // Then sign out:
        // NOTE put this in the profile view test, not here. because it beyond authentication view
        /*composeRule.onNode(
            hasText("Profile")
        ).performClick()
        composeRule.onNode(
            hasText("Sign out") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText("Sign out successful.").assertExists()*/
    }

    @Test
    fun `Sign in flow works correctly for an account which does not exist and is not blank`() {
        composeRule
            .onNodeWithText("Enter email address")
            .performTextInput("unknown@gmail.com")
        composeRule
            .onNodeWithText("Enter password")
            .performTextInput("secure_password:D")
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).assertExists()
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).performClick()

        // Sign in fail - user remains in sign up screen.
        composeRule.onNodeWithText("Your sign in attempt was unsuccessful.").assertExists()
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).assertExists()

    }

}