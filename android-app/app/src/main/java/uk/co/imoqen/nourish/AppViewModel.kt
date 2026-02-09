package uk.co.imoqen.nourish

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uk.co.imoqen.nourish.authentication.AuthenticationManager
import javax.inject.Inject

// view model holding state / methods used across the entire app
// such as the logins tate

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager
): ViewModel() {

    val authenticationState = authenticationManager.authenticationState

}