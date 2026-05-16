package com.hallisanthe.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageCompressor {

    // Compresses user-selected image to JPEG in cache for faster upload and lower bandwidth.
    suspend fun compress(context: Context, imageUri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            val compressedFile = File(context.cacheDir, "compressed_${UUID.randomUUID()}.jpg")
            FileOutputStream(compressedFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, output)
            }
            Uri.fromFile(compressedFile)
        } catch (_: Exception) {
            null
        }
    }
}
