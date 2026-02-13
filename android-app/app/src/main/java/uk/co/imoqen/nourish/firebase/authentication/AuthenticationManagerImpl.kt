package uk.co.imoqen.nourish.firebase.authentication

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.authentication.AuthenticationState
import uk.co.imoqen.nourish.firebase.firestore.FirestoreManager
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The implementation of AuthenticationManager, the entry point in the app for communication with
 * Firebase Authentication and Firebase Firestore. These are dependencies, injected in as a Singleton
 * component to ensure only one exists within the app at a given time.
 * Most functions are suspend, which throw up exceptions to the viewmodel. The viewmodel then deals
 * with them appropriately in a runCatching block.
 * @param auth - FirebaseAuth authentication platform holding a list of all users.
 * @param firestoreManager - provides access to the FirebaseFirestore database holding user data.
 */
@Singleton
class AuthenticationManagerImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreManager: FirestoreManager
): AuthenticationManager {

    // State to manage the currently logged in user's uid and state (logged in or logged out)
    private val mutableAuthenticationState = MutableStateFlow(AuthenticationState())
    override val authenticationState: StateFlow<AuthenticationState> = mutableAuthenticationState.asStateFlow()

    // Runs when the Manager is created; upon app launch and sign in.
    init {
        updateLoggedInState()
    }

    /**
     * Update the logged in status using the setter; to set whether a user is currently logged in,
     * and if so, their uid.
     */
    override fun updateLoggedInState() {
        if (isLoggedIn()) {
            setLoginState(true, auth.currentUser!!.uid)
            return
        }
        setLoginState(false)
    }

    /**
     * Sign out the currently signed in user and update login status.
     */
    override fun signOut() {
        if (isLoggedIn()) {
            auth.signOut()
        }
        setLoginState(false)
    }

    /**
     * Setter for the state: sets the logged in state and uid of logged in user ("" if not logged in).
     */
    fun setLoginState(loggedInState: Boolean, uid: String = "") {
        mutableAuthenticationState.update { currentState ->
            currentState.copy(
                loggedIn = loggedInState,
                uid = uid
            )
        }
    }

    // Methods used in the AuthenticationViewModel:

    /**
     * Sign up with email and password: password-provider account.
     * @param email - the email of the account to sign up
     * @param password - the password of the account to sign up
     */
    override suspend fun signUpWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
        // If success, can now proceed to login:
        if (auth.currentUser != null) {
            setLoginState(true, auth.currentUser!!.uid) // set logged in if successful
        }
    }

    /**
     * Sign in with email and password: password-provider account.
     * @param email - the email of the account to sign in
     * @param password - the password of the account to sign in
     */
    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
        if (auth.currentUser != null) {
            setLoginState(true, auth.currentUser!!.uid) // set logged in if successful
        }
    }

    /**
     * Sign in with a Google account: Google-provider account.
     * @param context - the local context, allowing the default_web_client_id string to be obtained.
     */
    override suspend fun signInWithGoogle(context: Context) {
        // Builds the Google Id option and fields for Google sign in
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            // Default web client id from the resources, generated from details provided in
            // the google-services.json file provided by google cloud:
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false) // wont auto select an account
            .setNonce(generateNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credentialManager = CredentialManager.Companion.create(context)
        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential

        if (credential is CustomCredential) {
            if (credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                // Sign in with Google:
                val googleIdTokenCredential = GoogleIdTokenCredential.Companion
                    .createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null
                )
                auth.signInWithCredential(firebaseCredential).await()

                // Check if validly signed in:
                if (auth.currentUser != null) {
                    setLoginState(true, auth.currentUser!!.uid)
                }
            }
        }
    }

    /**
     * Sends the password request email to the given email address. Only sends if the email is
     * associated with an account on the app.
     * @param email - the email to send the request to.
     */
    override suspend fun passwordResetRequest(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    /**
     * Adds a user to the Firebase Firestore if they are not already in it, to ensure the Firestore
     * is always kept up to date with all user data.
     */
    override suspend fun addIfNotExists() {
        if (!isLoggedIn()) {
            return // early return if not logged in; can't add.
        }
        val user = auth.currentUser!!
        firestoreManager.addUserToFirestoreIfNotExists(user)
    }

    // Methods used in the ProfileViewModel:

    /**
     * Determines whether the currently logged in user has a password provider.
     * There are two types of provider supported:
     * - Google (sign up/in with Google)
     * - Password (sign up/in with email and password)
     * This method checks if the user has the latter type of account.
     * @returns true if has password provider, false otherwise.
     */
    override suspend fun hasPasswordProvider(): Boolean {
        if (!isLoggedIn()) {
            return false
        }
        return auth.currentUser!!.providerData.any { it.providerId == "password" }
    }

    /**
     * Gets the credentials of the current user, for use during reauthentication.
     * @param password - the password to get the credentials for, using the current email.
     * @returns valid AuthCredentials if password is valid for the email, null otherwise.
     */
    override suspend fun getCredentials(password: String): AuthCredential? {
        if (!isLoggedIn()) {
            return null // early return if not logged in; no auth credentials.
        }
        return EmailAuthProvider.getCredential(
            auth.currentUser!!.email!!,
            password
        )
    }

    /**
     * Reauthenticates an account. This is required to perform sensitive actions such as
     * password changes and account deletion; required for an extra level of security.
     * Only performed on password-provider accounts (Google accounts must follow an alternative flow).
     * https://firebase.google.com/docs/auth/admin/manage-sessions
     * @param password - the password to reauthenticate the email with
     */
    override suspend fun reauthenticate(password: String) {
        // first get the credentials
        val credentials = getCredentials(password)
        if (credentials != null) { // therefore must also be logged in so can do currentUser!!
            // then reauthenticate with those credentials:
            auth.currentUser!!.reauthenticate(credentials).await()
        }
    }

    /**
     * Updates the password of a password-provider account. Requires reauthentication beforehand.
     */
    override suspend fun updatePassword(newPassword: String) {
        if (!isLoggedIn()) {
            return // early return if not logged in; no auth credentials.
        }
        auth.currentUser!!.updatePassword(newPassword)
    }

    /**
     * Deletes a password-provider account.
     */
    override suspend fun deletePasswordAccount() {
        if (!isLoggedIn()) {
            return // early return if not logged in; no auth credentials.
        }
        val user = auth.currentUser!!
        deleteAllUserData(user)
        user.delete()
            .addOnSuccessListener {
                signOut() // sign them out as the account is now deleted
            }
    }

    /**
     * Deletes a Google-provider account.
     * This is based on an approach found on StackOverflow:
     * https://stackoverflow.com/questions/40898952/how-to-re-authenticate-a-user-on-firebase-with-google-provider
     * it requires navigating the user into their web app and following a flow to reauthenticate
     * their Google account. If successful, the account is deleted and the user is logged out.
     * @param activity - the current activity, allowing the web app to be opened for reauthentication
     * @param provider - the Google OAuthProvider
     */
    override suspend fun deleteGoogleAccount(activity: Activity, provider: OAuthProvider) {
        if (!isLoggedIn()) {
            return // early return if not logged in; no auth credentials.
        }
        val user = auth.currentUser!!
        // reauthenticate by opening log in in users browser
        user.startActivityForReauthenticateWithProvider(
            activity,
            provider
        ).await()
        // delete users data on firebase firestore
        deleteAllUserData(user)
        // delete user from firebase authentication
        user.delete().await()
        // then sign out locally:
        signOut()
    }

    // Methods used in RecipeDiscoveryViewModel:

    /**
     * Gets the current token of a user, containing the JWT (Java Web Token) string allowing for
     * authentication.
     * This can be passed into the Bearer Authorization header of API requests to authenticate a user.
     * @returns the token result if the user is logged in and valid, null otherwise.
     */
    override suspend fun getUserToken(): GetTokenResult? {
        if (!isLoggedIn()) {
            return null // early return if not logged in; no auth credentials.
        }
        val user = auth.currentUser!!
        return user.getIdToken(true).await()
    }

    // Auxiliary functions, thus not declared in the interface

    /**
     * Private auxiliary function to generate a nonce, a number only used once.
     * This is used to build the GoogleIdOption when signing in with google, to provide freshness
     * to the request; hence preventing replay attacks. This is recommended by Google
     * https://android-developers.googleblog.com/2022/05/boost-security-of-your-app-with-nonce.html
     * @returns the nonce String
     */
    private fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        // convert to byte array:
        val bytes = rawNonce.toByteArray()
        // encrypt:
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    /**
     * Private auxiliary function to deletes all of a user's data from the Firebase Firestore.
     * This is to be performed when a user's account is deleted.
     * @param user - the user to delete the data of.
     */
    private suspend fun deleteAllUserData(user: FirebaseUser) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(user.uid)

        // Recursively delete all of the user data
        val tokenUsages = userRef.collection("token-usage").get().await().documents
        tokenUsages.forEach{ tokenUsage ->
            tokenUsage.reference.delete().await()
        }
        userRef.delete().await()
    }

    /**
     * Private auxiliary function to determine if a user is currently logged in.
     * @returns true if logged in, false otherwise.
     */
    private fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}