package ro.aenigma.models

data class SharedDataCreate (
    val publicKey: String,
    val signedData: String,
    val accessCount: Int = 1
)
