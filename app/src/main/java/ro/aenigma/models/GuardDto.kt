package ro.aenigma.models

import java.time.ZonedDateTime

data class GuardDto(
    val id: Long = 0,
    val address: String,
    val publicKey: String,
    val hostname: String?,
    val onionService: String?,
    val graphVersion: String?,
    val dateCreated: ZonedDateTime = ZonedDateTime.now()
)
