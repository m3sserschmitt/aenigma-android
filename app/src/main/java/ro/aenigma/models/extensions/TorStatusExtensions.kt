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
