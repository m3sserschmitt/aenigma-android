package ro.aenigma.models

import java.time.ZonedDateTime

class Message (
    val chatId: String,
    val text: String,
    val incoming: Boolean = true,
    val dateReceivedOnServer: ZonedDateTime? = null,
    val uuid: String? = null
)
