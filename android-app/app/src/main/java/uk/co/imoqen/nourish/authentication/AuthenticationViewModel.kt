package uk.co.imoqen.nourish.authentication

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.R
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager
): ViewModel() {

    // SEPARATION OF CONCERNS: ui state vs state

    // UI STATE:
    // Purpose: pattern to encapsulate the mutable state to ensure it is not mutable from the viewmodel.
    // this is what is editable, only within this view model and not within composables.
    // pattern taken from advice in: https://medium.com/@rowaido.game/managing-ui-states-in-jetpack-compose-7eb15e4f6931
    // this is used IN the view model and NOT OUTSIDE of it (not in the view):
    private val mutableUiState = MutableStateFlow(AuthenticationUiState())

    // this is the encapsulated, immutable state made from the mutable state above. this is what
    // is observable from the Authentication composables. not used within the view model.
    val uiState: StateFlow<AuthenticationUiState> = mutableUiState.asStateFlow()

    // STATE:
    private val mutableState = MutableStateFlow(AuthenticationState())
    val state: StateFlow<AuthenticationState> = mutableState.asStateFlow()

    //val authenticationManager = AuthenticationManager()

    // runs when app starts
    init {
        updateLoggedInStatus()
    }

    // STATE:
    // update logged in state
    fun setLoggedIn(loggedInState: Boolean) {
        mutableState.update { currentState ->
            currentState.copy(
                loggedIn = loggedInState
            )
        }
    }
    fun signOut() {

        // then sign out on firebase too:
        if (Firebase.auth.currentUser != null) {
            Firebase.auth.signOut()
        }
        // and update stored logged in status:
        updateLoggedInStatus()
    }

    // UI STATE:
    fun setEmail(email: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                email = email
            )
        }
    }

    fun setPassword(password: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                password = password
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

    // calling the authentication manager methods from the view model:
    fun signInWithEmailAndPassword() {
        viewModelScope.launch() {
            // because suspend function:
            // this can throw error
            // https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/run-catching.html
            runCatching {
                authenticationManager.signInWithEmailAndPassword(
                    mutableUiState.value.email,
                    mutableUiState.value.password
                )
            }
                .onSuccess {
                    // add the user to firestore if they dont already exist in firestore:
                    authenticationManager.addIfNotExists()
                    setLoggedIn(true) // update logged in state for use around the app
                }
                .onFailure {
                    // maybe do something
                }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch() {
            runCatching {
                authenticationManager.signInWithGoogle(context)
            }
                .onSuccess {
                    authenticationManager.addIfNotExists()
                    setLoggedIn(true) // update logged in state for use around the app
                }
                .onFailure {

                }
        }
    }

    fun signUpWithUsernameAndPassword() {
        viewModelScope.launch() {
            runCatching {
                authenticationManager.signUpWithUsernameAndPassword(
                    mutableUiState.value.signupEmail,
                    mutableUiState.value.signupPassword
                )
            }
                .onSuccess {
                    authenticationManager.addIfNotExists()
                    setLoggedIn(true) // update logged in state for use around the app
                    Log.i("sign up:", "success")
                }
                .onFailure { fail ->
                    Log.i("sign up:", "fail: $fail")
                }
        }
    }

    fun passwordResetRequest() {
        if (mutableUiState.value.emailToResetPassword == "") {
            setPasswordResetStatus("The email cannot be blank.")
            return // early return
        }
        viewModelScope.launch() {
            runCatching {
                authenticationManager.passwordResetRequest(mutableUiState.value.emailToResetPassword)
            }
                .onSuccess {
                    setPasswordResetStatus("A password reset email has been sent to ${mutableUiState.value.emailToResetPassword}. Please follow the instructions in this email. Can't find the email? make sure to check your spam folder!")
                }
                .onFailure {
                    setPasswordResetStatus("The password reset email failed to send.")
                }
        }
    }

    fun updateLoggedInStatus() {
        setLoggedIn(authenticationManager.isLoggedIn())
    }
}