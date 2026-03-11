package ro.aenigma.models.factories

import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.models.ExportedContactDataDto

object ExportedContactDataFactory {
    @JvmStatic
    fun create(
        name: String?,
        publicKey: String?,
        guardAddress: String?,
        guardHostname: String?
    ): ExportedContactDataDto {
        return ExportedContactDataDto(
            name = name,
            publicKey = publicKey,
            address = publicKey.getAddressFromPublicKey(),
            guardHostname = guardHostname,
            guardAddress = guardAddress
        )
    }

    @JvmStatic
    fun create(address: String): ExportedContactDataDto {
        return ExportedContactDataDto(
            address = address,
            name = null,
            publicKey = null,
            guardHostname = null,
            guardAddress = null
        )
    }
}
