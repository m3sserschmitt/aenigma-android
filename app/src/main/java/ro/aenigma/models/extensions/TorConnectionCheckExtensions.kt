package ro.aenigma.models.extensions

import ro.aenigma.models.enums.TorCircuitState

object TorConnectionCheckExtensions {
    @JvmStatic
    fun TorCircuitState.isOk(): Boolean {
        return this == TorCircuitState.OK
    }
}
