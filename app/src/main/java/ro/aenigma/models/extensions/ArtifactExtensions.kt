package ro.aenigma.models.extensions

import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.Artifact
import java.time.ZonedDateTime

object ArtifactExtensions {
    @JvmStatic
    fun Artifact.toMessage(serverUuid: String, dateReceivedOnServer: ZonedDateTime?): MessageEntity? {
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
