package uk.co.imoqen.nourish.common

/**
 * Constants file consisting of error messages used throughout the app.
 * Their relevant app area is provided in the first word, e.g. API, INPUT, PASSWORD
 */
object ErrorMessage {
    // API errors, relating to LLM requests
    const val API_ERROR_SERVICE_UNREACHABLE = "An error occurred. Please check your device's internet " +
            "connection and ensure that the Nourish app has permission to access the internet."
    const val API_ERROR_BLANK_PROMPT = "Prompt cannot be blank."

    // Input errors for input fields
    const val INPUT_ERROR_BLANK = "All field(s) must not be blank."

    // Authentication errors involving password and account
    const val PASSWORD_ERROR_CHANGE = "Password could not be updated."
    const val PASSWORD_ERROR_MATCH = "Passwords do not match."

    const val DELETE_ERROR = "Account could not be deleted."

    const val USER_ERROR_SIGNUP = "Your sign up attempt was unsuccessful."
    const val USER_ERROR_SIGNIN = "Your sign in attempt was unsuccessful."

    const val USER_ERROR_EMAIL_SEND = "Password reset email failed to send."
}