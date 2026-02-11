package uk.co.imoqen.nourish.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// MAYBE DELETE THIS? replaced by apimodule
object RetrofitClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    private const val GOOGLE_CLOUD_URL = "GOOGLE_CLOUD_URL"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GOOGLE_CLOUD_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object ApiClient {
    val apiService: ApiService by lazy {
        RetrofitClient.retrofit.create(ApiService::class.java)
    }
}