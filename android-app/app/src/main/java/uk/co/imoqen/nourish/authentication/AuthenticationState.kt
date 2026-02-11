package uk.co.imoqen.nourish.authentication

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

// separation of concerns: this holds purely the logged-in user state, used across the ui, not specific to the authentication view
// but determined IN the authentication view: a product of it.
data class AuthenticationState (
    val loggedIn: Boolean = false,
    val uid: String = ""
)