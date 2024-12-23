package ro.aenigma.util

import ro.aenigma.util.Constants.Companion.APP_DOMAIN
import java.net.URL

fun String?.isAppDomain(): Boolean
{
    return if(this.isNullOrBlank())
        false
    else try {
        val url = URL(this)
        url.host == APP_DOMAIN
    } catch (e: Exception) {
        false
    }
}
