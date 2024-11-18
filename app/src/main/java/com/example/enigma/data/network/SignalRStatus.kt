package com.example.enigma.data.network

abstract class SignalRStatus(
    val error: String?
) {
    class NotConnected: SignalRStatus(null)

    class Connecting: SignalRStatus(null)

    class Connected: SignalRStatus(null)

    class Authenticating: SignalRStatus(null)

    class Authenticated: SignalRStatus(null)

    open class Error(error: String?): SignalRStatus(error)
    {
        class ConnectionRefused(error: String?): Error(error)

        class Disconnected(error: String?): SignalRStatus(error)

        class Aborted(error: String?): SignalRStatus(error)
    }
}
