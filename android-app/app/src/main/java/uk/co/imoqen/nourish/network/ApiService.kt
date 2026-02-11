package uk.co.imoqen.nourish.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("generate")
    fun generateRecipe(@Body() request: LlmRequest, @Header("Authorization") bearer: String): Call<LlmResponse>

}