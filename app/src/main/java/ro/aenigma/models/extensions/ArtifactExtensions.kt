package ro.aenigma.models.extensions

import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.ArtifactDto
import java.time.ZonedDateTime

object ArtifactExtensions {
    @JvmStatic
    fun ArtifactDto.toMessage(serverUuid: String, dateReceivedOnServer: ZonedDateTime?): MessageEntity? {
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
