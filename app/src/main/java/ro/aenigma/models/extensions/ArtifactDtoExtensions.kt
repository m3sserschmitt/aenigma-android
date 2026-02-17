package ro.aenigma.models.extensions

import ro.aenigma.models.ArtifactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.factories.MessageDtoFactory
import java.time.ZonedDateTime

object ArtifactDtoExtensions {
    @JvmStatic
    fun ArtifactDto.toMessageDto(serverUuid: String, dateReceivedOnServer: ZonedDateTime?): MessageDto? {
        return MessageDtoFactory.createIncoming(
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
