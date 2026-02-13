package uk.co.imoqen.nourish.network.apimanager

import uk.co.imoqen.nourish.network.LlmRequest

interface ApiManager {

    fun handleRecipeGeneration(
        request: LlmRequest,
        bearerToken: String,
        onResponse: (String?) -> Unit
    )

}