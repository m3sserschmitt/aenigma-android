package ro.aenigma.models.extensions

import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.Artifact
import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime

object ArtifactExtensions {
    @JvmStatic
    fun Artifact.isGroupUpdate(): Boolean {
        return (type == MessageType.GROUP_CREATE
                || type == MessageType.GROUP_RENAMED
                || type == MessageType.GROUP_MEMBER_ADD
                || type == MessageType.GROUP_MEMBER_REMOVE) && resourceUrl != null
    }

    @JvmStatic
    fun Artifact.isText(): Boolean {
        return (type == MessageType.TEXT || type == MessageType.REPLY) && text != null
    }

    @JvmStatic
    fun Artifact.encryptedText(): String? {
        return CryptoProvider.masterKeyEncryptEx(CryptoProvider.base64Decode(text ?: return null) ?: return null)
    }

    @JvmStatic
    fun Artifact.toMessage(serverUuid: String, dateReceivedOnServer: ZonedDateTime?): MessageEntity? {
        val text = if(isText()) {
            text
        }
        else if(isGroupUpdate()) {
            encryptedText()
        } else {
            text
        }
        return MessageEntityFactory.createIncoming(
            chatId = chatId ?: return null,
            senderAddress = senderAddress ?: return null,
            serverUUID = serverUuid,
            text = text,
            type = type ?: return null,
            actionFor = actionFor,
            refId = refId ?: return null,
            dateReceivedOnServer = dateReceivedOnServer,
        )
    }
}
