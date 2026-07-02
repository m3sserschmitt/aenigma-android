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

import ro.aenigma.models.enums.TorStatus

object TorStatusExtensions {
    @JvmStatic
    fun TorStatus.torPreferenceIsChanging(torPreference: Boolean): Boolean {
        return (!torPreference && this == TorStatus.ON) || (torPreference && this == TorStatus.OFF)
    }

    @JvmStatic
    fun TorStatus.torPreferenceIsNotChanging(torPreference: Boolean): Boolean {
        return (torPreference && this == TorStatus.ON) || (!torPreference && this == TorStatus.OFF)
    }

    @JvmStatic
    fun TorStatus.with(torPreference: Boolean, action: () -> Unit) {
        if(torPreferenceIsNotChanging(torPreference)) {
            action()
        }
    }

    @JvmStatic
    fun TorStatus.shouldStartTor(torPreference: Boolean): Boolean {
        return torPreference && this == TorStatus.OFF
    }

    @JvmStatic
    fun TorStatus.shouldStopTor(torPreference: Boolean): Boolean {
        return !torPreference && this == TorStatus.ON
    }
}
