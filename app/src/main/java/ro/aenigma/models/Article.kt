package ro.aenigma.models

data class Article (
    val id: Long?,
    val title: String?,
    val description: String?,
    val url: String?,
    val imageUrls: List<String?>?,
    val date: String?,
)
