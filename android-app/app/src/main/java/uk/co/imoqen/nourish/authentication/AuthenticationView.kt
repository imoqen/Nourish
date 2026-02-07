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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier,
    authenticationViewModel: AuthenticationViewModel
) {

    // the ui state from the view model:
    val uiState by authenticationViewModel.uiState.collectAsState()

    // stores the current logged in user, method provided by firebase:
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        // display login view:
        if (uiState.status == AuthenticationUiStatus.LOGIN) {

            Text("Sign in")
            // email:
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { newEmail ->
                    authenticationViewModel.setEmail(newEmail)
                },
                placeholder = {
                    Text("Enter email address")
                },
                leadingIcon = {
                    Icons.Filled.MailOutline
                },
                modifier = Modifier.fillMaxWidth()
            )
            // password:
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { newPassword ->
                    authenticationViewModel.setPassword(newPassword)
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

            val sheetState = rememberModalBottomSheetState()
                    Button(
                        onClick = {
                            authenticationViewModel.setShowBottomSheet(true)
                        }
                    ) {
                        Text("reset password")
                    }

                if (uiState.showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            authenticationViewModel.setShowBottomSheet(false)
                        },
                        sheetState = sheetState
                    ) {
                        // Sheet content
                        Column() {
                            // email for password reset:
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
        // otherwise display sign up:
        else {
            BackHandler {
                // handle back button press by going back to login view if on sign up view:
                authenticationViewModel.setStatus(AuthenticationUiStatus.LOGIN)
            }

            // back to login?:
            Button(
                onClick = {
                    authenticationViewModel.setStatus(AuthenticationUiStatus.LOGIN)
                }
            ) {
                Text("Back to login")
            }

            // sign up form:
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
            // password:
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

            Button(
                onClick = {
                    if (uiState.signupPassword == uiState.signupPasswordConfirmation) {
                        authenticationViewModel.signUpWithUsernameAndPassword()
                    }
                }
            ) {
                Text("Sign up")
            }
        }
    }
}