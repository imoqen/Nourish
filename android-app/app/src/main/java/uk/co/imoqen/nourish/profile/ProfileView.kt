package uk.co.imoqen.nourish.profile

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import uk.co.imoqen.nourish.common.NourishBottomCard
import uk.co.imoqen.nourish.common.NourishButton
import uk.co.imoqen.nourish.common.NourishDangerButton
import uk.co.imoqen.nourish.common.NourishDivider
import uk.co.imoqen.nourish.common.NourishErrorText
import uk.co.imoqen.nourish.common.NourishH1
import uk.co.imoqen.nourish.common.NourishH2
import uk.co.imoqen.nourish.common.NourishText
import uk.co.imoqen.nourish.common.NourishTextField

/**
 * View for Profile, where the user can log out, delete their account and (if their account is
 * password-provider), change their password.
 * @param profileViewModel - the view model holding methods and state for use in the view.
 *
 * @author Imogen Pemberton, 2026
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val email = profileViewModel.getEmail()

    val keyboardFocusManager =
        LocalFocusManager.current

    val passwordChangeSheetState = rememberModalBottomSheetState()

    NourishBottomCard(
        cardContents = {

            NourishH1("Your Profile")
            if (email != null) {
                NourishText("Signed in with email:")
                NourishH2(email)
            }

            NourishDivider()

            // Sign out:
            Button(
                onClick = {
                    profileViewModel.signOut()
                }
            ) {
                Text("Sign out")
            }

            // Ensures password reset is only possible for username/password accounts
            // (not Google accounts)
            if (uiState.hasPasswordProvider) {
                Button(
                    onClick = {
                        profileViewModel.setShowPasswordChangeBottomSheet(true)
                    }
                ) {
                    Text("Change password")
                }

                if (uiState.showPasswordChangeBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            profileViewModel.setShowPasswordChangeBottomSheet(false)
                        },
                        sheetState = passwordChangeSheetState
                    ) {
                        // Bottom sheet for password change:
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Old password, for reauthentication
                            NourishTextField(
                                value = uiState.oldPassword,
                                onValueChange = { password ->
                                    profileViewModel.setOldPassword(password)
                                },
                                placeholder = "Enter existing password",
                                leadingIcon = Icons.Filled.Lock,
                                onDone = {

                                },
                                visualTransformation = PasswordVisualTransformation()
                            )
                            NourishTextField(
                                value = uiState.newPassword,
                                onValueChange = { updatedPassword ->
                                    profileViewModel.setNewPassword(updatedPassword)
                                },
                                placeholder = "Enter a new password",
                                leadingIcon = Icons.Filled.Lock,
                                onDone = {
                                    keyboardFocusManager.clearFocus(force = true)
                                },
                                visualTransformation = PasswordVisualTransformation()
                            )

                            NourishButton(
                                onClick = {
                                    profileViewModel.updatePassword(
                                        onSuccess = {
                                            profileViewModel.setShowPasswordChangeBottomSheet(false)
                                        }
                                    )
                                },
                                text = "Update Password"
                            )

                            if (uiState.passwordChangeStatus != "") {
                                NourishErrorText(uiState.passwordChangeStatus)
                            }
                        }
                    }
                }
            }

            NourishDivider()

            val deleteAccountSheetState = rememberModalBottomSheetState()

            NourishDangerButton(
                onClick = {
                    profileViewModel.setShowDeleteAccountBottomSheet(true)
                },
                "Delete Account"
            )

            if (uiState.showDeleteAccountBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        profileViewModel.setShowDeleteAccountBottomSheet(false)
                    },
                    sheetState = deleteAccountSheetState,
                    modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)
                ) {
                    val keyboardFocusManager = LocalFocusManager.current
                    // Bottom sheet for account deletion:
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NourishH2("Are you sure?")
                        NourishText("This will delete all data associated with your account, and cannot be undone.\n")

                        // Case: password-provider account
                        if (uiState.hasPasswordProvider) {
                            NourishText("To confirm, type your password below.")
                            // Password for reauthentication
                            NourishTextField(
                                value = uiState.deleteAccountPassword,
                                onValueChange = { password ->
                                    profileViewModel.setDeleteAccountPassword(password)
                                },
                                placeholder = "Enter your password",
                                leadingIcon = Icons.Filled.Lock,
                                visualTransformation = PasswordVisualTransformation(),
                                onDone = {
                                    keyboardFocusManager.clearFocus(force = true)
                                }
                            )
                            NourishDangerButton(
                                onClick = {
                                    profileViewModel.deletePasswordAccount()
                                },
                                text = "Confirm and Delete"
                            )
                        }

                        // Case: Google-provider account
                        else {
                            NourishText("Confirm by signing back in to Google")
                            val activity = LocalActivity.current

                            NourishDangerButton(
                                onClick = {
                                    profileViewModel.deleteGoogleAccount(activity)
                                },
                                text = "Confirm and Delete"
                            )
                        }
                        if (uiState.deleteStatus != "") {
                            NourishErrorText(uiState.deleteStatus)
                        }
                    }
                }
            }
        }
    )

}