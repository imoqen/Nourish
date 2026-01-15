package uk.co.imoqen.nourish.authentication

import android.content.Context
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uk.co.imoqen.nourish.R
import uk.co.imoqen.nourish.ui.theme.NourishTheme
import java.security.MessageDigest
import java.util.UUID

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val authenticationManager = remember {
        AuthenticationManager(context)
    }
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Sign in")
        // email:
        OutlinedTextField(
            value = email,
            onValueChange = { newEmailValue ->
                email = newEmailValue
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
            value = password,
            onValueChange = { newPasswordValue ->
                password = newPasswordValue
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
                authenticationManager.signInWithEmailAndPassword(email,password)
                    .onEach { response ->
                        if (response is AuthenticationResponse.Success) {
                            Log.i("login", "login success username password")
                        } else {
                            Log.i("login", "login error username password")
                        }
                    }.launchIn(coroutineScope) // have to launch in coroutine scope or else wont return anything
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
                        } else {
                            Log.i("login", "login error google")
                        }
                    }.launchIn(coroutineScope) // have to launch in coroutine scope or else wont return anything
            }
        ) {
            Text("Sign in with Google")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    NourishTheme {
        LoginScreen()
    }
}

class AuthenticationManager(val context: Context) {
    private val auth = Firebase.auth

    // adapted from android docs:
    fun createUserWithEmailAndPassword(
        email: String, password: String
    ): Flow<AuthenticationResponse> = callbackFlow {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // user created successfully
                    trySend(AuthenticationResponse.Success)
                } else {
                    // user not created successfully
                    trySend(
                        AuthenticationResponse.Error(errorMessage= task.exception?.message ?: "")
                    )
                }
            }
        awaitClose()
    }

    fun signInWithEmailAndPassword(
        email: String, password: String
    ): Flow<AuthenticationResponse> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthenticationResponse.Success)
                } else {
                    trySend(AuthenticationResponse.Error(errorMessage = task.exception?.message ?: ""))
                }
            }
        awaitClose()
    }

    private fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        // convert to byte array:
        val bytes = rawNonce.toByteArray()
        // encrypt:
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    fun signInWithGoogle(): Flow<AuthenticationResponse> = callbackFlow {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            // gets the default web client id from the resources, generated from details provided in
            // the google-services.json file provided by google cloud:
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false) // wont auto select an account
            .setNonce(generateNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context = context, request = request)

            val credential = result.credential

            if (credential is CustomCredential) {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val firebaseCredential = GoogleAuthProvider.getCredential(
                            googleIdTokenCredential.idToken,
                            null
                        )

                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    trySend(AuthenticationResponse.Success)
                                } else {
                                    Log.i("login error", "${task.exception?.message}")
                                    trySend(AuthenticationResponse.Error(errorMessage = task.exception?.message ?: ""))
                                }
                            }

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.i("login error", "token parsing: ${e.message}")
                        trySend(AuthenticationResponse.Error(errorMessage = e.message ?: ""))
                    }
                }
            }
        } catch (e: Exception) {
            Log.i("login error", "${e.message}")
            trySend(AuthenticationResponse.Error(errorMessage = e.message ?: ""))
        }

        awaitClose()
    }

}

interface AuthenticationResponse {
    data object Success: AuthenticationResponse
    data class Error(val errorMessage: String): AuthenticationResponse
}