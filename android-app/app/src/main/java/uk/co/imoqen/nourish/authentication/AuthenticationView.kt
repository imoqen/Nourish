package uk.co.imoqen.nourish.authentication

import uk.co.imoqen.nourish.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.imoqen.nourish.common.GoogleButton
import uk.co.imoqen.nourish.common.GradientButton
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishTextDivider
import uk.co.imoqen.nourish.common.NourishTextField

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
    val keyboardFocusManager = LocalFocusManager.current // To close the keyboard once user submits input

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=84.dp).background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.TopCenter,

        ) {
            Image(
                painter = painterResource(R.drawable.nourish_title),
                contentDescription = "Nourish Logo",
                modifier = Modifier.height(82.dp).padding(16.dp),
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onBackground
                )
            )
        }

        // ensure bottom box sticks to bottom:
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { clip = false }  // to prevent parent clipping
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 28.dp,
                            shape = RoundedCornerShape(32.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = 32.dp) // room for logo
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp,)
                            .padding(bottom = 40.dp),
                             // extra padding at bottom for the built in mobile navbar
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                            // Sign in View:
                            if (uiState.status == AuthenticationUiStatus.SIGNIN) {
                                Text(
                                    text = "Sign in",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom=6.dp)
                                )

                                // Email:
                                NourishTextField(
                                    value = uiState.signinEmail,
                                    onValueChange = { newEmail ->
                                        authenticationViewModel.setSigninEmail(newEmail)
                                    },
                                    placeholder = "Enter email address",
                                    icon = Icons.Filled.MailOutline
                                )
                                // Password:
                                NourishTextField(
                                    value = uiState.signinPassword,
                                    onValueChange = { newPassword ->
                                        authenticationViewModel.setSigninPassword(newPassword)
                                    },
                                    placeholder = "Enter password",
                                    icon = Icons.Filled.Lock,
                                    visualTransformation = PasswordVisualTransformation()
                                )

                                if (uiState.signinError != "") {
                                    Text(
                                        uiState.signinError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                NourishButton(
                                    onClick = {
                                        keyboardFocusManager.clearFocus()
                                        authenticationViewModel.signInWithEmailAndPassword()
                                    },
                                    text = "Sign in"
                                )

                                NourishTextDivider("or")

                                GoogleButton(onClick = { authenticationViewModel.signInWithGoogle(context) })

                                NourishTextDivider("Don't have an account yet?")

                                Row() {
                                    NourishButton(
                                        text = "Sign Up",
                                        onClick = {
                                            authenticationViewModel.setStatus(AuthenticationUiStatus.SIGNUP)
                                        }
                                    )

                                    NourishButton(
                                        text = "Forgot Password",
                                        onClick = {
                                            authenticationViewModel.setShowBottomSheet(true)
                                        }
                                    )
                                }

                                val sheetState = rememberModalBottomSheetState()

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
                                                    authenticationViewModel.setEmailToResetPassword(
                                                        newEmail
                                                    )
                                                },
                                                placeholder = {
                                                    Text("Enter account email")
                                                },
                                                leadingIcon = {
                                                    Icons.Filled.Email
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            NourishButton(
                                                text = "Reset Password",
                                                onClick = {
                                                    authenticationViewModel.passwordResetRequest()
                                                }
                                            )

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
                                        authenticationViewModel.setSignupPasswordConfirmation(
                                            newPassword
                                        )
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
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.nourish_icon),
                        contentDescription = "Nourish Logo",
                        modifier = Modifier.size(112.dp).offset(y = (-70).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundSquare() {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Sets a fixed size of 100x100 dp
            .background(Color.White)
            .clip(RoundedCornerShape(100.dp))
    )
}