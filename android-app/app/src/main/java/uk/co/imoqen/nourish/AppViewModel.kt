package uk.co.imoqen.nourish

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uk.co.imoqen.nourish.firebase.authentication.AuthenticationManager
import javax.inject.Inject

/**
 * View model accessible across the entire app, for holding state such as the login state.
 * @param authenticationManager - dependent on the AuthenticationManager
 *
 * @author Imogen Pemberton, 2026
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager
): ViewModel() {

    // Authentication state is accessible anywhere across the app:
    val authenticationState = authenticationManager.authenticationState

}