package ro.aenigma.util

enum class MessageType {
    @Description("Text message")
    TEXT,

    @Description("Message deleted")
    DELETE,

    @Description("Conversation deleted")
    DELETE_ALL
}

inline fun <reified T : Enum<T>>String?.parseEnum(): T? {
    if (this == null) {
        return null
    }
    return enumValues<T>().find { it.name.equals(this, ignoreCase = true) }
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
