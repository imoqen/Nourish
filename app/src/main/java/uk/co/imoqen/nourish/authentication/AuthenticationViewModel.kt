package uk.co.imoqen.nourish.authentication

import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import uk.co.imoqen.nourish.R
import java.security.MessageDigest
import java.util.UUID

class AuthenticationViewModel: ViewModel() {

    private val auth = Firebase.auth

    // Purpose: pattern to encapsulate the mutable state to ensure it is not mutable from the viewmodel.
    // this is what is editable, only within this view model and not within composables.
    // pattern taken from advice in: https://medium.com/@rowaido.game/managing-ui-states-in-jetpack-compose-7eb15e4f6931
    private val mutableUiState = MutableStateFlow(AuthenticationUiState())

    // this is the encapsulated, immutable state made from the mutable state above. this is what
    // is observable from the Authentication composables.
    val uiState: StateFlow<AuthenticationUiState> = mutableUiState.asStateFlow()

    fun setLoggedInUser(firebaseUser: FirebaseUser?) {
        mutableUiState.update { currentState ->
            currentState.copy(
                currentUser = firebaseUser
            )
        }
    }

    fun setEmail(email: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                email = email
            )
        }
    }

    fun setPassword(password: String) {
        mutableUiState.update { currentState ->
            currentState.copy(
                password = password
            )
        }
    }

    fun signOut() {
        mutableUiState.update { currentState ->
            currentState.copy(
                currentUser = null // simply set the current user to be null
            )
        }
        // then sign out on firebase too:
        if (Firebase.auth.currentUser != null) {
            Firebase.auth.signOut()
        }
        Log.i("logout", "current firebase user: ${Firebase.auth.currentUser}, current local user: ${uiState.value.currentUser}")
    }
}