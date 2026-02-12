package uk.co.imoqen.nourish.common

import android.widget.Toast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object UiEventBus {
    val mutableUiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = mutableUiEvent.asSharedFlow()

    suspend fun addEvent(event: UiEvent) {
        mutableUiEvent.emit(event)
    }
}