package uk.co.imoqen.nourish.common

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

// with help from https://www.geeksforgeeks.org/android/how-to-compress-image-in-android-before-uploading-it-to-firebase-storage/
// and for saving: https://github.com/philipplackner/ImageCompression/blob/master/app/src/main/java/com/plcoding/imagecompression/FileManager.kt

// compress the image to ensure low file size for sending to cloud run service and LLM
// returns a mipmap of pair low qual, high qual
fun produceMipmap(
    uri: Uri,
    contentResolver: ContentResolver
): Pair<ByteArray, ByteArray>? {
    // Get the bitmap from the uri of the image:
    var bitmap: Bitmap? = null
    try {
        bitmap = ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(contentResolver, uri))
    } catch (e: IOException) {
        e.printStackTrace()
    }
    // Resize the image to have greatest dimension 1024px
    if (bitmap != null) {
        val width = bitmap.width
        val height = bitmap.height
        val greatestDimension = if (width > height) width else height
        // todo could adopt a mipmapping approach where the smallest is sent to the llm (512kb) and then
        // larger is displayed to the user (1024+). can write about this in the report.
        if (greatestDimension > 1024) { // CAN ALTER THIS VALUE IN THE REPORT AND SHOW HOW THE COMPRESSION CHANGES
            val scale = 1024f / greatestDimension
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            // Resize accordingly
            bitmap = bitmap.scale(newWidth, newHeight, false)
        }
        // Compress: 2nd param is compression quality 0-100; 25 is highly compressed.
        // MIP MAPPING APPROACH: the highly compressed (25) is used to send to the LLM. the less compressed (50) is used to
        // be displayed to the user.
        val lowQuality = compressImage(bitmap, 25) // 25% of original quality
        val highQuality = compressImage(bitmap, 75) // 75% of original quality
        if (lowQuality != null && highQuality != null) {
            return Pair(lowQuality, highQuality) // first is low qual, second is high qual
        }
    }
    return null
}

fun compressImage(
    bitmap: Bitmap,
    quality: Int // val from 0 to 100: 100 is 100% of original quality, 25 is 25% of original quality etc. so lower = lower quality.
): ByteArray? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
    val fileInBytes: ByteArray = byteArrayOutputStream.toByteArray()
    return fileInBytes
}

// save the image locally to be used when making the llm call
suspend fun saveMipmap(
    uri: Uri?,
    context: Context,
    onSave: (String, String) -> Unit
) {
    if (uri != null) {
        Log.d("PhotoPicker", "Selected URI: $uri")
        val contentResolver = context.contentResolver

        // mime type is like image/png etc
        val mimeType = contentResolver.getType(uri)
        // gets the file type:
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        val compressedByteArrayMipmap = produceMipmap(uri, contentResolver)

        if (compressedByteArrayMipmap != null) {
            val time = System.currentTimeMillis()
            val lowQualityFilename = "compressed-low-quality-recipe-image-$time.$extension"
            val highQualityFilename = "compressed-high-quality-recipe-image-$time.$extension"
            saveImage(lowQualityFilename, compressedByteArrayMipmap.first, context)
            saveImage(highQualityFilename, compressedByteArrayMipmap.second, context)
            onSave(lowQualityFilename, highQualityFilename)
        }
    } else {
        Log.d("PhotoPicker", "No media selected")
    }
}

suspend fun saveImage(
    filename: String,
    byteArray: ByteArray,
    context: Context
) {
    withContext(Dispatchers.IO) {
        Log.d("IMG", "Saving $filename exists=${context.getFileStreamPath(filename).exists()}")
        context
            // save to internal storage
            // filename, context mode, then writes to the byte array
            .openFileOutput(filename, Context.MODE_PRIVATE)
            .use { outputStream ->
                outputStream.write(byteArray)
            }
    }
}


