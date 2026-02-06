package uk.co.imoqen.nourish.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("posts/{id}")
    fun getPostById(@Path("id") postId: Int): Call<ApiResponse>

    @POST("generate")
    fun generateRecipe(@Body() request: LlmRequest, @Header("Authorization") bearer: String): Call<LlmResponse>

}