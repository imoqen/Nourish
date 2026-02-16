package uk.co.imoqen.nourish.network.apimanager

import uk.co.imoqen.nourish.network.request.LlmRequest
import uk.co.imoqen.nourish.network.request.LlmRequestType
import java.io.File

interface ApiManager {

    fun makeRetrofitCall(
        type: LlmRequestType,
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String = "", // optional; only for generating from image
        filesDir: File? = null, // optional; only for generating from image
        onResponse: (String?) -> Unit,
    )

}