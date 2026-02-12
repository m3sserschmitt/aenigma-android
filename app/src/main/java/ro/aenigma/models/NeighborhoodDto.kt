package ro.aenigma.models

data class NeighborhoodDto (
    val address: String? = null,
    val hostname: String? = null,
    val onionService: String? = null,
    val neighbors: List<String?>? = null,
)
