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
fun compressImage(
    uri: Uri,
    contentResolver: ContentResolver
): ByteArray? {
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
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val fileInBytes: ByteArray = byteArrayOutputStream.toByteArray()
        return fileInBytes
    }
    return null
}

// save the image locally to be used when making the llm call
suspend fun saveImage(
    uri: Uri?,
    context: Context,
    onSave: (String) -> Unit
) {
    if (uri != null) {
        Log.d("PhotoPicker", "Selected URI: $uri")
        val contentResolver = context.contentResolver

        // mime type is like image/png etc
        val mimeType = contentResolver.getType(uri)
        // gets the file type:
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        val compressedByteArray = compressImage(uri, contentResolver)

        if (compressedByteArray != null) {
            val time = System.currentTimeMillis()
            val filename = "compressed-recipe-image-$time-${UUID.randomUUID()}.$extension"
            withContext(Dispatchers.IO) {
                Log.d("IMG", "Saving $filename exists=${context.getFileStreamPath(filename).exists()}")
                context
                    // save to internal storage
                    // filename, context mode, then writes to the byte array
                    .openFileOutput(filename, Context.MODE_PRIVATE)
                    .use { outputStream ->
                        outputStream.write(compressedByteArray)
                    }
                    onSave(filename)
            }
        }
    } else {
        Log.d("PhotoPicker", "No media selected")
    }
}

