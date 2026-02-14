package uk.co.imoqen.nourish.profile

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import javax.inject.Inject

/**
 * ViewModel for Profile.
 * Dependent on the AuthenticationManager to perform authentication based actions such as
 * sign out, password changing and account deletion.
 * @param authenticationManager - dependent on the AuthenticationManager
 * @param uiEventBus - event bus for handling Ui events such as Toasts, shared across the app
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager,
    private val uiEventBus: UiEventBus
): ViewModel() {
    private val mutableUiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = mutableUiState.asStateFlow()

    // When the app launches or user logs in, the profile view model is re-initialised to ensure fresh state.
    init {
        hasPasswordProvider()
    }

    // Setters for the Ui State:
    fun setHasPasswordProvider(hasPasswordProvider: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                hasPasswordProvider = hasPasswordProvider
            )
        }
    }

    fun setShowPasswordChangeBottomSheet(showPasswordChangeBottomSheet: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showPasswordChangeBottomSheet = showPasswordChangeBottomSheet
            )
        }
    }

    fun setOldPassword(oldPassword: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                oldPassword = oldPassword
            )
        }
    }

    fun setNewPassword(newPassword: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                newPassword = newPassword
            )
        }
    }

    fun setPasswordChangeStatus(passwordChangeStatus: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                passwordChangeStatus = passwordChangeStatus
            )
        }
    }

    fun setShowDeleteAccountBottomSheet(showDeleteAccountBottomSheet: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showDeleteAccountBottomSheet = showDeleteAccountBottomSheet
            )
        }
    }

    fun setDeleteAccountPassword(deleteAccountPassword: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                deleteAccountPassword = deleteAccountPassword
            )
        }
    }

    fun setDeleteStatus(deleteStatus: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                deleteStatus = deleteStatus
            )
        }
    }

    // Functions used within the View to perform actions:

    /**
     * Determines whether the currently logged in user has a password-provider.
     * There are two types of provider supported:
     * - Google (sign up/in with Google)
     * - Password (sign up/in with email and password)
     * This method checks if the user has the latter type of account.
     */
    fun hasPasswordProvider() {
        viewModelScope.launch() {
            runCatching {
                authenticationManager.hasPasswordProvider()
            }
                .onSuccess { result ->
                    setHasPasswordProvider(result)
                }
                .onFailure { error ->
                    Log.e("PASSWORD PROVIDER", "Error: $error")
                }
        }
    }

    /**
     * Updates the password of a password-provider account. Requires reauthentication beforehand.
     */
    fun updatePassword() {
        if (mutableUiState.value.oldPassword == "" || mutableUiState.value.newPassword == "") {
            setPasswordChangeStatus(ErrorMessage.INPUT_ERROR_BLANK)
            return // early return
        }
        viewModelScope.launch() {
            // First, reauthenticate the user
            runCatching {
                authenticationManager.reauthenticate(mutableUiState.value.oldPassword)
                // If reauthenticated successfully, password can be changed:
                authenticationManager.updatePassword(mutableUiState.value.newPassword)
            }
                .onSuccess {
                    setPasswordChangeStatus("Password updated successfully.")
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Password updated successfully."))
                }
                .onFailure { fail ->
                    if (fail.message != null && fail.message!!.contains(ErrorMessage.FIREBASE_PASSWORD_ERROR)) {
                        setPasswordChangeStatus(ErrorMessage.USER_ERROR_WEAK_PASSWORD_CHANGE)
                    } else {
                        // Password change error messages are purposefully non-revealing, as revealing
                        // if the password is correct / incorrect etc could allow for an enumeration attack.
                        setPasswordChangeStatus(ErrorMessage.PASSWORD_ERROR_CHANGE)
                        uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.PASSWORD_ERROR_CHANGE))
                    }
                }
        }
    }

    /**
     * Deletes a password-provider account.
     */
    fun deletePasswordAccount() {
        if (mutableUiState.value.deleteAccountPassword == "") {
            setDeleteStatus(ErrorMessage.INPUT_ERROR_BLANK)
            return // early return
        }
        viewModelScope.launch() {
            runCatching {
                // First reauthenticate the user:
                authenticationManager.reauthenticate(mutableUiState.value.deleteAccountPassword)
                // Failure has not occurred thus can delete the account.
                authenticationManager.deletePasswordAccount()
            }
                .onSuccess {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Account successfully deleted."))
                }
                .onFailure {
                    setDeleteStatus(ErrorMessage.DELETE_ERROR)
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.DELETE_ERROR))
                }
        }
    }

    /**
     * Deletes a Google-provider account.
     * This is based on an approach found on StackOverflow:
     * https://stackoverflow.com/questions/40898952/how-to-re-authenticate-a-user-on-firebase-with-google-provider
     * it requires navigating the user into their web app and following a flow to reauthenticate
     * their Google account. If successful, the account is deleted and the user is logged out.
     * @param activity - the current activity, allowing the web app to be opened for reauthentication
     */
    fun deleteGoogleAccount(activity: Activity?) {
        viewModelScope.launch() {
            if (activity != null) {
                runCatching {
                    authenticationManager.deleteGoogleAccount(activity)
                }
                    .onSuccess {
                        uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Account successfully deleted."))
                    }
                    .onFailure {
                        setDeleteStatus(ErrorMessage.DELETE_ERROR)
                        uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.DELETE_ERROR))
                    }
            }
        }
    }

    /**
     * Signs out the current logged in user.
     */
    fun signOut() {
        authenticationManager.signOut()
        viewModelScope.launch() {
            uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Sign out successful."))
        }
    }
}