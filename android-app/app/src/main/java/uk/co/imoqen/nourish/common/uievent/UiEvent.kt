package uk.co.imoqen.nourish.common.uievent

/**
 * onEvent is handled in main activity so viewable across the app
 * Based on https://medium.com/@hunterfreas/handling-ui-events-in-jetpack-compose-a-clean-approach-c8fd1bfc6231
 */

sealed interface UiEvent {
    data class ShowSnackbar(val message: String): UiEvent
}