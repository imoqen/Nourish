package uk.co.imoqen.nourish

import android.content.Context
import co.uk.imoqen.testfixtures.common.FakeUiEventBus
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import co.uk.imoqen.testfixtures.repository.FakeTagRepository
import co.uk.imoqen.testfixtures.repository.FakeUserRepository
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.imoqen.nourish.authentication.AuthenticationUiStatus
import uk.co.imoqen.nourish.authentication.AuthenticationViewModel
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.setup.MainDispatcherRule

/**
 * Unit tests for the Authentication ViewModel
 *
 * @author Imogen Pemberton, 2026
 */
class AuthenticationViewModelTest {

    val fakeAuthenticationManager = FakeAuthenticationManager()
    lateinit var fakeUiEventBus: FakeUiEventBus
    // fake repositories:
    val fakeUserRepository = FakeUserRepository()
    val fakeTagRepository = FakeTagRepository()

    lateinit var vm: AuthenticationViewModel

    @Before
    fun init() {
        fakeUiEventBus = FakeUiEventBus()
        vm = AuthenticationViewModel(fakeAuthenticationManager, fakeUiEventBus, fakeUserRepository, fakeTagRepository)
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `signInWithEmailAndPassword() fails when username or password are blank`() {
        // Empty string username and password:
        Assert.assertEquals(vm.uiState.value.signinEmail, "")
        Assert.assertEquals(vm.uiState.value.signinPassword, "")
        vm.signInWithEmailAndPassword()
        Assert.assertEquals(vm.uiState.value.signinError, ErrorMessage.INPUT_ERROR_BLANK)

        // Just a blank password:
        vm.setSigninEmail("test@gmail.com")
        Assert.assertEquals(vm.uiState.value.signinPassword, "")
        vm.signInWithEmailAndPassword()
        Assert.assertEquals(vm.uiState.value.signinError, ErrorMessage.INPUT_ERROR_BLANK)

        // Just a blank email:
        vm.setSigninEmail("")
        vm.setSigninPassword("secure_password")
        vm.signInWithEmailAndPassword()
        Assert.assertEquals(vm.uiState.value.signinError, ErrorMessage.INPUT_ERROR_BLANK)
    }

    @Test
    fun `signInWithEmailAndPassword() successful for a valid account`() {
        // Valid account:
        vm.setSigninEmail("test@gmail.com")
        vm.setSigninPassword("secure_password")
        vm.signInWithEmailAndPassword()
        // No sign in error:
        Assert.assertEquals(vm.uiState.value.signinError, "")
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar("Sign in successful.")
        )
    }

