package co.uk.imoqen.testfixtures.common

import kotlinx.coroutines.flow.MutableSharedFlow
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus

/**
 * Mock UiEventBus
 */
class FakeUiEventBus(): UiEventBus {

    // Not used in the tests, simply present to fulfil the interface contract
    override val uiEvents = MutableSharedFlow<UiEvent>()
    // Used for testing purposes.
    val events = mutableListOf<UiEvent>()

    override suspend fun enqueueEvent(event: UiEvent) {
        events.add(event)
    }
}