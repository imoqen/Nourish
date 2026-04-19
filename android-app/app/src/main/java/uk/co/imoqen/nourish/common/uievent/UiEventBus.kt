package uk.co.imoqen.nourish.common.uievent

import kotlinx.coroutines.flow.SharedFlow

/**
 * Allows for a single UiEvent flow to be shared across the app, events can be added (emitted) using enqueue.
 * adapted from https://medium.com/@sharmapraveen91/eventbus-using-kotlin-flow-ef3c47b18643
 *
 * @author Imogen Pemberton, 2026
 */
interface UiEventBus {
    val uiEvents: SharedFlow<UiEvent>

    suspend fun enqueueEvent(event: UiEvent)
}