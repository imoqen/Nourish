package uk.co.imoqen.nourish.profile

import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel
) {

    Column() {

        Text("Profile Screen")

        val uiState by profileViewModel.uiState.collectAsState()

        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        // to ensure password reset is only possible for username/password accounts (not Google accounts), to avoid confusion:
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
                    sheetState = sheetState
                ) {
                    // Sheet content
                    Column() {
                        // old password:
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
                        // new password:
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

        val deleteAccountBottomSheetState = rememberModalBottomSheetState()

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
                sheetState = deleteAccountBottomSheetState
            ) {
                // Sheet content
                Column() {
                    Text("Are you sure you wish to delete your account?")
                    Text("This will delete all data associated with your account. This action cannot be undone.")

                    if (uiState.hasPasswordProvider) {
                        Text("Please type your password below to confirm.")
                        // old password:
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

                    Button(onClick = {
                        scope.launch { deleteAccountBottomSheetState.hide() }.invokeOnCompletion {
                            if (!deleteAccountBottomSheetState.isVisible) {
                                profileViewModel.setShowDeleteAccountBottomSheet(false)
                            }
                        }
                    }) {
                        Text("Hide bottom sheet")
                    }
                }
            }
        }



        Button(
            onClick = {
                profileViewModel.signOut() // sign out locally by changing the auth current user state via the method
                Log.i("signoutreq", "signing out")
            }
        ) {
            Text("Sign out")
        }
    }
}