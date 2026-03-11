package ro.aenigma.models.factories

import ro.aenigma.crypto.extensions.HashExtensions.getSha256Hex
import ro.aenigma.models.ExportedContactDataDto
import ro.aenigma.models.GroupDataDto
import java.util.UUID

object GroupDataFactory {
    @JvmStatic
    fun create(name: String, members: List<ExportedContactDataDto>, admins: List<String>): GroupDataDto {
        return GroupDataDto(
            address = UUID.randomUUID().toString().getSha256Hex(),
            name = name,
            members = members,
            admins = admins,
            nonce = 1
        )
    }
}
