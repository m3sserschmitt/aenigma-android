package com.example.enigma.workers

open class SignalRWorkerAction(val value: Int = 0) {
    companion object
    {
        @JvmStatic
        fun connectPullCleanup(): SignalRWorkerAction
        {
            return SignalRWorkerAction((Connect() and Pull() and Cleanup()).value)
        }
    }

    class Connect: SignalRWorkerAction(1)
    class Pull: SignalRWorkerAction(2)
    class Cleanup: SignalRWorkerAction(4)
    class Broadcast: SignalRWorkerAction(8)
    class Disconnect: SignalRWorkerAction(16)

    infix fun and(action: SignalRWorkerAction): SignalRWorkerAction
    {
        return SignalRWorkerAction(action.value or value)
    }

    infix fun contains(action: SignalRWorkerAction): Boolean
    {
        return action.value and value != 0
    }
}
