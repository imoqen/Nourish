package uk.co.imoqen.nourish.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit2 API service, this declares the endpoints to the API which can be called.
 */
interface ApiService {

    /**
     * POST /generate endpoint, to generate a recipe for a given meal.
     * This fulfills the requirement "How to cook X?"
     */
    @POST("generate") // maybe change to "howtocook"
    fun generateRecipe(@Body() request: LlmRequest, @Header("Authorization") bearer: String): Call<LlmResponse>

}