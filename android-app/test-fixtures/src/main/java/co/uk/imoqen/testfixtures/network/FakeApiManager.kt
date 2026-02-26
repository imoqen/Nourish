package co.uk.imoqen.testfixtures.network

import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import uk.co.imoqen.nourish.network.response.ApiResponse
import uk.co.imoqen.nourish.network.response.LlmDishesProposed
import java.io.File

class FakeApiManager(): ApiManager {

    override fun makeRetrofitCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String,
        filesDir: File?,
        onDishesProposed: (LlmDishesProposed?) -> Unit,
        onResponse: (ApiResponse?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

}