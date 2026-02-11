package uk.co.imoqen.nourish.authentication

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
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

    fun isLoggedIn(): Boolean

    suspend fun getUserToken(): GetTokenResult?

    // Methods specific to use in the ProfileViewModel:

    /**
     * Determines whether the currently logged in user has a password-provider.
     * There are two types of provider supported:
     * - Google (sign up/in with Google)
     * - Password (sign up/in with email and password)
     * This method checks if the user has the latter type of account.
     * @returns true if has password provider, false otherwise.
     */
    suspend fun hasPasswordProvider(): Boolean

    /**
     * Gets the credentials of the current user, for use during reauthentication.
     * @param password - the password to get the credentials for, using the current email.
     * @returns valid AuthCredentials if password is valid for the email, null otherwise.
     */
    suspend fun getCredentials(password: String): AuthCredential?

    /**
     * Reauthenticates an account. This is required to perform sensitive actions such as
     * password changes and account deletion; required for an extra level of security.
     * Only performed on password-provider accounts (Google accounts must follow an alternative flow).
     * https://firebase.google.com/docs/auth/admin/manage-sessions
     * @param password - the password to reauthenticate the email with
     */
    suspend fun reauthenticate(password: String)

    /**
     * Updates the password of a password-provider account. Requires reauthentication beforehand.
     */
    suspend fun updatePassword(newPassword: String)

    /**
     * Deletes a password-provider account.
     */
    suspend fun deletePasswordAccount()

    /**
     * Deletes a Google-provider account.
     * This is based on an approach found on StackOverflow:
     * https://stackoverflow.com/questions/40898952/how-to-re-authenticate-a-user-on-firebase-with-google-provider
     * it requires navigating the user into their web app and following a flow to reauthenticate
     * their Google account. If successful, the account is deleted and the user is logged out.
     * @param activity - the current activity, allowing the web app to be opened for reauthentication
     * @param provider - the Google OAuthProvider
     */
    suspend fun deleteGoogleAccount(activity: Activity, provider: OAuthProvider)
}