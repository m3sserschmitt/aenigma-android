package ro.aenigma.models

data class ServerInfo (
    val address: String? = null,
    val graphVersion: String? = null,
    val onionService: String? = null,
    val hostname: String? = null
)
