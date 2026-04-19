package uk.co.imoqen.nourish.common

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Functions in this file handle the compression, local saving, and saving an image as a MipMap
 * (lower quality and higher quality).
 * A MipMapping approach was adopted so that a higher quality image can be stored locally with the
 * recipe, and a lower quality image can be sent to the LLM. Thus, reducing token usage and cost
 * of the LLM call while preserving the higher quality for saving on the device.
 *
 * Image Compression: adapted from
 * https://www.geeksforgeeks.org/android/how-to-compress-image-in-android-before-uploading-it-to-firebase-storage/
 *
 * Image Saving: adapted from
 * https://github.com/philipplackner/ImageCompression/blob/master/app/src/main/java/com/plcoding/imagecompression/FileManager.kt
 */

/**
 * Produces the MipMap pair of low quality, high quality.
 * @param uri - The uri of the image to MipMap
 * @param contentResolver - The content resolver used to assist image saving
 * @returns The MipMap as a pair.
 *
 * @author Imogen Pemberton, 2026
 */
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
        // Larger is displayed to the user (1024+). can write about this in the report.
        // Can alter this value in the report and show how compression changes.
        if (greatestDimension > 1024) {
            val scale = 1024f / greatestDimension
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            // Resize accordingly
            bitmap = bitmap.scale(newWidth, newHeight, false)
        }
        // Compress: 2nd param is compression quality 0-100; 25 is highly compressed.
        // MipMapping approach: the highly compressed (25) is used to send to the LLM.
        // The less compressed (75) is used for displaying to the user.
        val lowQuality = compressImage(bitmap, 25) // 25% of original quality
        val highQuality = compressImage(bitmap, 75) // 75% of original quality
        if (lowQuality != null && highQuality != null) {
            // first is low quality, second is high quality:
            return Pair(lowQuality, highQuality)
        }
    }
    return null
}

/**
 * Compresses an image given a bitmap.
 * @param bitmap - The image bitmap to compress.
 * @param quality - value from 0 to 100 reflecting quality preservation: 100 is 100% of original quality,
 *                  25 is 25% of original quality. Therefore lower = lower quality.
 * @returns The compressed image as a ByteArray, or null if an error occurred.
 */
fun compressImage(
    bitmap: Bitmap,
    quality: Int
): ByteArray? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
    val fileInBytes: ByteArray = byteArrayOutputStream.toByteArray()
    return fileInBytes
}

/**
 * Saves the MipMap locally.
 * @param uri - The uri of the image to save as a MipMap
 * @param context - The local context, required to access device's local storage
 * @param onSave - function which runs when the image save has complete.
 */
suspend fun saveMipmap(
    uri: Uri?,
    context: Context,
    onSave: (String, String) -> Unit
) {
    if (uri != null) {
        val contentResolver = context.contentResolver
        // Mime type is like png, jpg, etc
        val mimeType = contentResolver.getType(uri)
        // Gets the file type:
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        val compressedByteArrayMipmap = produceMipmap(uri, contentResolver)

        // Uses timestamp to ensure each saved image is unique; no overwriting.
        if (compressedByteArrayMipmap != null) {
            val time = System.currentTimeMillis()
            val lowQualityFilename = "compressed-low-quality-recipe-image-$time.$extension"
            val highQualityFilename = "compressed-high-quality-recipe-image-$time.$extension"
            saveImage(lowQualityFilename, compressedByteArrayMipmap.first, context)
            saveImage(highQualityFilename, compressedByteArrayMipmap.second, context)
            onSave(lowQualityFilename, highQualityFilename)
        }
    }
}

/**
 * Saves the image locally.
 * @param filename - The name to save the image file under.
 * @param byteArray - the byte array of the image to save.
 * @param context - The local context, required to access the device's local storage.
 */
suspend fun saveImage(
    filename: String,
    byteArray: ByteArray,
    context: Context
) {
    withContext(Dispatchers.IO) {
        context
            // Save to internal storage:
            .openFileOutput(filename, Context.MODE_PRIVATE)
            .use { outputStream ->
                outputStream.write(byteArray)
            }
    }
}


