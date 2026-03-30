package uk.co.imoqen.nourish

import android.app.Activity
import android.content.Context
import co.uk.imoqen.testfixtures.common.FakeUiEventBus
import co.uk.imoqen.testfixtures.firebase.FakeAuthenticationManager
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.profile.ProfileViewModel
import uk.co.imoqen.nourish.setup.MainDispatcherRule

class ProfileViewModelTest {

    val fakeAuthenticationManager = FakeAuthenticationManager()
    lateinit var fakeUiEventBus: FakeUiEventBus

    lateinit var vm: ProfileViewModel

    @Before
    fun init() {
        fakeUiEventBus = FakeUiEventBus()
        vm = ProfileViewModel(fakeAuthenticationManager, fakeUiEventBus)
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `hasPasswordProvider() returns correct values for password provider account and Google account`() {
        // Google:
        fakeAuthenticationManager.setLoginState(true,"google_uid")
        vm.hasPasswordProvider()
        Assert.assertEquals(vm.uiState.value.hasPasswordProvider, false)

        // Password:
        fakeAuthenticationManager.setLoginState(true,"test_uid")
        vm.hasPasswordProvider()
        Assert.assertEquals(vm.uiState.value.hasPasswordProvider, true)
    }

    @Test
    fun `updatePassword() passes for filled valid fields and fails for blank input field(s)`() {
        // All blank:
        vm.updatePassword()
        Assert.assertEquals(vm.uiState.value.passwordChangeStatus, ErrorMessage.INPUT_ERROR_BLANK)

        vm.setOldPassword("password")
        vm.updatePassword()
        Assert.assertEquals(vm.uiState.value.passwordChangeStatus, ErrorMessage.INPUT_ERROR_BLANK)

        vm.setNewPassword("password2")
        vm.setOldPassword("")
        vm.updatePassword()
        Assert.assertEquals(vm.uiState.value.passwordChangeStatus, ErrorMessage.INPUT_ERROR_BLANK)

        // Both correct and valid:
        vm.setNewPassword("password2")
        vm.setOldPassword("password")
        vm.updatePassword()
        // no error - success:
        Assert.assertEquals(vm.uiState.value.passwordChangeStatus, "")
    }

    @Test
    fun `updatePassword() fails if the old password entered is incorrect`() {
        // Old password is not correct - doesn't match the one stored in the account
        vm.setNewPassword("incorrect_pw")
        vm.setOldPassword("invalid")
        vm.updatePassword()
        Assert.assertEquals(vm.uiState.value.passwordChangeStatus, ErrorMessage.PASSWORD_ERROR_CHANGE)
    }

    @Test
    fun `deletePasswordAccount() fails if blank password is entered`() {
        // Old password is not correct - doesn't match the one stored in the account
        vm.deletePasswordAccount()
        Assert.assertEquals(vm.uiState.value.deleteStatus, ErrorMessage.INPUT_ERROR_BLANK)
    }

    @Test
    fun `deletePasswordAccount() fails if incorrect password is entered`() {
        // Old password is not correct - doesn't match the one stored in the account
        vm.setDeleteAccountPassword("invalid")
        vm.deletePasswordAccount()
        Assert.assertEquals(vm.uiState.value.deleteStatus, ErrorMessage.DELETE_ERROR)
    }

    @Test
    fun `deletePasswordAccount() succeeds if valid password is entered`() {
        // Old password is  correct - matches the one stored in the account
        vm.setDeleteAccountPassword("correct_password")
        vm.deletePasswordAccount()
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar("Account successfully deleted.")
        )
    }

    @Test
    fun `signOut() signs out the user successfully`() {
        vm.signOut()
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar("Sign out successful.")
        )
    }

    @Test
    fun `setShowPasswordChangeBottomSheet() sets correctly`() {
        Assert.assertEquals(
            vm.uiState.value.showPasswordChangeBottomSheet,
            false
        )
        vm.setShowPasswordChangeBottomSheet(true)
        Assert.assertEquals(
            vm.uiState.value.showPasswordChangeBottomSheet,
            true
        )
    }

    @Test
    fun `setShowDeleteAccountBottomSheet() sets correctly`() {
        Assert.assertEquals(
            vm.uiState.value.showDeleteAccountBottomSheet,
            false
        )
        vm.setShowDeleteAccountBottomSheet(true)
        Assert.assertEquals(
            vm.uiState.value.showDeleteAccountBottomSheet,
            true
        )
    }

    @Test
    fun `getEmail() returns the email`() {
        Assert.assertEquals(
            vm.getEmail(),
            "test@gmail.com"
        )
    }

    @Test
    fun `deleteGoogleAccount() does not delete Google account if the user is not logged in`() {
        val mockActivity = mockk<Activity>()
        vm.deleteGoogleAccount(mockActivity)
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar(ErrorMessage.DELETE_ERROR)
        )
    }

    @Test
    fun `deleteGoogleAccount() deletes a Google account`() {
        fakeAuthenticationManager.setLoginState(true, "valid_user")
        val mockActivity = mockk<Activity>()
        vm.deleteGoogleAccount(mockActivity)
        Assert.assertEquals(
            fakeUiEventBus.events.first(),
            UiEvent.ShowSnackbar("Account successfully deleted.")
        )
    }
}