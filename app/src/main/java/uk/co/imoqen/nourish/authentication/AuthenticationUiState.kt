package uk.co.imoqen.nourish.authentication

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

// purpose: stores the currently logged in user. could expand in future to store any other auth things needed
data class AuthenticationUiState(
    val currentUser: FirebaseUser? = Firebase.auth.currentUser, // null if logged out, not null if logged in. null by default.

    // for logging in:
    val email: String = "",
    val password: String = ""
)