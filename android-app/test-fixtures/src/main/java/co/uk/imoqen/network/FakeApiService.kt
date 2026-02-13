package co.uk.imoqen.network

import retrofit2.Call

class FakeApiService(): ApiService {
    override fun generateRecipe(
        request: LlmRequest,
        bearer: String
    ): Call<LlmResponse> {
        TODO("Not yet implemented")
    }
}