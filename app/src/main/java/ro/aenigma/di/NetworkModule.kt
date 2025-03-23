package ro.aenigma.di

import ro.aenigma.data.network.BaseUrlInterceptor
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.util.Constants.Companion.SERVER_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import ro.aenigma.util.SerializerExtensions.createJsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrlInterceptor(): BaseUrlInterceptor {
        return BaseUrlInterceptor()
    }

    @Singleton
    @Provides
    fun provideHttpClient(baseUrlInterceptor: BaseUrlInterceptor): OkHttpClient
    {
        return OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(baseUrlInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideConverterFactory(): JacksonConverterFactory
    {
        return createJsonConverterFactory()
    }

    @Singleton
    @Provides
    fun provideRetrofitInstance(
        okHttpClient: OkHttpClient,
        jacksonConverterFactory: JacksonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(jacksonConverterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): EnigmaApi
    {
        return retrofit.create(EnigmaApi::class.java)
    }
}
