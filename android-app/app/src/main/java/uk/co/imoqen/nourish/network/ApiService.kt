package uk.co.imoqen.nourish.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmRecipe

/**
 * Retrofit2 API service, this declares the endpoints to the API which can be called.
 */
interface ApiService {

    /**
     * POST /generate endpoint, to generate a recipe for a given meal.
     * This fulfills the requirement "How to cook X?"
     */
    @POST("recipe")
    fun generateRecipe(
        @Body() request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmRecipe>

    @Multipart
    @POST("recipe-from-dish-image")
    fun generateRecipeFromDishImage(
        @Part image: MultipartBody.Part,
        @Part("detail") request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmRecipe>

    @POST("dishes-from-ingredients")
    fun generateDishesFromIngredients(
        @Body() request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmDishesProposed>

    // have to use a different endpoint because the llm prompt will be different in the backend
    @Multipart
    @POST("dishes-from-ingredients-image")
    fun generateDishesFromIngredientsImage(
        @Part image: MultipartBody.Part,
        @Part("detail") request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmDishesProposed>

    @Multipart
    @POST("dishes-from-image")
    fun generateDishesFromImage(
        @Part image: MultipartBody.Part,
        @Part("detail") request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmDishesProposed>

}