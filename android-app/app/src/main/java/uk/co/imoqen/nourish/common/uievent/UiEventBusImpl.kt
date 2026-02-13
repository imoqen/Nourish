package uk.co.imoqen.nourish.common.uievent

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UiEventBusImpl @Inject constructor(): UiEventBus {
    val mutableUiEvents = MutableSharedFlow<UiEvent>()
    override val uiEvents = mutableUiEvents.asSharedFlow()

    override suspend fun enqueueEvent(event: UiEvent) {
        mutableUiEvents.emit(event)
    }
}