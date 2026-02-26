package uk.co.imoqen.nourish.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.db.user.User
import uk.co.imoqen.nourish.db.user.UserRepository
import uk.co.imoqen.nourish.db.user.UserRepositoryImpl
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import javax.inject.Inject

/**
 * ViewModel for Authentication.
 * Dependent on the AuthenticationManager to perform authentication based actions such as
 * sign up, sign in, password change.
 * @param authenticationManager - dependent on the AuthenticationManager
 * @param uiEventBus - event bus for handling Ui events such as Toasts, shared across the app
 *
 */
@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager,
    private val uiEventBus: UiEventBus,
    private val userRepository: UserRepository,
): ViewModel() {
    // Purpose: pattern to encapsulate the mutable state to ensure it is not mutable from the viewmodel.
    // pattern taken from advice in: https://medium.com/@rowaido.game/managing-ui-states-in-jetpack-compose-7eb15e4f6931
    private val mutableUiState = MutableStateFlow(AuthenticationUiState())
    // Observable from the Authentication composables; not used within the view model.
    val uiState: StateFlow<AuthenticationUiState> = mutableUiState.asStateFlow()

    // Setters for the UiState values
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

    // only add the user if not already in. this conflict is handled by the Dao (onconflict).
    fun addUserToRoom() {
        viewModelScope.launch() {
            val uid = authenticationManager.getUid()
            val email = authenticationManager.getEmail()
            if (uid !=null && email != null) {
                userRepository.insert(User(uid, email))
            }
        }
    }

    // Functions used within the View to perform actions:

    /**
     * Sign in with email and password: password-provider account.
     */
    fun signInWithEmailAndPassword() {
        viewModelScope.launch() {
            if (mutableUiState.value.signinEmail == "" || mutableUiState.value.signinPassword == "") {
                setSigninError(ErrorMessage.INPUT_ERROR_BLANK)
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNIN))
                return@launch
            }
            setSigninError("")
            // runCatching is used to call a suspend error and handle exceptions appropriately
            // https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/run-catching.html
            runCatching {
                authenticationManager.signInWithEmailAndPassword(
                    mutableUiState.value.signinEmail,
                    mutableUiState.value.signinPassword
                )
                // then add the user to firestore if they don't already exist in firestore:
                authenticationManager.addIfNotExists()
                // then add to local db room:
                addUserToRoom()
            }
                .onSuccess {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Sign in successful."))
                    // reset ui state for sign in:
                    resetUiState()
                }
                .onFailure {
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNIN))
                }
        }
    }

    /**
     * Sign in with Google account: Google-provider account.
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch() {
            runCatching {
                authenticationManager.signInWithGoogle(context)
                // then add the user to firestore if they don't already exist in firestore:
                authenticationManager.addIfNotExists()
                // then add to local db room:
                addUserToRoom()
            }
                .onSuccess {
                    resetUiState()
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Sign in successful."))
                }
                .onFailure { e ->
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNIN))
                }
        }
    }

    /**
     * Sign up with email and password: create a password-provider account.
     */
    fun signUpWithEmailAndPassword() {
        viewModelScope.launch() {
            // Blank input fields:
            if (
                mutableUiState.value.signupEmail == "" ||
                mutableUiState.value.signupPassword == "" ||
                mutableUiState.value.signupPasswordConfirmation == ""
            ) {
                setSignupError(ErrorMessage.INPUT_ERROR_BLANK)
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNUP))
                return@launch
            }
            // Passwords do not match:
            if (mutableUiState.value.signupPassword != mutableUiState.value.signupPasswordConfirmation) {
                setSignupError(ErrorMessage.PASSWORD_ERROR_MATCH)
                uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNUP))
                return@launch
            }
            runCatching {
                authenticationManager.signUpWithEmailAndPassword(
                    mutableUiState.value.signupEmail,
                    mutableUiState.value.signupPassword
                )
                // then add the user to firestore if they don't already exist in firestore:
                authenticationManager.addIfNotExists()
                // then add to local db room:
                addUserToRoom()
            }
                .onSuccess {
                    resetUiState()
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar("Sign up successful. You have now been signed in."))
                }
                .onFailure { fail ->
                    if (fail.message != null && fail.message!!.contains(ErrorMessage.FIREBASE_PASSWORD_ERROR)) {
                        setSignupError(ErrorMessage.USER_ERROR_WEAK_PASSWORD)
                    } else {
                        uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_SIGNUP_EMAIL))
                    }
                }
        }
    }

    /**
     * Send a password reset request email to the associated account.
     * The user can then reset their password using this link.
     * If the account is a Google-provider account, this will add password-provider to their account;
     * so their account will now use both password and Google login.
     */
    fun passwordResetRequest() {
        if (mutableUiState.value.emailToResetPassword == "") {
            setPasswordResetStatus(ErrorMessage.INPUT_ERROR_BLANK)
            return // early return
        }
        viewModelScope.launch() {
            runCatching {
                authenticationManager.passwordResetRequest(mutableUiState.value.emailToResetPassword)
            }
                .onSuccess {
                    setPasswordResetStatus(
                        "A password reset email has been sent to ${mutableUiState.value.emailToResetPassword}.\n" +
                                "Please follow the instructions in this email. \n\n" +
                                "Can't find the email? " +
                                "Please check your spam folder!")
                }
                .onFailure {
                    setPasswordResetStatus(ErrorMessage.USER_ERROR_EMAIL_SEND)
                    uiEventBus.enqueueEvent(UiEvent.ShowSnackbar(ErrorMessage.USER_ERROR_EMAIL_SEND))
                }
        }
    }

    /**
     * Auxiliary function to run upon successful sign in / sign up, to clear the state.
     */
    private fun resetUiState() {
        setSigninEmail("")
        setSigninPassword("")
        setSignupEmail("")
        setSignupPassword("")
        setSignupPasswordConfirmation("")
        setSigninError("")
        setSignupError("")
    }

}