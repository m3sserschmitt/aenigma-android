package ro.aenigma.models

class PendingMessage(
    val uuid: String?,
    val destination: String?,
    val content: String?,
    val dateReceived: String?,
    val sent: Boolean?
)
