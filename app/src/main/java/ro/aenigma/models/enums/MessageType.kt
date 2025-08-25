package ro.aenigma.models.enums

enum class MessageType {
    TEXT,
    REPLY,
    FILES,
    DELETE,
    DELETE_ALL,
    GROUP_CREATE,
    GROUP_RENAMED,
    GROUP_MEMBER_ADD,
    GROUP_MEMBER_LEAVE,
    GROUP_MEMBER_REMOVE,
}
