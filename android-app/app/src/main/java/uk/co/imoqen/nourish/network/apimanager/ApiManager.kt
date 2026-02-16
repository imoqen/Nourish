package uk.co.imoqen.nourish.network.apimanager

import android.graphics.Bitmap
import uk.co.imoqen.nourish.network.request.LlmRequest
import java.io.File

interface ApiManager {

    fun handleRecipeGeneration(
        request: LlmRequest,
        bearerToken: String,
        onResponse: (String?) -> Unit
    )

    fun handleFromImage(
        request: LlmRequest,
        bearerToken: String,
        imageFilename: String,
        filesDir: File,
        onResponse: (String?) -> Unit
    )

}