package uk.co.imoqen.nourish.authentication

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.StateFlow

interface AuthenticationManager {

    // keeps track of if logged in
    val authenticationState: StateFlow<AuthenticationState>
    fun getCurrentUser(): FirebaseUser?

    fun addUserToFirestore(user: FirebaseUser)

    suspend fun addIfNotExists()

    fun firebaseSignOut()

    fun updateLoggedInState()

    fun signOut()

    suspend fun signUpWithUsernameAndPassword(email: String, password: String)

    suspend fun signInWithEmailAndPassword(email: String, password: String)

    suspend fun signInWithGoogle(context: Context)

    suspend fun deleteAllUserData(user: FirebaseUser)

    suspend fun passwordResetRequest(email: String)

    // used in profile view model:

    suspend fun hasPasswordProvider(): Boolean

    fun isLoggedIn(): Boolean

    suspend fun getCredentials(password: String): AuthCredential?

    suspend fun reauthenticate(password: String)

    suspend fun updatePassword(newPassword: String)

    suspend fun deletePasswordAccount()

    suspend fun deleteGoogleAccount(activity: Activity, provider: OAuthProvider)
}