package uk.co.imoqen.nourish.authentication

/**
 * UiState for Authentication: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably.
 */
data class AuthenticationUiState(
    // For signing in:
    val signinEmail: String = "",
    val signinPassword: String = "",
    val signinError: String = "",

    // For signing up:
    val signupEmail: String = "",
    val signupPassword: String = "",
    val signupPasswordConfirmation: String = "",
    val signupError: String = "",

    // For resetting password:
    val showBottomSheet: Boolean = false,
    val emailToResetPassword: String = "",
    val passwordResetStatus: String = "",

    // Tracks which view the user is on (sign in or sign up).
    val status: AuthenticationUiStatus = AuthenticationUiStatus.SIGNIN // Signin page by default
)

/**
 * Holds which screen the user is currently on in the Authentication flow (Sign in / Sign up);
 * separate from base navigation as must not be back-navigable once logged in
 */
enum class AuthenticationUiStatus {
    SIGNIN,
    SIGNUP
}