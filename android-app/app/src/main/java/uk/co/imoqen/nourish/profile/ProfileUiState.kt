package uk.co.imoqen.nourish.profile

/**
 * UiState for Profile: accessed in the View/ViewModel and mutated in the ViewModel.
 * This encapsulation allows state to persist and be updated reliably.
 */
data class ProfileUiState (
    // Password provider, for determining if password reset should be displayed:
    val hasPasswordProvider: Boolean = false,

    // Change password section:
    val showPasswordChangeBottomSheet: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val passwordChangeStatus: String = "",

    // Delete account section:
    val showDeleteAccountBottomSheet: Boolean = false,
    val deleteAccountPassword: String = "",
    val deleteStatus: String = ""
)