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

package ro.aenigma.services

open class ClientAction(val value: Int = 0) {
    companion object {
        @JvmStatic
        fun connectPullCleanup(): ClientAction {
            return ClientAction((Connect and Pull and Cleanup).value)
        }
    }

    object Connect : ClientAction(1)
    object Pull : ClientAction(2)
    object Cleanup : ClientAction(4)
    object Disconnect : ClientAction(8)

    infix fun and(action: ClientAction): ClientAction {
        return ClientAction(action.value or value)
    }

    infix fun contains(action: ClientAction): Boolean {
        return (action.value and value) != 0
    }
}
