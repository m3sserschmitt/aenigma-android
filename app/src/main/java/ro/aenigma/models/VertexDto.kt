package ro.aenigma.models

data class VertexDto (
    val publicKey: String? = null,
    val signedData: String? = null,
    val neighborhood: NeighborhoodDto? = null
)
