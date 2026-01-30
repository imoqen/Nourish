package uk.co.imoqen.nourish.authentication

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthenticationViewModel: ViewModel() {

    // Purpose: pattern to encapsulate the mutable state to ensure it is not mutable from the viewmodel.
    // this is what is editable, only within this view model and not within composables.
    // pattern taken from advice in: https://medium.com/@rowaido.game/managing-ui-states-in-jetpack-compose-7eb15e4f6931
    private val mutableUiState = MutableStateFlow(AuthenticationUiState())

    // this is the encapsulated, immutable state made from the mutable state above. this is what
    // is observable from the Authentication composables.
    val uiState: StateFlow<AuthenticationUiState> = mutableUiState.asStateFlow()

    fun setLoggedInUser(firebaseUser: FirebaseUser) {
        mutableUiState.update { currentState ->
            currentState.copy(
                currentUser = firebaseUser
            )
        }
    }
}