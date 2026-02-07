package uk.co.imoqen.nourish.authentication

import android.util.Log
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.co.imoqen.nourish.ui.theme.NourishTheme

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
    val authenticationManager = remember {
        AuthenticationManager()
    }

    val coroutineScope = rememberCoroutineScope()


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

            val ctxt = LocalContext.current
            Button(
                onClick = {
                    authenticationManager.signInWithGoogle(ctxt)
                        .onEach { response ->
                            if (response is AuthenticationResponse.Success) {
                                Log.i("login", "login success google")
                                // redetermine the logged in user to trigger recomposition:
                                authenticationViewModel.updateLoggedInStatus()
                            } else {
                                Log.i("login", "login error google")
                            }
                        }
                        .launchIn(coroutineScope) // have to launch in coroutine scope or else wont return anything
                }
            ) {
                Text("Sign in with Google")
            }

            val sheetState = rememberModalBottomSheetState()
            val scope = rememberCoroutineScope()
            var showBottomSheet by remember { mutableStateOf(false) }
            var emailToResetPassword by remember { mutableStateOf("") }
            var passwordResetStatus by remember { mutableStateOf("") }

                    Button(
                        onClick = {
                            showBottomSheet = true
                        }
                    ) {
                        Text("reset password")
                    }

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState
                    ) {
                        // Sheet content
                        Column() {
                            // email for password reset:
                            OutlinedTextField(
                                value = emailToResetPassword,
                                onValueChange = { newEmail ->
                                    emailToResetPassword = newEmail
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
                                    if (emailToResetPassword == "") {
                                        passwordResetStatus = "The email cannot be blank."
                                        return@Button // early return
                                    }
                                    Firebase.auth.sendPasswordResetEmail(emailToResetPassword)
                                        .addOnSuccessListener{
                                            passwordResetStatus = "A password reset email has been sent to $emailToResetPassword. Please follow the instructions in this email.\nCan't find the email? make sure to check your spam folder!"
                                        }
                                        .addOnFailureListener { error ->
                                            passwordResetStatus = if (error.message != null) {
                                                error.message!!
                                            } else {
                                                "The password reset email failed to send."
                                            }
                                        }
                                }
                            ) {
                                Text("Reset password")
                            }

                            if (passwordResetStatus != "") {
                                Text(passwordResetStatus)
                            }

                            Button(onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }) {
                                Text("Hide bottom sheet")
                            }
                        }
                    }
                }


                    /*Firebase.auth.sendPasswordResetEmail(email)
                        .then(() => {
                            // Password reset email sent!
                            // ..
                        })
                    .catch((error) => {
                    var errorCode = error.code;
                    var errorMessage = error.message;
                    // ..
                });

                     */

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
                        authenticationManager.signUpWithUsernameAndPassword(uiState.signupEmail, uiState.signupPassword)
                            .onEach { response ->
                                if (response is AuthenticationResponse.Success) {
                                    Log.i("signup", "login success signup")
                                    // redetermine the logged in user to trigger recomposition:
                                    authenticationViewModel.updateLoggedInStatus()
                                } else {
                                    Log.i("signup", "login error signup")
                                }
                            }
                            .launchIn(coroutineScope) // have to launch in coroutine scope or else wont return anything
                    }
                }
            ) {
                Text("Sign up")
            }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    NourishTheme {
        LoginScreen()
    }
}*/