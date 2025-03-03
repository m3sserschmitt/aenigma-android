package ro.aenigma.models

class SharedDataCreate (
    val publicKey: String,
    val signedData: String,
    val accessCount: Int = 1
)
