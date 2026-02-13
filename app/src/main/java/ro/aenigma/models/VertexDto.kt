package ro.aenigma.models

data class VertexDto (
    val address: String? = null,
    val publicKey: String? = null,
    val signedData: String? = null,
    val neighborhood: NeighborhoodDto? = null
)
