package ro.aenigma.models.extensions

import ro.aenigma.models.ArticleDto
import ro.aenigma.models.AttachmentsMetadataDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.models.extensions.MessageDtoExtensions.getDateTime
import ro.aenigma.util.PrettyDateFormatter

object MessageWithDetailsDtoExtensions {
    @JvmStatic
    fun MessageWithDetailsDto.toArticleDto(uri: String?, files: List<String>?, metadata: AttachmentsMetadataDto?): ArticleDto {
        return ArticleDto(
            id = null,
            title = metadata?.title,
            author = sender?.name,
            description = metadata?.description,
            url = uri,
            imageUrls = files,
            date = PrettyDateFormatter.format(message.getDateTime())
        )
    }
}
