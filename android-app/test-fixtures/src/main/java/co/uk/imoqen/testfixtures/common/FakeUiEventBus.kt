package co.uk.imoqen.testfixtures.common

import kotlinx.coroutines.flow.MutableSharedFlow
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus

class FakeUiEventBus(): UiEventBus {

    // not used in the tests, simply present to fulfil the interface contract
    override val uiEvents = MutableSharedFlow<UiEvent>()
    val events = mutableListOf<UiEvent>() // used for testing purposes.

    override suspend fun enqueueEvent(event: UiEvent) {
        events.add(event)
    }
}