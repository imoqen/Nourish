package uk.co.imoqen.nourish.network.apimanager

import android.graphics.Bitmap
import uk.co.imoqen.nourish.network.LlmRequest

interface ApiManager {

    fun handleRecipeGeneration(
        request: LlmRequest,
        bearerToken: String,
        onResponse: (String?) -> Unit
    )

    fun handleFromImage(
        request: LlmRequest,
        bearerToken: String,
        image: Bitmap,
        onResponse: (String?) -> Unit
    )

}