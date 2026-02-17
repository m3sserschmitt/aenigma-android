package ro.aenigma.data.database.extensions

import android.content.Context
import org.ocpsoft.prettytime.PrettyTime
import ro.aenigma.R
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toDto
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.MessageWithDetailsDto

object MessageWithDetailsEntityExtensions {
    @JvmStatic
    fun MessageWithDetails.toDto(): MessageWithDetailsDto {
        return MessageWithDetailsDto(
            message = message.toDto(),
            sender = sender?.toDto(),
            actionFor = actionFor?.toDto()
        )
    }

    @JvmStatic
    fun MessageWithDetails.toArticleDto(context: Context): ArticleDto {
        val date = PrettyTime().format(message.date)
        val senderName = sender?.name ?: context.getString(R.string.unknown)
        return ArticleDto(
            id = message.id,
            title = context.getString(R.string.article_title_template,  senderName, date),
            description = message.text,
            url = null,
            date = message.date.toString(),
            imageUrls = message.files
        )
    }
}
