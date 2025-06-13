package ro.aenigma.models

data class GroupData(
    val address: String? = null,
    val name: String? = null,
    val members: List<ExportedContactData>? = null,
    val admins: List<String?>? = null,
    val nonce: Long? = null
)
