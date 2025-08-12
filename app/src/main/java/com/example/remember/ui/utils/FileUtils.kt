package com.example.remember.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun copyFileToInternalStorage(context: Context, sourceUri: Uri): Uri? {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(sourceUri) ?: return null

        val fileExtension = getFileExtension(context, sourceUri)
        val destinationFile = File(context.filesDir, "item${System.currentTimeMillis()}.$fileExtension")

        try {
            val outputStream = FileOutputStream(destinationFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return Uri.fromFile(destinationFile)
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)?.split('/')?.lastOrNull()
    }

    fun createTempImageUri(context: Context): Uri {
        val tempFile = File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            context.cacheDir
        ).apply {
            createNewFile()
        }
        val authority = "${context.packageName}.provider"
        return FileProvider.getUriForFile(context, authority, tempFile)
    }
}