package ro.aenigma.models

import java.time.ZonedDateTime

data class ParsedMessageDto (
    val chatId: String? = null,
    val content: String? = null,
    val dateReceivedOnServer: ZonedDateTime? = null,
    val uuid: String? = null
)
