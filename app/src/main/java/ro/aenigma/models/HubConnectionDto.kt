package ro.aenigma.models

import com.microsoft.signalr.HubConnection
import kotlinx.coroutines.flow.MutableStateFlow

data class HubConnectionDto(
    val connection: HubConnection? = null,
    val closedByClient: MutableStateFlow<Boolean> = MutableStateFlow(false)
)
