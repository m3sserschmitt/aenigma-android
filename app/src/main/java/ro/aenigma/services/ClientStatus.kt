package ro.aenigma.services

sealed class ClientStatus(
    val error: String?,
    private val level: Int
) {
    infix fun smallerThan(status: ClientStatus): Boolean {
        return level < status.level
    }

    infix fun greaterOrEqualThan(status: ClientStatus): Boolean {
        return this greaterThan status || level == status.level
    }

    infix fun greaterThan(status: ClientStatus): Boolean {
        return level > status.level
    }

    object NotConnected : ClientStatus(null, 0)

    object DisconnectedByClient : ClientStatus(null, 0)

    object Connecting : ClientStatus(null, 1)

    object Connected : ClientStatus(null, 2)

    object Authenticating : ClientStatus(null, 3)

    object Authenticated : ClientStatus(null, 4)

    object Pulling : ClientStatus(null, 5)

    object Cleaning : ClientStatus(null, 5)

    object Synchronized : ClientStatus(null, 6)

    object Clean : ClientStatus(null, 6)

    open class Error(status: ClientStatus?, error: String? = null) :
        ClientStatus(error = error, level = (status ?: NotConnected).level) {
        class ConnectionRefused(error: String? = null) : Error(status = NotConnected, error = error)

        class Disconnected(error: String? = null) :
            Error(status = NotConnected, error = error)

        object Aborted : ClientStatus(error = null, level = Int.MIN_VALUE)
    }
}
