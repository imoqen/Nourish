package uk.co.imoqen.nourish.authentication

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.imoqen.nourish.MainActivity
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.firebase.FakeFirestoreManager
import uk.co.imoqen.nourish.firebase.FirebaseModule
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationModule
import uk.co.imoqen.nourish.firebase.firestore.FirestoreManager
import uk.co.imoqen.nourish.network.ApiModule
import co.uk.imoqen.testfixtures.network.FakeApiManager
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.apimanager.ApiManagerModule
import javax.inject.Inject

// https://developer.android.com/training/dependency-injection/hilt-testing
// https://developer.android.com/develop/ui/compose/testing/testing-cheatsheet

// https://developer.android.com/training/testing/ui-tests
// https://dagger.dev/hilt/testing.html
@UninstallModules(AuthenticationModule::class, FirebaseModule::class, ApiManagerModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthenticationViewTest {

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

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `Sign in flow works correctly for blank input and displays error`() {
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
    fun `Sign in flow works correctly for an account which exists and signs in`() {
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
    fun `Sign in flow works correctly for an account which does not exist and is not blank and displays error`() {
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

    @Test
    fun `Sign up works correctly for a blank input and displays error`() {
        // Navigate to sign up page
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()
        // Press sign up with all fields blank
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()

        // Sign up fail - error displayed and user remains in sign up screen.
        composeRule.onNodeWithText("All field(s) must not be blank.").assertExists()
        composeRule.onNodeWithText("Your sign up attempt was unsuccessful.").assertExists()
        composeRule.onNode(
            hasText("Back to login") and hasClickAction()
        ).assertExists()
    }

    @Test
    fun `Sign up works correctly when passwords do not match and displays error`() {
        // Navigate to sign up page
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()
        // Enter passwords which do not match
        composeRule
            .onNodeWithText("Enter email address")
            .performTextInput("unknown@gmail.com")
        composeRule
            .onNodeWithText("Enter password")
            .performTextInput("secure_password:D")
        composeRule
            .onNodeWithText("Confirm password")
            .performTextInput("insecure_password:D")
        // Press sign up
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()

        // Sign up fail - error displayed and user remains in sign up screen.
        composeRule.onNodeWithText("Passwords do not match.").assertExists()
        composeRule.onNodeWithText("Your sign up attempt was unsuccessful.").assertExists()
        composeRule.onNode(
            hasText("Back to login") and hasClickAction()
        ).assertExists()
    }

    @Test
    fun `Sign up works correctly when email entered is invalid and displays error`() {
        // Navigate to sign up page
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()
        // Enter passwords which do not match
        composeRule
            .onNodeWithText("Enter email address")
            .performTextInput("invalidemail")
        composeRule
            .onNodeWithText("Enter password")
            .performTextInput("validpassword")
        composeRule
            .onNodeWithText("Confirm password")
            .performTextInput("validpassword")
        // Press sign up
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()

        // Sign up fail - error displayed and user remains in sign up screen.
        composeRule.onAllNodesWithText("Your sign up attempt was unsuccessful.").assertCountEquals(2)
        composeRule.onNode(
            hasText("Back to login") and hasClickAction()
        ).assertExists()
    }

    @Test
    fun `Sign up works correctly when valid credentials entered and user subsequently gets signed in`() {
        // Navigate to sign up page
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()
        // Enter passwords which do not match
        composeRule
            .onNodeWithText("Enter email address")
            .performTextInput("test@gmail.com")
        composeRule
            .onNodeWithText("Enter password")
            .performTextInput("validpassword")
        composeRule
            .onNodeWithText("Confirm password")
            .performTextInput("validpassword")
        // Press sign up
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()

        // Sign up success, user gets signed in and user can now see the app contents:
        composeRule.onNodeWithText("Sign up successful. You have now been signed in.").assertExists()
        composeRule.onNode(
            hasText("Recipe Finder")
        ).assertExists()
        composeRule.onNode(
            hasText("Recipe Book")
        ).assertExists()
        composeRule.onNode(
            hasText("Shopping List")
        ).assertExists()
    }

    @Test
    fun `Navigation between sign in and sign up page works correctly`() {
        // On sign in page:
        composeRule.onAllNodesWithText("Sign in").assertCountEquals(2)
        // Navigate to sign up page
        composeRule.onNode(
            hasText("Sign up") and hasClickAction()
        ).performClick()
        // On sign up page:
        composeRule.onNodeWithText("Sign up").assertExists()
        // Can navigate back to sign in page
        composeRule.onNode(
            hasText("Back to login") and hasClickAction()
        ).performClick()
        // Back on sign in page
        composeRule.onAllNodesWithText("Sign in").assertCountEquals(2)
    }

    @Test
    fun `Sign in flow works correctly for a Google account`() {
        composeRule.onNode(
            hasText("Sign in with Google") and hasClickAction()
        ).performClick()

        // User successfully signs in
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
    }

    @Test
    fun `Forgot password flow works correctly when valid email entered`() {
        composeRule.onNode(
            hasText("Forgotten your password?") and hasClickAction()
        ).performClick()
        composeRule
            .onNodeWithText("Enter account email")
            .performTextInput("test@gmail.com")
        composeRule.onNode(
            hasText("Reset password") and hasClickAction()
        ).performClick()

        // Password reset email gets successfully sent:
        composeRule.onNodeWithText("A password reset email has been sent to test@gmail.com. Please follow the instructions in this email. Can't find the email? Make sure to check your spam folder!").assertExists()
    }

    @Test
    fun `Forgot password flow works correctly when invalid email entered`() {
        composeRule.onNode(
            hasText("Forgotten your password?") and hasClickAction()
        ).performClick()
        composeRule
            .onNodeWithText("Enter account email")
            .performTextInput("invalid@gmail.com")
        composeRule.onNode(
            hasText("Reset password") and hasClickAction()
        ).performClick()

        // Password reset email gets successfully sent:
        composeRule.onAllNodesWithText("Password reset email failed to send.").assertCountEquals(2)
    }

    @Test
    fun `Forgot password flow works correctly when blank email entered`() {
        composeRule.onNode(
            hasText("Forgotten your password?") and hasClickAction()
        ).performClick()
        // no email entered.
        composeRule.onNode(
            hasText("Reset password") and hasClickAction()
        ).performClick()

        // Password reset email gets successfully sent:
        composeRule.onNodeWithText("All field(s) must not be blank.").assertExists()
    }

}