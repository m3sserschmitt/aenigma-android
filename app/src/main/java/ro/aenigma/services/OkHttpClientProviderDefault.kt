package ro.aenigma.services

import okhttp3.OkHttpClient

class OkHttpClientProviderDefault: IOkHttpClientProvider {
    override suspend fun getInstance(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }
}
