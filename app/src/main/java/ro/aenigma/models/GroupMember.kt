package ro.aenigma.models

data class GroupMember(
    val name: String? = null,
    val publicKey: String? = null,
    val address: String? = null
) {
    override fun hashCode(): Int {
        return address.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupMember

        return address == other.address
    }
}
