package ro.aenigma.models

data class ArticleDto (
    val messageId: Long? = null,
    val title: String? = null,
    val author: String? = null,
    val description: String? = null,
    val url: String? = null,
    val imageUrls: List<String?>? = null,
    val date: String? = null,
)
