package ro.aenigma.models

data class OnionDetails(
    val text: String?,
    val action: MessageAction?,
    val address: String?,
    val guardAddress: String?,
    val guardHostname: String?,
    val publicKey: String?,
    val refId: String?
)
