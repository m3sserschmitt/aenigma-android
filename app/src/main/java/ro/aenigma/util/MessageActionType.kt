package ro.aenigma.util

enum class MessageActionType {
    @Description("Text message")
    TEXT,

    @Description("Message deleted")
    DELETE,

    @Description("Conversation deleted")
    DELETE_ALL,

    @Description("Replied to message")
    REPLY
}

inline fun <reified T : Enum<T>>T?.getDescription(): String? {
    if (this == null) {
        return null
    }
    return try {
        return this::class.java
            .getField(this.name)
            .getAnnotation(Description::class.java)
            ?.value
    } catch (ex: Exception) {
        null
    }
}
