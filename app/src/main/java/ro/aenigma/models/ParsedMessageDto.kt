package ro.aenigma.models

import java.time.ZonedDateTime

class ParsedMessageDto (
    val chatId: String,
    val content: String,
    val dateReceivedOnServer: ZonedDateTime? = null,
    val uuid: String? = null
)
