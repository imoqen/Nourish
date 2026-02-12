package uk.co.imoqen.nourish.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Allows for a single UiEvent flow to be shared across the app, events can be added (emitted) using enqueue.
 * adapted from https://medium.com/@sharmapraveen91/eventbus-using-kotlin-flow-ef3c47b18643
 */

@Singleton
class UiEventBus @Inject constructor() {
    val mutableUiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = mutableUiEvents.asSharedFlow()

    suspend fun enqueueEvent(event: UiEvent) {
        mutableUiEvents.emit(event)
    }
}