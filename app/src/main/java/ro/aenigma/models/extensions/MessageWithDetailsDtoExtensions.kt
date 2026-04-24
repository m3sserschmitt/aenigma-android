package ro.aenigma.models.extensions

import ro.aenigma.models.ArticleDto
import ro.aenigma.models.AttachmentsMetadataDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.models.extensions.MessageDtoExtensions.getDateTime
import ro.aenigma.util.Constants.Companion.NEWS_FEED_TIME_PERIOD
import ro.aenigma.util.PrettyDateFormatter
import java.time.ZonedDateTime

object MessageWithDetailsDtoExtensions {
    @JvmStatic
    fun MessageWithDetailsDto.toArticleDto(
        uri: String?,
        files: List<String>?,
        metadata: AttachmentsMetadataDto?
    ): ArticleDto {
        return ArticleDto(
            messageId = message.id,
            title = metadata?.title,
            author = sender?.name,
            description = metadata?.description,
            url = uri,
            imageUrls = files,
            date = PrettyDateFormatter.format(getDateTime())
        )
    }

    @JvmStatic
    fun MessageWithDetailsDto.getDateTime(): ZonedDateTime {
        return message.getDateTime()
    }

    @JvmStatic
    fun MessageWithDetailsDto.isWithinNewsfeedPeriod(): Boolean {
        return getDateTime().isAfter(ZonedDateTime.now().minus(NEWS_FEED_TIME_PERIOD))
    }
}
