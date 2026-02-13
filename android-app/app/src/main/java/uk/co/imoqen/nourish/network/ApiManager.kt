package uk.co.imoqen.nourish.network

interface ApiManager {

    fun handleRecipeGeneration(
        request: LlmRequest,
        bearerToken: String,
        onResponse: (String?) -> Unit
    )

}