package uk.co.imoqen.nourish

import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.imoqen.nourish.authentication.AuthenticationViewModel
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.UiEventBus

internal class AuthenticationViewModelTest {

    val fakeAuthenticationManager = FakeAuthenticationManager()
    val uiEventBus = UiEventBus()

    lateinit var vm: AuthenticationViewModel

    @Before
    fun init() {
        vm = AuthenticationViewModel(fakeAuthenticationManager, uiEventBus)
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Test
    fun `signInWithEmailAndPassword() fails when username or password are blank`() {
        // Empty string username and password:
        Assert.assertEquals(vm.uiState.value.emailToResetPassword, "")
        Assert.assertEquals(vm.uiState.value.passwordResetStatus, "")
        vm.signInWithEmailAndPassword()
        Assert.assertEquals(vm.uiState.value.signinError, ErrorMessage.INPUT_ERROR_BLANK)
    }
}