package uk.co.imoqen.nourish.authentication

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

// purpose: stores things in the auth ui
data class AuthenticationUiState(
    // for logging in:
    val email: String = "",
    val password: String = "",

    // for signing up:
    val signupEmail: String = "",
    val signupPassword: String = "",
    val signupPasswordConfirmation: String = "",

    val status: AuthenticationUiStatus = AuthenticationUiStatus.LOGIN // login page by default
)

// to hold which screen on (login or signup) - separate from base navigation as must not be back navigatable once logged in
enum class AuthenticationUiStatus {
    LOGIN,
    SIGNUP
}