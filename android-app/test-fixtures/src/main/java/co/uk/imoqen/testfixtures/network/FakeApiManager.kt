package co.uk.imoqen.testfixtures.network

import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.request.LlmRequest

class FakeApiManager(): ApiManager {
    override fun handleRecipeGeneration(
        request: LlmRequest,
        bearerToken: String,
        onResponse: (String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

}