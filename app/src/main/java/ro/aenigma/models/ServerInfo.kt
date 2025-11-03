package ro.aenigma.models

data class ServerInfo (
    val publicKey: String? = null,
    val address: String? = null,
    val graphVersion: String? = null,
    val onionService: String? = null,
    val hostname: String? = null
)
