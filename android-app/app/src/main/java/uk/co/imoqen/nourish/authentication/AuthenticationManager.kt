package uk.co.imoqen.nourish.authentication

import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthenticationManager {
    fun getCurrentUser(): FirebaseUser?

    fun addUserToFirestore(user: FirebaseUser)

    fun addIfNotExists()

    fun firebaseSignOut()

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
}