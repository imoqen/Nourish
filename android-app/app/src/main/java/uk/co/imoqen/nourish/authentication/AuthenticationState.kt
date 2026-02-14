package uk.co.imoqen.nourish.authentication

/**
 * Holds purely the state relating to login, to use across the UI.
 * Not specific to the authentication view, but determined in it; a product of it.
 */
data class AuthenticationState (
    val loggedIn: Boolean = false,
    val uid: String = "",
    val email: String = ""
)