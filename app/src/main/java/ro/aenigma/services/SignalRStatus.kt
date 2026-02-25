package ro.aenigma.services

sealed class SignalRStatus(
    val error: String?,
    private val level: Int
) {
    infix fun smallerThan(status: SignalRStatus): Boolean {
        return level < status.level
    }

    infix fun greaterOrEqualThan(status: SignalRStatus): Boolean {
        return this greaterThan status || level == status.level
    }

    infix fun greaterThan(status: SignalRStatus): Boolean {
        return level > status.level
    }

    object NotConnected : SignalRStatus(null, 0)

    object Connecting : SignalRStatus(null, 1)

    object Connected : SignalRStatus(null, 2)

    object Authenticating : SignalRStatus(null, 3)

    object Authenticated : SignalRStatus(null, 4)

    object Pulling : SignalRStatus(null, 5)

    object Cleaning : SignalRStatus(null, 5)

    object Synchronized : SignalRStatus(null, 6)

    object Clean : SignalRStatus(null, 6)

    open class Error(status: SignalRStatus?, error: String? = null) :
        SignalRStatus(error = error, level = (status ?: NotConnected).level) {
        class ConnectionRefused(error: String? = null) : Error(status = NotConnected, error = error)

        class Disconnected(error: String? = null) :
            Error(status = NotConnected, error = error)

        class Aborted(error: String? = null) : SignalRStatus(error = error, level = Int.MIN_VALUE)
    }
}
