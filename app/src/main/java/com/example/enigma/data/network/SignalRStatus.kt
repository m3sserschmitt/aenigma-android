package com.example.enigma.data.network

abstract class SignalRStatus(
    val previous: SignalRStatus?,
    val error: String?
) {
    class NotConnected:
        SignalRStatus(null, null)

    class Connecting(previous: SignalRStatus):
        SignalRStatus(previous, null)

    class Connected(previous: SignalRStatus):
        SignalRStatus(previous, null)

    class Authenticating(previous: SignalRStatus):
        SignalRStatus(previous, null)

    class Authenticated(previous: SignalRStatus):
        SignalRStatus(previous, null)

    class Disconnected(previous: SignalRStatus, error: String?):
        SignalRStatus(previous, error)

    open class Error(previous: SignalRStatus, error: String?): SignalRStatus(previous, error)
    { class ConnectionRefused(previous: SignalRStatus, error: String?): Error(previous, error) }

    class Aborted(previous: SignalRStatus, error: String?):
        SignalRStatus(previous, error)
}
