package ro.aenigma.models.factories

import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.models.GroupMember

object GroupMemberFactory {
    @JvmStatic
    fun create(name: String, publicKey: String): GroupMember {
        return GroupMember(
            name = name,
            publicKey = publicKey,
            address = publicKey.getAddressFromPublicKey()
        )
    }
}
