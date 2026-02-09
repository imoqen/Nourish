package uk.co.imoqen.nourish.authentication

import android.content.Context
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

    fun isLoggedIn(): Boolean
}