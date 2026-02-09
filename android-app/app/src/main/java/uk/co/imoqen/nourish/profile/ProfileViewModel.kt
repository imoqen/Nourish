package uk.co.imoqen.nourish.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.imoqen.nourish.authentication.AuthenticationManager
import uk.co.imoqen.nourish.authentication.AuthenticationState
import uk.co.imoqen.nourish.authentication.AuthenticationUiState
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authenticationManager: AuthenticationManager
): ViewModel() {

    private val mutableUiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = mutableUiState.asStateFlow()



}