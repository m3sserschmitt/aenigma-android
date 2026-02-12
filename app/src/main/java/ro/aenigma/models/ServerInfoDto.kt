package ro.aenigma.models

data class ServerInfoDto (
    val address: String? = null,
    val graphVersion: String? = null,
    val onionService: String? = null,
    val hostname: String? = null
)
