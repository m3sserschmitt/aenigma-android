package ro.aenigma.models.extensions

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ro.aenigma.models.Neighborhood

object NeighborhoodExtensions {
    @JvmStatic
    fun Neighborhood.normalizeHostname(): Neighborhood {
        return copy(hostname = hostname?.toHttpUrlOrNull().toString())
    }
}
