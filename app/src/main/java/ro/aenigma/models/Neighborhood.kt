package ro.aenigma.models

data class Neighborhood (
    val address: String? = null,
    val hostname: String? = null,
    val neighbors: List<String>? = null,
)
