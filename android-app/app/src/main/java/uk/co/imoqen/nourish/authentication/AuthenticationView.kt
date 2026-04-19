package uk.co.imoqen.nourish.authentication

import uk.co.imoqen.nourish.R
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import uk.co.imoqen.nourish.common.ErrorMessage
import uk.co.imoqen.nourish.common.GoogleButton
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
 *
 * @author Imogen Pemberton, 2026
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

    Box {

        // Cycles between background images via Crossfade. All images were taken by myself,
        // of home cooked meals.
        var imageIndex by remember { mutableIntStateOf(0) }
        val images = listOf(
            R.drawable.nourish_salad,
            R.drawable.nourish_sushi,
            R.drawable.nourish_chickpea,
            R.drawable.nourish_pasta,
            R.drawable.nourish_sweetpotato,
            R.drawable.nourish_curry,
            )
        LaunchedEffect(Unit) {
            while(true) {
                delay(5000)
                imageIndex = if (imageIndex == 5) 0 else imageIndex + 1
            }
        }
        Crossfade(
            targetState = imageIndex,
            animationSpec = tween(
                durationMillis = 1000,
                delayMillis = 0,
                easing = FastOutSlowInEasing
            )
        ) { targetState ->
            Image(
                painterResource(images[targetState]),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().padding(0.dp),
            )
        }

        NourishBottomCard(
            titleSection = {
                // TITLE:
                Image(
                    painter = painterResource(R.drawable.nourish_title_outlined),
                    contentDescription = "Nourish Title Text",
                    modifier = Modifier.height(100.dp).padding(16.dp),
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
                        leadingIcon = Icons.Filled.MailOutline,
                        onDone = { keyboardFocusManager.clearFocus() }
                    )
                    // Password:
                    NourishTextField(
                        value = uiState.signinPassword,
                        onValueChange = { newPassword ->
                            authenticationViewModel.setSigninPassword(newPassword)
                        },
                        placeholder = "Enter password",
                        leadingIcon = Icons.Filled.Lock,
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
                            val keyboardFocusManager = LocalFocusManager.current // local to modal bottom sheet
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
                                    leadingIcon = Icons.Filled.Email,
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
                        leadingIcon = Icons.Filled.MailOutline,
                        onDone = { keyboardFocusManager.clearFocus() }
                    )

                    // Password:
                    NourishTextField(
                        value = uiState.signupPassword,
                        onValueChange = { newPassword ->
                            authenticationViewModel.setSignupPassword(newPassword)
                        },
                        placeholder = "Enter password",
                        leadingIcon = Icons.Filled.Lock,
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
                        leadingIcon = Icons.Filled.Lock,
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
}