package ro.aenigma.models.hubInvocation

data class RoutingRequest(
    val payloads: List<String?>? = null,
    val uuid: String? = null
)
