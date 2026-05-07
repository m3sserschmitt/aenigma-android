package ro.aenigma.models.extensions

import ro.aenigma.models.enums.MessageType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object MessageTypeExtensions {
    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun MessageType?.isGroupCreateOrUpdate(): Boolean {
        contract {
            returns(true) implies (this@isGroupCreateOrUpdate != null)
        }
        return isGroupCreate() || isGroupUpdate()
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun MessageType?.isGroupUpdate(): Boolean {
        contract {
            returns(true) implies (this@isGroupUpdate != null)
        }
        return this in listOf(
            MessageType.GROUP_RENAMED,
            MessageType.GROUP_MEMBER_ADD,
            MessageType.GROUP_MEMBER_REMOVE
        )
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun MessageType?.isGroupCreate(): Boolean {
        contract {
            returns(true) implies (this@isGroupCreate != null)
        }
        return this == MessageType.GROUP_CREATE
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun MessageType?.isGroupMemberLeave(): Boolean {
        contract {
            returns(true) implies (this@isGroupMemberLeave != null)
        }
        return this == MessageType.GROUP_MEMBER_LEAVE
    }
}
