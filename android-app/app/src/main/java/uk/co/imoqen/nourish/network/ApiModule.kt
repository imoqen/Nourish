package uk.co.imoqen.nourish.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Retrofit2 API module, building the Retrofit instance which is called in the service.
 * This is connected to the Google Cloud URL which is called.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    private const val GOOGLE_CLOUD_URL = "GOOGLE_CLOUD_URL"

    // required so that custom timeout can be set, as the llm can sometimes take long to respond
    // 12. timeouts: https://www.baeldung.com/guide-to-okhttp
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_CLOUD_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}