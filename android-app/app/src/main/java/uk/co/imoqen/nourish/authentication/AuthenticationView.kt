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
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.GoogleButton
import uk.co.imoqen.nourish.common.GradientButton
import uk.co.imoqen.nourish.common.NourishBottomCard
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishErrorText
import uk.co.imoqen.nourish.common.NourishH1
import uk.co.imoqen.nourish.common.NourishText
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
    val keyboardFocusManager =
        LocalFocusManager.current // To close the keyboard once user submits input

    NourishBottomCard(
        titleSection = {

            // TITLE:
            Image(
                painter = painterResource(R.drawable.nourish_title),
                contentDescription = "Nourish Title Text",
                modifier = Modifier.height(82.dp).padding(16.dp),
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onBackground
                )
            )

        },
        cardContents = {
            // Sign in View:
            if (uiState.status == AuthenticationUiStatus.SIGNIN) {
                NourishH1("Sign in")

                // Email:
                NourishTextField(
                    value = uiState.signinEmail,
                    onValueChange = { newEmail ->
                        authenticationViewModel.setSigninEmail(newEmail)
                    },
                    placeholder = "Enter email address",
                    icon = Icons.Filled.MailOutline,
                    onDone = { keyboardFocusManager.clearFocus() }
                )
                // Password:
                NourishTextField(
                    value = uiState.signinPassword,
                    onValueChange = { newPassword ->
                        authenticationViewModel.setSigninPassword(newPassword)
                    },
                    placeholder = "Enter password",
                    icon = Icons.Filled.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    onDone = { keyboardFocusManager.clearFocus() }
                )

                if (uiState.signinError != "") {
                    NourishErrorText(uiState.signinError)
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
                        text = "Sign up",
                        onClick = {
                            authenticationViewModel.setStatus(AuthenticationUiStatus.SIGNUP)
                        }
                    )

                    NourishButton(
                        text = "Forgot password",
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
                        sheetState = sheetState,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            NourishTextField(
                                value = uiState.emailToResetPassword,
                                onValueChange = { newEmail ->
                                    authenticationViewModel.setEmailToResetPassword(
                                        newEmail
                                    )
                                },
                                placeholder = "Enter account email",
                                icon = Icons.Filled.Email,
                                onDone = { keyboardFocusManager.clearFocus(force = true) }
                            )

                            // handle different cases of messages:
                            if (uiState.passwordResetStatus == ErrorMessage.INPUT_ERROR_BLANK || uiState.passwordResetStatus == ErrorMessage.USER_ERROR_EMAIL_SEND) {
                                NourishErrorText(uiState.passwordResetStatus)
                            } else if (uiState.passwordResetStatus != "") {
                                NourishText(uiState.passwordResetStatus)
                            }

                            NourishButton(
                                text = "Reset password",
                                onClick = {
                                    keyboardFocusManager.clearFocus(force = true)
                                    authenticationViewModel.passwordResetRequest()
                                }
                            )
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

                NourishButton(
                    text = "Back to Sign in",
                    onClick = {
                        keyboardFocusManager.clearFocus()
                        authenticationViewModel.setStatus(AuthenticationUiStatus.SIGNIN)
                    }
                )

                // Sign up form:
                NourishTextField(
                    value = uiState.signupEmail,
                    onValueChange = { newEmail ->
                        authenticationViewModel.setSignupEmail(newEmail)
                    },
                    placeholder = "Enter email address",
                    icon = Icons.Filled.MailOutline,
                    onDone = { keyboardFocusManager.clearFocus() }
                )

                // Password:
                NourishTextField(
                    value = uiState.signupPassword,
                    onValueChange = { newPassword ->
                        authenticationViewModel.setSignupPassword(newPassword)
                    },
                    placeholder = "Enter password",
                    icon = Icons.Filled.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    onDone = { keyboardFocusManager.clearFocus() }
                )
                NourishTextField(
                    value = uiState.signupPasswordConfirmation,
                    onValueChange = { newPassword ->
                        authenticationViewModel.setSignupPasswordConfirmation(
                            newPassword
                        )
                    },
                    placeholder = "Confirm password",
                    icon = Icons.Filled.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    onDone = { keyboardFocusManager.clearFocus() }
                )

                if (uiState.signupError != "") {
                    NourishErrorText(uiState.signupError)
                }

                NourishButton(
                    onClick = {
                        keyboardFocusManager.clearFocus()
                        authenticationViewModel.signUpWithEmailAndPassword()
                    },
                    text = "Sign up"
                )
            }
        },
        iconSection = {
            Image(
                painter = painterResource(R.drawable.nourish_icon),
                contentDescription = "Nourish Logo",
                modifier = Modifier.size(112.dp).offset(y = (-70).dp)
            )
        }
    )
}