    @Test
    fun `signInWithEmailAndPassword() fails for an invalid account`() {
        vm.setSigninEmail("unknown@gmail.com")
        vm.setSigninPassword("secure_password")
        vm.signInWithEmailAndPassword()
        // Sign in error:
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNIN)
        )
    }

    @Test
    fun `signUpWithEmailAndPassword() succeeds for a valid email and matching passwords`() {
        // Valid account:
        vm.setSignupEmail("test@gmail.com")
        vm.setSignupPassword("secure_password")
        vm.setSignupPasswordConfirmation("secure_password")
        vm.signUpWithEmailAndPassword()
        // Successful sign up:
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar("Sign up successful. You have now been signed in.")
        )
    }

    @Test
    fun `signUpWithEmailAndPassword() fails when passwords do not match`() {
        vm.setSignupEmail("test@gmail.com")
        vm.setSignupPassword("secure_password")
        vm.setSignupPasswordConfirmation("secure_pppassword")
        vm.signUpWithEmailAndPassword()
        // Error:
        Assert.assertEquals(vm.uiState.value.signupError, ErrorMessage.PASSWORD_ERROR_MATCH)
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNUP)
        )
    }

    @Test
    fun `signUpWithEmailAndPassword() fails when field(s) are left blank`() {
        vm.setSignupEmail("test@gmail.com")
        vm.setSignupPassword("")
        vm.setSignupPasswordConfirmation("secure_password")
        vm.signUpWithEmailAndPassword()
        // Error:
        Assert.assertEquals(vm.uiState.value.signupError, ErrorMessage.INPUT_ERROR_BLANK)
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNUP)
        )

        vm.setSignupEmail("test@gmail.com")
        vm.setSignupPassword("secure_password")
        vm.setSignupPasswordConfirmation("")
        vm.signUpWithEmailAndPassword()
        // Error:
        Assert.assertEquals(vm.uiState.value.signupError, ErrorMessage.INPUT_ERROR_BLANK)
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNUP)
        )

        vm.setSignupEmail("")
        vm.setSignupPassword("secure_password")
        vm.setSignupPasswordConfirmation("secure_password")
        vm.signUpWithEmailAndPassword()
        // Error:
        Assert.assertEquals(vm.uiState.value.signupError, ErrorMessage.INPUT_ERROR_BLANK)
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNUP)
        )
    }

    @Test
    fun `signUpWithEmailAndPassword() fails for an account which already exists`() {
        vm.setSignupEmail("alreadytaken@gmail.com")
        vm.setSignupPassword("secure_password")
        vm.setSignupPasswordConfirmation("secure_password")
        vm.signUpWithEmailAndPassword()
        // Error:
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNUP_EMAIL)
        )
    }

    @Test
    fun `passwordResetRequest() fails for a blank email address`() {
        Assert.assertEquals(vm.uiState.value.emailToResetPassword, "")
        vm.passwordResetRequest()
        // Error:
        Assert.assertEquals(vm.uiState.value.passwordResetStatus, ErrorMessage.INPUT_ERROR_BLANK)
    }

    @Test
    fun `passwordResetRequest() fails for an email not associated with an account on the app`() {
        vm.setEmailToResetPassword("nonexistent@gmail.com")
        vm.passwordResetRequest()
        // Error:
        Assert.assertEquals(
            vm.uiState.value.passwordResetStatus,
            ErrorMessage.USER_ERROR_EMAIL_SEND
        )
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_EMAIL_SEND)
        )
    }

    @Test
    fun `passwordResetRequest() succeeds for a valid email with an account`() {
        vm.setEmailToResetPassword("test@gmail.com")
        vm.passwordResetRequest()
        // Error:
        Assert.assertEquals(
            vm.uiState.value.passwordResetStatus,
            "A password reset email has been sent to test@gmail.com.\n" +
                    "Please follow the instructions in this email. \n\n" +
                    "Can't find the email? " +
                    "Please check your spam folder!"
        )
    }

    @Test
    fun `setStatus() sets correctly`() {
        Assert.assertEquals(
            vm.uiState.value.status,
            AuthenticationUiStatus.SIGNIN
        )
        vm.setStatus(AuthenticationUiStatus.SIGNUP)
        Assert.assertEquals(
            vm.uiState.value.status,
            AuthenticationUiStatus.SIGNUP
        )
    }

    @Test
    fun `setShowBottomSheet() sets correctly`() {
        Assert.assertEquals(
            vm.uiState.value.showBottomSheet,
            false
        )
        vm.setShowBottomSheet(true)
        Assert.assertEquals(
            vm.uiState.value.showBottomSheet,
            true
        )
    }

    @Test
    fun `populateDbWithTags() sets the default tags in db if user doesn't yet have tags`() {
        fakeAuthenticationManager.setLoginState(true, "user_without_tags")
        vm.populateDbWithTags()

        Assert.assertEquals(
            fakeTagRepository.lastCalled,
            "insert" // successfully inserted
        )
    }

    @Test
    fun `signInWithGoogle() signs the user in`() {
        val mockContext = mockk<Context>()
        vm.signInWithGoogle(mockContext)

        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar("Sign in successful.")
        )
    }
}