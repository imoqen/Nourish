package uk.co.imoqen.nourish.firebase

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uk.co.imoqen.nourish.authentication.AuthenticationState
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager

class FakeAuthenticationManager(): AuthenticationManager {

    private val mutableAuthenticationState = MutableStateFlow(AuthenticationState())

    override val authenticationState: StateFlow<AuthenticationState> = mutableAuthenticationState.asStateFlow()

    override suspend fun addIfNotExists() {
        // do nothing
    }

    override fun updateLoggedInState() {
        setLoginState(false) // logged out by default
    }

    fun setLoginState(loggedInState: Boolean, uid: String = "") {
        mutableAuthenticationState.update { currentState ->
            currentState.copy(
                loggedIn = loggedInState,
                uid = uid
            )
        }
    }

    override fun signOut() {
        TODO("Not yet implemented")
    }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String) {
        // valid email address
        if (email == "test@gmail.com") {
            setLoginState(true, "test_uid")
        }
        // otherwise, throw an error as all other email address are invalid.
        else {
            throw RuntimeException("Fake sign in failure")
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        // Unknown email address, not registered on the app:
        if (email == "unknown@gmail.com") {
            throw RuntimeException("Fake sign in failure")
        }
        // Known email address, registered on the app:
        // dont throw anything; success.
        else {
            setLoginState(true, "test_uid")
        }
    }

    override suspend fun signInWithGoogle(context: Context) {
        // simply log in the user
        setLoginState(true, "test_uid")
    }

    override suspend fun passwordResetRequest(email: String) {
        // Password reset request only works for test@gmail.com
        if (email != "test@gmail.com") {
            throw RuntimeException("Fake password reset request failure")
        }
    }

    override suspend fun hasPasswordProvider(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getCredentials(password: String): AuthCredential? {
        TODO("Not yet implemented")
    }

    override suspend fun reauthenticate(password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePassword(newPassword: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePasswordAccount() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGoogleAccount(
        activity: Activity,
        provider: OAuthProvider
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getUserToken(): GetTokenResult? {
        TODO("Not yet implemented")
    }
}