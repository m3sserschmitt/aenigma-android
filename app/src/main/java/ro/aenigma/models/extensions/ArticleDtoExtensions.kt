package ro.aenigma.models.extensions

import ro.aenigma.models.ArticleDto
import ro.aenigma.util.PrettyDateFormatter

object ArticleDtoExtensions {
    @JvmStatic
    fun ArticleDto.prettyFormat(): ArticleDto {
        return copy(
            id = null,
            title = title?.takeIf { it.isNotBlank() },
            author = author?.takeIf { it.isNotBlank() },
            description = description?.takeIf { it.isNotBlank() },
            imageUrls = imageUrls?.filterNotNull(),
            date = date?.let {
                PrettyDateFormatter.format(it)
            }
        )
    }
}
