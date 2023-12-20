package com.example.enigma.data.network

open class SignalRStatus(val previous: SignalRStatus?, val error: String?) {

    class NotConnected():
        SignalRStatus(null, null)

    class Connecting(previous: SignalRStatus, error: String?):
        SignalRStatus(previous, error)

    class Connected(previous: SignalRStatus, error: String?):
        SignalRStatus(previous, error)

    class Authenticating(previous: SignalRStatus, error: String?):
        SignalRStatus(previous, error)

    class Authenticated(previous: SignalRStatus, error: String?):
        SignalRStatus(previous, error)

    class Disconnected(previous: SignalRStatus, error: String?):
        SignalRStatus(previous, error)

    class Error(previous: SignalRStatus, error: String?):
        SignalRStatus(previous, error)
}
