package uk.co.imoqen.nourish.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import uk.co.imoqen.nourish.network.request.LlmActionRequest
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import uk.co.imoqen.nourish.network.response.LlmNutrition
import uk.co.imoqen.nourish.network.response.LlmQuestionResponse
import uk.co.imoqen.nourish.network.response.LlmRecipe

/**
 * Retrofit2 API service, this declares the endpoints to the API which can be called.
 *
 * @author Imogen Pemberton, 2026
 */
interface ApiService {

    // LLM Requests:

    /**
     * POST /recipe endpoint, to generate a recipe for a given meal.
     * This fulfills the requirement "How to cook X?"
     */
    @POST("recipe")
    fun generateRecipe(
        @Body() request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmRecipe>

    /**
     * POST /recipe-from-dish-image endpoint, to generate the recipe for a dish, given an image.
     * This fulfills the requirement "How to cook <photo>?"
     */
    @Multipart
    @POST("recipe-from-dish-image")
    fun generateRecipeFromDishImage(
        @Part image: MultipartBody.Part,
        @Part("detail") request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmRecipe>

    /**
     * POST /dishes-from-image endpoint, to generate 4x dishes for a given image.
     * This fulfills the requirement "How to cook <photo>?"
     */
    @Multipart
    @POST("dishes-from-image")
    fun generateDishesFromImage(
        @Part image: MultipartBody.Part,
        @Part("detail") request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmDishesProposed>

    /**
     * POST /dishes-from-ingredients endpoint, to generate 4x dishes for a given list of ingredients.
     * This fulfills the requirement "What can I cook with <list_of_ingredients>?"
     */
    @POST("dishes-from-ingredients")
    fun generateDishesFromIngredients(
        @Body() request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmDishesProposed>

    /**
     * POST /dishes-from-ingredients-image, to generate 4x dishes for a given image of ingredients.
     * This fulfills the requirement "What can I cook with <photo>?"
     */
    @Multipart
    @POST("dishes-from-ingredients-image")
    fun generateDishesFromIngredientsImage(
        @Part image: MultipartBody.Part,
        @Part("detail") request: LlmRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmDishesProposed>

    // LLM Action Requests:

    /**
     * POST /recipe-nutrition, to generate nutrition for 1 serving of a recipe.
     * This fulfills the requirement "What is the nutrition of <recipe>?"
     */
    @POST("recipe-nutrition")
    fun generateNutrition(
        @Body() request: LlmActionRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmNutrition>

    /**
     * POST /query-recipe, to generate an answer for a recipe query.
     * This fulfills the requirement "Answer my question regarding <recipe>?"
     */
    @POST("query-recipe")
    fun generateQueryAnswer(
        @Body() request: LlmActionRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmQuestionResponse>

    /**
     * POST /alter-recipe, to generate an alteration of a recipe.
     * This fulfills the requirement "Alter the following recipe <recipe> such that <alteration>"
     */
    @POST("alter-recipe")
    fun generateAlteredRecipe(
        @Body() request: LlmActionRequest,
        @Header("Authorization") bearer: String
    ): Call<LlmRecipe>
}