package ro.aenigma.models

data class Vertex (
    val publicKey: String? = null,
    val signedData: String? = null,
    val neighborhood: Neighborhood? = null
)
