package uk.co.imoqen.nourish.common

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.graphics.scale
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

// with help from https://www.geeksforgeeks.org/android/how-to-compress-image-in-android-before-uploading-it-to-firebase-storage/
// and for saving: https://github.com/philipplackner/ImageCompression/blob/master/app/src/main/java/com/plcoding/imagecompression/FileManager.kt

fun compressImage(
    uri: Uri,
    contentResolver: ContentResolver
): ByteArray? {

    // images are stored with timestamp as their name
    val timestamp = "" + System.currentTimeMillis()
    var bitmap: Bitmap? = null

    // first, resize:

    //val bitmap = null
    try {
        // this is for image bitmap, to display the image - not for compressing it etc.
        bitmap = ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(contentResolver, uri))
        //.asImageBitmap()*/
        //MediaStore.Images.Media.getBitmap(contentResolver), uri)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val baos = ByteArrayOutputStream()


    // here we can choose quality factor
    // in third parameter(ex. here it is 25)
    if (bitmap != null) {
        // first, resize:

        val width = bitmap.width
        val height = bitmap.height
        Log.i("PhotoPicker","original width and height: $width, $height")

        val greatestDimension = if (width > height) width else height
        // only scale down if required
        // could adopt a mipmapping approach where the smallest is sent to the llm (512kb) and then
        // larger is displayed to the user (1024+). can write about this in the report.
        if (greatestDimension > 1024) { // CAN ALTER THIS VALUE IN THE REPORT AND SHOW HOW THE COMPRESSION CHANGES
            // float vals
            val scale = 1024f / greatestDimension
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            Log.i("PhotoPicker", "resizing down to $newWidth, $newHeight")
            // resize accordingly
            bitmap = bitmap.scale(newWidth, newHeight, false)
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)

        val fileInBytes: ByteArray = baos.toByteArray()
        return fileInBytes
    }
    return null
}

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
            val filename = "compressed-recipe-image-$time.$extension"
            withContext(Dispatchers.IO) {
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

