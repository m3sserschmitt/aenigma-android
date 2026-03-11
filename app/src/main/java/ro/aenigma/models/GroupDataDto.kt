package ro.aenigma.models

data class GroupDataDto(
    val address: String? = null,
    val name: String? = null,
    val members: List<ExportedContactDataDto>? = null,
    val admins: List<String?>? = null,
    val nonce: Long? = null
)
