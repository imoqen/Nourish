package uk.co.imoqen.nourish.common.uievent

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implements the methods defined in UiEventBus.
 * This provides a way to trigger a Snackbar display for messages / notifications, from anywhere
 * in the application.
 *
 * @author Imogen Pemberton, 2026
 */
@Singleton
class UiEventBusImpl @Inject constructor(): UiEventBus {
    val mutableUiEvents = MutableSharedFlow<UiEvent>()
    override val uiEvents = mutableUiEvents.asSharedFlow()

    override suspend fun enqueueEvent(event: UiEvent) {
        mutableUiEvents.emit(event)
    }
}