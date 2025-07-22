package ro.aenigma.models

import kotlinx.coroutines.flow.MutableStateFlow
import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime

data class MessageDto(
    val id: Long,
    val chatId: String,
    val senderAddress: String?,
    val serverUUID: String?,
    val text: String?,
    val type: MessageType?,
    val actionFor: String?,
    val refId: String?,
    val incoming: Boolean,
    val sent: Boolean,
    val deleted: Boolean,
    val date: ZonedDateTime,
    val dateReceivedOnServer: ZonedDateTime?,
    val files: List<String>?,
    val deliveryStatus: MutableStateFlow<Boolean?> = MutableStateFlow(null)
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageDto) return false
        return id == other.id
    }
}
