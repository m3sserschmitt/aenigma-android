package ro.aenigma.models

data class VertexBroadcastRequest(
    val publicKey: String,
    val signedData: String
)
