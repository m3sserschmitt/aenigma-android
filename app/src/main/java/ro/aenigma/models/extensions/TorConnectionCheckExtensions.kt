package ro.aenigma.models.extensions

import ro.aenigma.models.enums.TorConnectionCheck

object TorConnectionCheckExtensions {
    @JvmStatic
    fun TorConnectionCheck.isFailed(): Boolean {
        return this == TorConnectionCheck.FAILED
    }

    @JvmStatic
    fun TorConnectionCheck.isOk(): Boolean {
        return this == TorConnectionCheck.OK
    }
}
