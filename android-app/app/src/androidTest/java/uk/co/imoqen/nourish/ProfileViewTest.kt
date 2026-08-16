package uk.co.imoqen.nourish

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.firebase.FirebaseModule
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationModule
import uk.co.imoqen.nourish.firebase.firestore.FirestoreManager
import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.apimanager.ApiManagerModule
import javax.inject.Inject

/**
 * Instrumented tests for the Profile View
 */
@UninstallModules(AuthenticationModule::class, FirebaseModule::class, ApiManagerModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProfileViewTest {

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
        signIn()
    }

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

    @Test
    fun `Delete account flow works for password-provider accounts`() {
        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        // have to dismiss the previous snackbar or new one wont show:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule.onNode(
            hasText("Delete Account") and hasClickAction()
        ).performClick()
        composeRule
            .onNodeWithText("Enter your password")
            .performTextInput("valid_password")
        composeRule.onNode(
            hasText("Confirm and Delete") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText("Account successfully deleted.").assertExists()
    }

    @Test
    fun `Delete account flow fails for invalid password for password-provider accounts`() {
        // Then sign out:
        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        // have to dismiss the previous snackbar or new one wont show:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule.onNode(
            hasText("Delete Account") and hasClickAction()
        ).performClick()
        composeRule
            .onNodeWithText("Enter your password")
            .performTextInput("invalid")
        composeRule.onNode(
            hasText("Confirm and Delete") and hasClickAction()
        ).performClick()
        composeRule.onAllNodesWithText(ErrorMessage.DELETE_ERROR).assertCountEquals(2)
    }

    @Test
    fun `Delete account flow works for Google-provider accounts`() {
        // First sign out of password-based account:
        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        composeRule.onNode(
            hasText("Sign out") and hasClickAction()
        ).performClick()

        // Sign in with Google account:
        composeRule.onNode(
            hasText("Sign in with Google") and hasClickAction()
        ).performClick()

        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        // have to dismiss the previous snackbar or new one wont show:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule.onNode(
            hasText("Delete Account") and hasClickAction()
        ).performClick()
        composeRule.onNode(
            hasText("Confirm and Delete") and hasClickAction()
        ).performClick()
    }

    @Test
    fun `Update password succeeds for password-provider accounts`() {
        // Then sign out:
        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        // have to dismiss the previous snackbar or new one wont show:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule.onNode(
            hasText("Change password") and hasClickAction()
        ).performClick()

        composeRule
            .onNodeWithText("Enter existing password")
            .performTextInput("password")
        composeRule
            .onNodeWithText("Enter a new password")
            .performTextInput("password2")

        composeRule.onNode(
            hasText("Update Password") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText("Password updated successfully.").assertExists()
    }

    @Test
    fun `Update password fails for invalid password entered for password-provider accounts`() {
        // Then sign out:
        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        // have to dismiss the previous snackbar or new one wont show:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule.onNode(
            hasText("Change password") and hasClickAction()
        ).performClick()

        composeRule
            .onNodeWithText("Enter existing password")
            .performTextInput("incorrect_pw2")
        composeRule
            .onNodeWithText("Enter a new password")
            .performTextInput("incorrect_pw")

        composeRule.onNode(
            hasText("Update Password") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText(ErrorMessage.PASSWORD_ERROR_CHANGE).assertExists()

    }

    @Test
    fun `Update password fails for blank password entered for password-provider accounts`() {
        // Then sign out:
        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        // have to dismiss the previous snackbar or new one wont show:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()
        composeRule.onNode(
            hasText("Change password") and hasClickAction()
        ).performClick()

        composeRule
            .onNodeWithText("Enter existing password")
            .performTextInput("")
        composeRule
            .onNodeWithText("Enter a new password")
            .performTextInput("password2")

        composeRule.onNode(
            hasText("Update Password") and hasClickAction()
        ).performClick()
        composeRule.onNodeWithText(ErrorMessage.INPUT_ERROR_BLANK).assertExists()
    }

    @Test
    fun `Change password option is not provided for Google accounts`() {
        // First sign out of password-based account:
        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        composeRule.onNode(
            hasText("Sign out") and hasClickAction()
        ).performClick()

        // Sign in with Google account:
        composeRule.onNode(
            hasText("Sign in with Google") and hasClickAction()
        ).performClick()

        composeRule.onNode(
            hasText("Profile")
        ).performClick()
        // have to dismiss the previous snackbar or new one wont show:
        composeRule.onNode(
            hasText("Dismiss")
        ).performClick()

        // no change password option:
        composeRule.onNodeWithText("Change password").assertDoesNotExist()
    }

}