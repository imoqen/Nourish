package uk.co.imoqen.nourish.authentication

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import uk.co.imoqen.nourish.common.UiEventBus
import javax.inject.Inject

// https://developer.android.com/training/dependency-injection/hilt-testing
// https://developer.android.com/develop/ui/compose/testing/testing-cheatsheet

// https://developer.android.com/training/testing/ui-tests
// https://dagger.dev/hilt/testing.html
@UninstallModules(AuthenticationModule::class, FirebaseModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthenticationViewTest {

    @Inject
    lateinit var uiEventBus: UiEventBus

    @BindValue
    @JvmField
    val authenticationManager: AuthenticationManager = FakeAuthenticationManager()

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
    fun `Sign in button is displayed and clickable`() {
        composeRule.onNode(
            hasText("Sign in") and hasClickAction()
        ).assertExists()
        //composeRule.onNodeWithText("Sign in").performClick()
        //composeRule.onNodeWithText("All field(s) must not be blank.").assertExists()
        //composeRule.onNodeWithText("unsuccessful").assertExists()
    }
}