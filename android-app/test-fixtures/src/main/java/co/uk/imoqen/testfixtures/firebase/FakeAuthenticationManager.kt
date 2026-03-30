package co.uk.imoqen.testfixtures.firebase

import android.app.Activity
import android.content.Context
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

    fun setLoginState(loggedInState: Boolean, uid: String = "") {
        mutableAuthenticationState.update { currentState ->
            currentState.copy(
                loggedIn = loggedInState,
                uid = uid
            )
        }
    }

    override fun signOut() {
        setLoginState(false)
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
        setLoginState(true, "google_uid")
    }

    override suspend fun passwordResetRequest(email: String) {
        // Password reset request only works for test@gmail.com
        if (email != "test@gmail.com") {
            throw RuntimeException("Fake password reset request failure")
        }
    }

    // mocks the password based accounts by setting their uid to "test_uid"
    override suspend fun hasPasswordProvider(): Boolean {
        return mutableAuthenticationState.value.uid == "test_uid"
    }

    override suspend fun reauthenticate(password: String) {
        // only failure if password is invalid
        if (password == "invalid") {
            throw RuntimeException("Fake reauthentication error")
        }
    }

    override suspend fun updatePassword(newPassword: String) {
        if (newPassword == "incorrect_pw") {
            throw RuntimeException("Fake password error")
        }
    }

    override suspend fun deletePasswordAccount() {
        // do nothing to simulate success
    }

    override suspend fun deleteGoogleAccount(
        activity: Activity,
    ) {
        if (!mutableAuthenticationState.value.loggedIn) {
            throw Exception()
        }
        // otherwise do nothing to simulate success
    }

    override suspend fun getUserToken(): String? {
        if (!mutableAuthenticationState.value.loggedIn) {
            return null
        }
        if (mutableAuthenticationState.value.uid == "invalid") {
            return "triggerError"
        }
        if (mutableAuthenticationState.value.uid == "triggerRecipeNotPossible") {
            return "triggerRecipeNotPossible"
        }
        return "validToken"
    }

    override fun getUid(): String {
        if (!mutableAuthenticationState.value.loggedIn) {
            return ""
        }
        return mutableAuthenticationState.value.uid
    }

    override fun getEmail(): String? {
        return "test@gmail.com"
    }

    override fun getIsFirstLogin(): Boolean {
        return true // first time logging in always
    }
}