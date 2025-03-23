package ro.aenigma.models

data class PendingMessage(
    val uuid: String? = null,
    val destination: String? = null,
    val content: String? = null,
    val dateReceived: String? = null,
    val sent: Boolean? = null
)
