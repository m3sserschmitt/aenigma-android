package ro.aenigma.data.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference

class BaseUrlInterceptor : Interceptor {

    private val baseUrl = AtomicReference<HttpUrl>()

    fun setBaseUrl(newUrl: String) {
        baseUrl.set(newUrl.toHttpUrlOrNull())
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val newBaseUrl = baseUrl.get()

        val newUrl = newBaseUrl?.newBuilder()
            ?.scheme(originalUrl.scheme)
            ?.host(newBaseUrl.host)
            ?.port(newBaseUrl.port)
            ?.encodedPath(originalUrl.encodedPath)
            ?.query(originalUrl.query)
            ?.build() ?: originalUrl

        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
