package ro.aenigma.models

import ro.aenigma.crypto.PublicKeyExtensions.getAddressFromPublicKey

class GroupMember(
    val name: String? = null,
    val publicKey: String? = null
) {
    override fun hashCode(): Int {
        return publicKey.getAddressFromPublicKey().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupMember

        return publicKey.getAddressFromPublicKey() == other.publicKey.getAddressFromPublicKey()
    }
}
