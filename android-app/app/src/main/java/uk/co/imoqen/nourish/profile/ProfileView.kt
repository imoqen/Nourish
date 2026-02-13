package uk.co.imoqen.nourish.profile

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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

/**
 * View for Profile, where the user can log out, delete their account and (if their account is
 * password-provider), change their password.
 * @param profileViewModel - the view model holding methods and state for use in the view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
) {
    Column() {
        Text("Profile")
        val uiState by profileViewModel.uiState.collectAsState()

        val passwordChangeSheetState = rememberModalBottomSheetState()

        // Ensures password reset is only possible for username/password accounts (not Google accounts)
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
                    Column() {
                        // Old password, for reauthentication
                        OutlinedTextField(
                            value = uiState.oldPassword,
                            onValueChange = { password ->
                                profileViewModel.setOldPassword(password)
                            },
                            placeholder = {
                                Text("Enter your account's existing password")
                            },
                            leadingIcon = {
                                Icons.Filled.Lock
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // New password, for password change
                        OutlinedTextField(
                            value = uiState.newPassword,
                            onValueChange = { updatedPassword ->
                                profileViewModel.setNewPassword(updatedPassword)
                            },
                            placeholder = {
                                Text("Enter a new password for your account")
                            },
                            leadingIcon = {
                                Icons.Filled.Lock
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                profileViewModel.updatePassword()
                            }
                        ) {
                            Text("Update Password")
                        }

                        if (uiState.passwordChangeStatus != "") {
                            Text(uiState.passwordChangeStatus)
                        }
                    }
                }
            }
        }

        val deleteAccountSheetState = rememberModalBottomSheetState()

        Button(
            onClick = {
                profileViewModel.setShowDeleteAccountBottomSheet(true)
            }
        ) {
            Text("Delete Account")
        }

        if (uiState.showDeleteAccountBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    profileViewModel.setShowDeleteAccountBottomSheet(false)
                },
                sheetState = deleteAccountSheetState
            ) {
                // Bottom sheet for account deletion:
                Column() {
                    Text("Are you sure you wish to delete your account?")
                    Text("This will delete all data associated with your account. This action cannot be undone.")
                    // Case: password-provider account
                    if (uiState.hasPasswordProvider) {
                        Text("Please type your password below to confirm.")
                        // Password for reauthentication
                        OutlinedTextField(
                            value = uiState.deleteAccountPassword,
                            onValueChange = { password ->
                                profileViewModel.setDeleteAccountPassword(password)
                            },
                            placeholder = {
                                Text("Enter your password")
                            },
                            leadingIcon = {
                                Icons.Filled.Lock
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                profileViewModel.deletePasswordAccount()
                            }
                        ) {
                            Text("Delete Account")
                        }
                    }
                    // Case: Google-provider account
                    else {
                        Text("Confirm by signing back in to google")
                        val activity = LocalActivity.current

                        Button(
                            onClick = {
                                profileViewModel.deleteGoogleAccount(activity)
                            }
                        ) {
                            Text("Delete Account")
                        }
                    }
                    if (uiState.deleteStatus != "") {
                        Text(uiState.deleteStatus)
                    }
                }
            }
        }

        // Sign out:
        Button(
            onClick = {
                profileViewModel.signOut()
            }
        ) {
            Text("Sign out")
        }
    }
}