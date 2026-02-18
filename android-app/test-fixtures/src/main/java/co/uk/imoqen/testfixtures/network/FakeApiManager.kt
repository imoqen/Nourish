package co.uk.imoqen.testfixtures.network

import uk.co.imoqen.nourish.network.apimanager.ApiManager
import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import java.io.File

class FakeApiManager(): ApiManager {

    override fun makeRetrofitCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String, // optional; only for generating from image
        filesDir: File?, // optional; only for generating from image
        onResponse: (String?) -> Unit,
    ) {
        //TODO not yet implemented
    }

}