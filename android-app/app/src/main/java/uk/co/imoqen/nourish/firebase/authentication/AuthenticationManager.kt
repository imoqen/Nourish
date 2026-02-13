package uk.co.imoqen.nourish.firebase.authentication

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.StateFlow
import uk.co.imoqen.nourish.authentication.AuthenticationState

interface AuthenticationManager {

    // State to manage the currently logged in user's uid and state (logged in or logged out)
    val authenticationState: StateFlow<AuthenticationState>

    /**
     * Adds a user to the Firebase Firestore if they are not already in it, to ensure the Firestore
     * is always kept up to date with all user data.
     */
    suspend fun addIfNotExists()

    /**
     * Sign out the currently signed in user and update login status.
     */
    fun signOut()

    // Methods used by the AuthenticationViewModel

    /**
     * Sign up with email and password: create a password-provider account.
     */
    suspend fun signUpWithEmailAndPassword(email: String, password: String)

    /**
     * Sign in with email and password: password-provider account.
     * @param email - the email of the account to sign in
     * @param password - the password of the account to sign in
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String)

    /**
     * Sign in with a Google account: Google-provider account.
     * @param context - the local context, allowing the default_web_client_id string to be obtained.
     */
    suspend fun signInWithGoogle(context: Context)

    /**
     * Sends the password request email to the given email address. Only sends if the email is
     * associated with an account on the app.
     * @param email - the email to send the request to.
     */
    suspend fun passwordResetRequest(email: String)

    // Methods used by the ProfileViewModel:

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
     */
    suspend fun deleteGoogleAccount(activity: Activity)

    // Methods used in the RecipeDiscoveryViewModel:

    /**
     * Gets the current token of a user, containing the JWT (Java Web Token) string allowing for
     * authentication.
     * This can be passed into the Bearer Authorization header of API requests to authenticate a user.
     * @returns the token as a string if the user is logged in and valid, null otherwise.
     */
    suspend fun getUserToken(): String?
}