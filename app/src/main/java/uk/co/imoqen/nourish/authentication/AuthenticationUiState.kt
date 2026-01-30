package uk.co.imoqen.nourish.authentication

import com.google.firebase.auth.FirebaseUser
import uk.co.imoqen.nourish.user.User

// purpose: stores the currently logged in user. could expand in future to store any other auth things needed
data class AuthenticationUiState(
    val currentUser: FirebaseUser? = null // null if logged out, not null if logged in. null by default.
)