package uk.co.imoqen.nourish.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.authentication.AuthenticationManager
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager
): ViewModel() {

    private val mutableUiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = mutableUiState.asStateFlow()

    // runs when app starts
    init {
        // determine if the user has password provider:
        hasPasswordProvider()
    }

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

    fun hasPasswordProvider() {
        viewModelScope.launch() {
            runCatching {
                authenticationManager.hasPasswordProvider()
            }
                .onSuccess { result ->
                    setHasPasswordProvider(result)
                }
                .onFailure { error ->
                    Log.e("Password provider", "Error: $error")
                }
        }
    }

    fun updatePassword() {
        if (mutableUiState.value.oldPassword == "" || mutableUiState.value.newPassword == "") {
            setPasswordChangeStatus("All fields must not be blank.")
            return // early return
        }
        viewModelScope.launch() {
            // first, reauthenticate the user
            runCatching {
                authenticationManager.reauthenticate(mutableUiState.value.oldPassword)
            }
                .onSuccess {
                    // if reauthenticated, password can be changed.
                    runCatching {
                        authenticationManager.updatePassword(mutableUiState.value.newPassword)
                    }
                        .onSuccess {
                            setPasswordChangeStatus("Password updated successfully.")
                        }
                        .onFailure {
                            setPasswordChangeStatus("Password update failed.")
                        }
                }
                .onFailure {
                    setPasswordChangeStatus("Password update failed.")
                    // not saying that the existing password is incorrect
                    // as this is insecure and allows for enumeration attackl.
                }
        }
    }


}