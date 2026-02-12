package ro.aenigma.models

data class GuardDto(
    val address: String,
    val publicKey: String,
    val hostname: String?,
    val onionService: String?,
    val graphVersion: String?,
)
