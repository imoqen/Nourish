package uk.co.imoqen.nourish.authentication

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uk.co.imoqen.nourish.ui.theme.NourishTheme

@Composable
fun LoginScreen(
    authenticationViewModel: AuthenticationViewModel
) {

    // the ui state from the view model:
    val uiState by authenticationViewModel.uiState.collectAsState()

    // stores the current logged in user, method provided by firebase:
    val context = LocalContext.current
    val authenticationManager = remember {
        AuthenticationManager(context)
    }
    // ensure that if the user is logged in, it is stored in the view model. if not logged in, will just be null.:
    authenticationViewModel.setLoggedInUser(authenticationManager.getCurrentUser())

    val coroutineScope = rememberCoroutineScope()


    Column(
        modifier = Modifier.fillMaxSize()
    ) {



        if(uiState.currentUser != null) {
            Text("Logged in with email: ${uiState.currentUser?.email}")
            Button(
                onClick = {
                    authenticationManager.signOutUser() // sign out in firebase
                    authenticationViewModel.setLoggedInUser(null) // sign out locally by changing the auth current user state via the method
                }
            ) {
                Text("Sign out")
            }
        } else {

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
                    authenticationViewModel.setEmail(newPassword)
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
                    authenticationManager.signInWithEmailAndPassword(uiState.email, uiState.password)
                        .onEach { response ->
                            if (response is AuthenticationResponse.Success) {
                                Log.i("login", "login success username password")
                                // redetermine the logged in user to trigger recomposition:
                                authenticationViewModel.setLoggedInUser(authenticationManager.getCurrentUser())
                            } else {
                                Log.i("login", "login error username password")
                            }
                        }
                        .launchIn(coroutineScope) // have to launch in coroutine scope or else wont return anything
                }
            ) {
                Text("Sign in")
            }

            Button(
                onClick = {
                    authenticationManager.signInWithGoogle()
                        .onEach { response ->
                            if (response is AuthenticationResponse.Success) {
                                Log.i("login", "login success google")
                                // redetermine the logged in user to trigger recomposition:
                                authenticationViewModel.setLoggedInUser(authenticationManager.getCurrentUser())
                            } else {
                                Log.i("login", "login error google")
                            }
                        }
                        .launchIn(coroutineScope) // have to launch in coroutine scope or else wont return anything
                }
            ) {
                Text("Sign in with Google")
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