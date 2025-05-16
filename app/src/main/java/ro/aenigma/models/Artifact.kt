package ro.aenigma.models

import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime

open class Artifact(
    val text: String? = null,
    val type: MessageType? = null,
    val senderName: String? = null,
    val groupResourceUrl: String? = null,
    val senderGuardAddress: String? = null,
    val senderGuardHostname: String? = null,
    val chatId: String? = null,
    val refId: String? = null,
    val actionFor: String? = null,
    val dateTimeCreated: ZonedDateTime? = ZonedDateTime.now()
)
