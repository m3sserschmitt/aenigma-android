package ro.aenigma.services

import okhttp3.OkHttpClient

interface IOkHttpClientProvider {
    suspend fun getInstance(): OkHttpClient
}
