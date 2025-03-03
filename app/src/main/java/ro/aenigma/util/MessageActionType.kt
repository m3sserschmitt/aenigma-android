package ro.aenigma.util

enum class MessageActionType {
    @Description("Text message")
    TEXT,

    @Description("Message deleted")
    DELETE,

    @Description("Conversation deleted")
    DELETE_ALL,

    @Description("Replied to message")
    REPLY,

    @Description("Text message within a group")
    GROUP_UPDATE,

    @Description("Group created")
    GROUP_CREATE
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
