package ro.aenigma.models

import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime

data class Artifact(
    val text: String? = null,
    val type: MessageType? = null,
    val senderName: String? = null,
    val resourceUrl: String? = null,
    val passphrase: String? = null,
    val guardAddress: String? = null,
    val guardHostname: String? = null,
    val senderAddress: String? = null,
    val refId: String? = null,
    val actionFor: String? = null,
    val chatId: String? = null,
    val dateTimeCreated: ZonedDateTime? = ZonedDateTime.now()
)
