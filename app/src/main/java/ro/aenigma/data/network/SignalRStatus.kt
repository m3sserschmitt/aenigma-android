package ro.aenigma.data.network

abstract class SignalRStatus(
    val error: String?,
    private val level: Int
) {
    infix fun smallerOrEqualThan(status: SignalRStatus): Boolean
    {
        return this smallerThan status || level == status.level
    }

    infix fun smallerThan(status: SignalRStatus): Boolean
    {
        return level < status.level
    }

    infix fun greaterOrEqualThan(status: SignalRStatus): Boolean
    {
        return this greaterThan status || level == status.level
    }

    infix fun greaterThan(status: SignalRStatus): Boolean
    {
        return level > status.level
    }

    class NotConnected: SignalRStatus(null, 0)

    class Connecting: SignalRStatus(null, 1)

    class Connected: SignalRStatus(null, 2)

    class Authenticating: SignalRStatus(null, 3)

    class Authenticated: SignalRStatus(null, 4)

    class Pulling: SignalRStatus(null, 5)

    class Cleaning: SignalRStatus(null, 5)

    class Broadcasting: SignalRStatus(null, 5)

    class Synchronized: SignalRStatus(null, 6)

    class Clean: SignalRStatus(null, 6)

    class Broadcasted: SignalRStatus(null, 6)

    class Reset(status: SignalRStatus?): SignalRStatus(null, status?.level ?: 0)

    open class Error(status: SignalRStatus?, error: String? = null): SignalRStatus(error, status?.level ?: 0)
    {
        class ConnectionRefused(error: String? = null): Error(Connecting(), error)

        class Disconnected(status: SignalRStatus?, error: String? = null): Error(status, error)

        class Aborted(error: String? = null): SignalRStatus(error, Int.MAX_VALUE)
    }
}
