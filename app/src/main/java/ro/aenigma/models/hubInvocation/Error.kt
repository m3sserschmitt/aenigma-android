package ro.aenigma.models.hubInvocation

data class Error(
    val message: String? = null,
    val properties: List<String>? = null
) {
    override fun toString(): String {
        return if(message != null) {
            message + if (properties != null) {
                "; Affected properties: " + properties.joinToString(limit = 3)
            } else {
                ""
            }
        }
        else{
            ""
        }
    }
}
