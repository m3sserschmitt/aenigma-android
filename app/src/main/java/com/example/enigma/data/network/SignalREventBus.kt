package com.example.enigma.data.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SignalREventBus private constructor() {

    private val _events = MutableSharedFlow<MessageReceivedEvent>()

    val events = _events.asSharedFlow()

    suspend fun invokeEvent(event: MessageReceivedEvent): Any = _events.emit(event)

    private object Holder { val INSTANCE = SignalREventBus() }

    companion object {
        @JvmStatic
        fun getInstance(): SignalREventBus {
            return Holder.INSTANCE
        }
    }
}
