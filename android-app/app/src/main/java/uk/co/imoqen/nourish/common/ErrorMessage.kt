package uk.co.imoqen.nourish.common

/**
 * Constants file consisting of error messages used throughout the app.
 * Their relevant app area is provided in the first word, e.g. API, INPUT, PASSWORD
 *
 * @author Imogen Pemberton, 2026
 */
object ErrorMessage {
    // API errors, relating to LLM requests
    const val API_ERROR_SERVICE_UNREACHABLE = "An error occurred. Please check your device's internet connection."
    const val API_ERROR_FAILED_TO_GEN = "An error occurred. Please try again later."
    const val API_ERROR_BLANK_PROMPT = "Input cannot be blank."

    // Input errors for input fields
    const val INPUT_ERROR_BLANK = "All field(s) must not be blank."

    // Authentication errors involving password and account
    const val PASSWORD_ERROR_CHANGE = "Password could not be updated."
    const val PASSWORD_ERROR_MATCH = "Passwords do not match."

    const val DELETE_ERROR = "Account could not be deleted."

    const val USER_ERROR_SIGNUP = "Your sign up attempt was unsuccessful."
    const val USER_ERROR_SIGNUP_EMAIL = "Your sign up attempt was unsuccessful. The email address may be invalid or taken."
    const val USER_ERROR_WEAK_PASSWORD = "Sign up unsuccessful. \nYour password must be at least 8 characters long and contain at least one number, upper case character, and lower case character."
    const val USER_ERROR_WEAK_PASSWORD_CHANGE = "Password not changed. \nYour password must be at least 8 characters long and contain at least one number, upper case character, and lower case character."

    const val USER_ERROR_SIGNIN = "Your sign in attempt was unsuccessful."

    const val USER_ERROR_EMAIL_SEND = "Password reset email failed to send."

    const val FIREBASE_PASSWORD_ERROR = "PASSWORD_DOES_NOT_MEET_REQUIREMENTS"
}