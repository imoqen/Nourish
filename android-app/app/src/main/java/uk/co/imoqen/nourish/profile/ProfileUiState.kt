package uk.co.imoqen.nourish.profile

data class ProfileUiState (
    // password provider, for determining if password reset should be displayed:
    val hasPasswordProvider: Boolean = false,

    // password change section:
    val showPasswordChangeBottomSheet: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val passwordChangeStatus: String = "",

    // account deletion section:
    val showDeleteAccountBottomSheet: Boolean = false,
    val deleteAccountPassword: String = "",
    val deleteStatus: String = ""
)