package ro.aenigma.services

open class ClientAction(val value: Int = 0) {
    companion object {
        @JvmStatic
        fun connectPullCleanup(): ClientAction {
            return ClientAction((Connect and Pull and Cleanup).value)
        }
    }

    object Connect : ClientAction(1)
    object Pull : ClientAction(2)
    object Cleanup : ClientAction(4)
    object Disconnect : ClientAction(8)

    infix fun and(action: ClientAction): ClientAction {
        return ClientAction(action.value or value)
    }

    infix fun contains(action: ClientAction): Boolean {
        return (action.value and value) != 0
    }
}
