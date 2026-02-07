package uk.co.imoqen.nourish.profile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.imoqen.nourish.authentication.AuthenticationState
import uk.co.imoqen.nourish.authentication.AuthenticationUiState

class ProfileViewModel {

    private val mutableUiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = mutableUiState.asStateFlow()



}