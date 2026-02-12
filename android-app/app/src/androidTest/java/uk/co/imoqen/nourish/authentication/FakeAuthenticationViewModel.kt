package uk.co.imoqen.nourish.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.ErrorMessage

// https://medium.com/@narula.rohtash/instrumentation-testing-a-jetpack-compose-login-screen-with-hilt-and-fake-viewmodels-ab4f99d8f660

class FakeAuthenticationViewModel : ViewModel() {

    private val mutableUiState = MutableStateFlow(AuthenticationUiState())
    val uiState: StateFlow<AuthenticationUiState> = mutableUiState.asStateFlow()

    // Setters - same as in the actual view model
    fun setSigninEmail(signinEmail: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                signinEmail = signinEmail
            )
        }
    }

    fun setSigninPassword(signinPassword: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                signinPassword = signinPassword
            )
        }
    }

    fun setStatus(status: AuthenticationUiStatus) {
        mutableUiState.update { currentState ->
            currentState.copy(
                status = status
            )
        }
    }

    fun setSignupEmail(signupEmail: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                signupEmail = signupEmail
            )
        }
    }

    fun setSignupPassword(signupPassword: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                signupPassword = signupPassword
            )
        }
    }

    fun setSignupPasswordConfirmation(signupPasswordConfirmation: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                signupPasswordConfirmation = signupPasswordConfirmation
            )
        }
    }

    // for password resetting:

    fun setShowBottomSheet(showBottomSheet: Boolean) {
        mutableUiState.update { currentState ->
            currentState.copy(
                showBottomSheet = showBottomSheet
            )
        }
    }

    fun setEmailToResetPassword(emailToResetPassword: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                emailToResetPassword = emailToResetPassword
            )
        }
    }

    fun setPasswordResetStatus(passwordResetStatus: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                passwordResetStatus = passwordResetStatus
            )
        }
    }

    fun setSigninError(signinError: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                signinError = signinError
            )
        }
    }

    fun setSignupError(signupError: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                signupError = signupError
            )
        }
    }

    // Fake, simplified functions:

    fun signInWithEmailAndPassword() {
        viewModelScope.launch() {
            // Case: unknown user, not associated with the app
            if (mutableUiState.value.signinEmail == "unknown@gmail.com") {
                setSigninError(ErrorMessage.USER_ERROR_SIGNIN)
            }
            // Case: valid user stored on Firebase
            else {

            }
        }
    }

}