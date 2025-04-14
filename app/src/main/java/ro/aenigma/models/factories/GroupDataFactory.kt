package ro.aenigma.models.factories

import ro.aenigma.crypto.extensions.HashExtensions.getSha256Hex
import ro.aenigma.models.GroupData
import ro.aenigma.models.GroupMember
import java.util.UUID

object GroupDataFactory {
    @JvmStatic
    fun create(name: String, members: List<GroupMember>, admins: List<String>): GroupData {
        return GroupData(
            address = UUID.randomUUID().toString().getSha256Hex(),
            name = name,
            members = members,
            admins = admins
        )
    }
}
