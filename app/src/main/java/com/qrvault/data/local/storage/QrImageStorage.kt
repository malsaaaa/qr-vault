package com.qrvault.data.local.storage

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface QrImageStorage {

    fun fileFor(name: String): File

    suspend fun importImage(uri: Uri): String

    fun shareUriFor(name: String): Uri

    suspend fun deleteImage(name: String)
}

class PrivateImageStorage(
    private val context: Context,
) : QrImageStorage {

    private val rootDir: File = File(context.filesDir, "qr_images")

    override fun fileFor(name: String): File = File(rootDir, name)

    override fun shareUriFor(name: String): Uri {
        val authority = "${context.applicationContext.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, fileFor(name))
    }

    override suspend fun importImage(uri: Uri): String = withContext(Dispatchers.IO) {
        if (!rootDir.exists()) rootDir.mkdirs()
        val name = "qr_${System.currentTimeMillis()}_${UUID.randomUUID()}.${extension(uri)}"
        val destination = File(rootDir, name)
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open source image")
        input.use { source ->
            destination.outputStream().use { target -> source.copyTo(target) }
        }
        if (!canDecode(destination)) {
            destination.delete()
            throw IOException("Image could not be decoded")
        }
        name
    }

    override suspend fun deleteImage(name: String) {
        withContext(Dispatchers.IO) {
            runCatching { fileFor(name).delete() }
        }
    }

    private fun extension(uri: Uri): String {
        val mime = runCatching { context.contentResolver.getType(uri) }.getOrNull()
        val fromMime = when {
            mime == "image/png" -> "png"
            mime == "image/webp" -> "webp"
            mime == "image/gif" -> "gif"
            mime == "image/jpeg" || mime == "image/jpg" -> "jpg"
            mime?.startsWith("image/") == true -> "jpg"
            else -> null
        }
        if (fromMime != null) return fromMime
        val path = uri.path ?: return "jpg"
        val dot = path.lastIndexOf('.')
        if (dot in 0 until path.length - 1) {
            val ext = path.substring(dot + 1).lowercase()
            if (ext in setOf("jpg", "jpeg", "png", "webp", "gif")) {
                return if (ext == "jpeg") "jpg" else ext
            }
        }
        return "jpg"
    }

    private fun canDecode(file: File): Boolean {
        return runCatching {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            options.outWidth > 0 && options.outHeight > 0
        }.getOrDefault(false)
    }
}