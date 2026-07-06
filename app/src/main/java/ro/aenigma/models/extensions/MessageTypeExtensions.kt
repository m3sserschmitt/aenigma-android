/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
