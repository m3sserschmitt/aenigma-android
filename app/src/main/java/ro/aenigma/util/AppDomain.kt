package ro.aenigma.util

import ro.aenigma.util.Constants.Companion.APP_DOMAIN
import java.net.URL

fun String?.isAppDomain(): Boolean {
    return if (this.isNullOrBlank()) {
        false
    } else try {
        val url = URL(this)
        val host = url.host.lowercase()
        val appDomain = APP_DOMAIN.lowercase()
        host == appDomain || host.endsWith(".$appDomain")
    } catch (_: Exception) {
        false
    }
}
