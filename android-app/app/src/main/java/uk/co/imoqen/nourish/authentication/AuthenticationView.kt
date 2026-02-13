package uk.co.imoqen.nourish.authentication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation

/**
 * View for Authentication, where the user can sign up, sign in and send a password reset link to
 * their account's email address.
 * @param authenticationViewModel - the view model holding methods and state for use in the view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier,
    authenticationViewModel: AuthenticationViewModel,
) {
    // Ui State:
    val uiState by authenticationViewModel.uiState.collectAsState()
    val context = LocalContext.current // required for Google sign in
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Sign in View:
        if (uiState.status == AuthenticationUiStatus.SIGNIN) {
            Text("Sign in")

            // Email:
            OutlinedTextField(
                value = uiState.signinEmail,
                onValueChange = { newEmail ->
                    authenticationViewModel.setSigninEmail(newEmail)
                },
                placeholder = {
                    Text("Enter email address")
                },
                leadingIcon = {
                    Icons.Filled.MailOutline
                },
                modifier = Modifier.fillMaxWidth()
            )
            // Password:
            OutlinedTextField(
                value = uiState.signinPassword,
                onValueChange = { newPassword ->
                    authenticationViewModel.setSigninPassword(newPassword)
                },
                placeholder = {
                    Text("Enter password")
                },
                leadingIcon = {
                    Icons.Filled.Lock
                },
                visualTransformation = PasswordVisualTransformation(), // to prevent password from being visible when typed
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    authenticationViewModel.signInWithEmailAndPassword()
                }
            ) {
                Text("Sign in")
            }

            Text("Don't have an account?")
            Button(
                onClick = {
                    authenticationViewModel.setStatus(AuthenticationUiStatus.SIGNUP)
                }
            ) {
                Text("Sign up")
            }
            Text("Or:")

            Button(
                onClick = {
                    authenticationViewModel.signInWithGoogle(context)
                }
            ) {
                Text("Sign in with Google")
            }

            if (uiState.signinError != "") {
                Text(uiState.signinError)
            }

            val sheetState = rememberModalBottomSheetState()
            Button(
                onClick = {
                    authenticationViewModel.setShowBottomSheet(true)
                }
            ) {
                Text("Forgotten your password?")
            }

                if (uiState.showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            authenticationViewModel.setShowBottomSheet(false)
                        },
                        sheetState = sheetState
                    ) {
                        Column() {
                            // Email for password reset:
                            OutlinedTextField(
                                value = uiState.emailToResetPassword,
                                onValueChange = { newEmail ->
                                    authenticationViewModel.setEmailToResetPassword(newEmail)
                                },
                                placeholder = {
                                    Text("Enter account email")
                                },
                                leadingIcon = {
                                    Icons.Filled.Email
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    authenticationViewModel.passwordResetRequest()
                                }
                            ) {
                                Text("Reset password")
                            }

                            if (uiState.passwordResetStatus != "") {
                                Text(uiState.passwordResetStatus)
                            }
                        }
                    }
                }
        }
        // Sign up View:
        else {
            BackHandler {
                // handle back button press by going back to login view if on sign up view:
                authenticationViewModel.setStatus(AuthenticationUiStatus.SIGNIN)
            }

            // Back to login?:
            Button(
                onClick = {
                    authenticationViewModel.setStatus(AuthenticationUiStatus.SIGNIN)
                }
            ) {
                Text("Back to login")
            }

            // Sign up form:
            OutlinedTextField(
                value = uiState.signupEmail,
                onValueChange = { newEmail ->
                    authenticationViewModel.setSignupEmail(newEmail)
                },
                placeholder = {
                    Text("Enter email address")
                },
                leadingIcon = {
                    Icons.Filled.MailOutline
                },
                modifier = Modifier.fillMaxWidth()
            )
            // Password:
            OutlinedTextField(
                value = uiState.signupPassword,
                onValueChange = { newPassword ->
                    authenticationViewModel.setSignupPassword(newPassword)
                },
                placeholder = {
                    Text("Enter password")
                },
                leadingIcon = {
                    Icons.Filled.Lock
                },
                visualTransformation = PasswordVisualTransformation(), // to prevent password from being visible when typed
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.signupPasswordConfirmation,
                onValueChange = { newPassword ->
                    authenticationViewModel.setSignupPasswordConfirmation(newPassword)
                },
                placeholder = {
                    Text("Confirm password")
                },
                leadingIcon = {
                    Icons.Filled.Lock
                },
                visualTransformation = PasswordVisualTransformation(), // to prevent password from being visible when typed
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.signupError != "") {
                Text(uiState.signupError)
            }


            Button(
                onClick = {
                    authenticationViewModel.signUpWithEmailAndPassword()
                }
            ) {
                Text("Sign up")
            }
        }
    }
}