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
    signOut: () -> Unit // sign out method
) {

    Column() {

        Text("Profile Screen")

        val profileViewModel: ProfileViewModel = hiltViewModel()
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
                                profileViewModel.setOldPassword(updatedPassword)
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
        var showDeleteAccountBottomSheet by remember { mutableStateOf(false) }
        var deleteAccountPassword by remember { mutableStateOf("") }
        var deleteStatus by remember { mutableStateOf("") }


        Button(
            onClick = {
                showDeleteAccountBottomSheet = true
            }
        ) {
            Text("Delete Account")
        }

        if (showDeleteAccountBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showDeleteAccountBottomSheet = false
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
                            value = deleteAccountPassword,
                            onValueChange = { password ->
                                deleteAccountPassword = password
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
                                if (deleteAccountPassword == "") {
                                    deleteStatus = "The password must not be blank."
                                    return@Button // early return
                                }
                                val credentials = EmailAuthProvider.getCredential(
                                    Firebase.auth.currentUser!!.email!!,
                                    deleteAccountPassword
                                )
                                Firebase.auth.currentUser!!.reauthenticate(credentials)
                                    .addOnSuccessListener {
                                        // delete all of the user data from firestore:
                                        scope.launch() {
                                            //TODO Uncomment:
                                            //deleteAllUserData(Firebase.auth.currentUser!!)
                                        }
                                        // then delete the user:
                                        Firebase.auth.currentUser!!.delete().addOnSuccessListener {
                                            signOut() // sign them out as the account is now deleted

                                        }
                                        // now can change the password as user authenticated successfully:
                                        //deleteStatus = "Account deleted successfully."
                                    }
                                    .addOnFailureListener { error ->

                                        deleteStatus =
                                            "Invalid password. Account could not be deleted."

                                    }
                            }
                        ) {
                            Text("Delete Account")
                        }
                    }
                    else {
                        Text("Confirm by signing back in to google")
                        val context = LocalContext.current
                        val credentialManager = CredentialManager.create(context)
                        val scope = rememberCoroutineScope()
                        val activity = LocalActivity.current

                        Button(
                            onClick = {
                                scope.launch() {
                                    // https://stackoverflow.com/questions/40898952/how-to-re-authenticate-a-user-on-firebase-with-google-provider
                                    val provider = OAuthProvider.newBuilder("google.com").build()

                                    if (activity != null) {
                                        Firebase.auth.currentUser!!.startActivityForReauthenticateWithProvider(
                                            activity,
                                            provider
                                        )
                                            .addOnSuccessListener {
                                                // delete all of the user data from firestore:
                                                scope.launch() {
                                                    //TODO Uncomment:
                                                    //deleteAllUserData(Firebase.auth.currentUser!!)
                                                }
                                                // then delete the user:
                                                Firebase.auth.currentUser!!.delete().addOnSuccessListener {
                                                    signOut() // sign them out as the account is now deleted
                                                } }
                                            .addOnFailureListener { e -> deleteStatus = "error: $e" }
                                    }

                                }
                            }
                        ) {
                            Text("Delete Account")
                        }

                    }

                    if (deleteStatus != "") {
                        Text(deleteStatus)
                    }

                    Button(onClick = {
                        scope.launch { deleteAccountBottomSheetState.hide() }.invokeOnCompletion {
                            if (!deleteAccountBottomSheetState.isVisible) {
                                showDeleteAccountBottomSheet = false
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
                signOut() // sign out locally by changing the auth current user state via the method
                Log.i("signoutreq", "signing out")
            }
        ) {
            Text("Sign out")
        }
    }
}