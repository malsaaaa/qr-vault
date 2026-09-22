package com.qrvault.data.repository

import android.net.Uri
import com.qrvault.data.local.database.QrItem
import com.qrvault.data.local.database.QrItemDao
import com.qrvault.data.local.storage.QrImageStorage
import kotlinx.coroutines.flow.Flow

class ImageImportException(cause: Throwable) : Exception(cause)

class QrRepository(
    private val dao: QrItemDao,
    private val imageStorage: QrImageStorage,
) {

    fun observeAll(): Flow<List<QrItem>> = dao.observeAll()

    suspend fun getById(id: Long): QrItem? = dao.getById(id)

    fun shareUriFor(item: QrItem): Uri = imageStorage.shareUriFor(item.imagePath)

    suspend fun addQr(
        uri: Uri,
        name: String,
        provider: String,
        description: String?,
    ): Result<Long> {
        val imageName = try {
            imageStorage.importImage(uri)
        } catch (e: Exception) {
            return Result.failure(ImageImportException(e))
        }
        val now = System.currentTimeMillis()
        val item = QrItem(
            name = name.trim(),
            provider = provider.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            imagePath = imageName,
            createdAt = now,
            updatedAt = now,
        )
        return try {
            val id = dao.insert(item)
            Result.success(id)
        } catch (e: Exception) {
            imageStorage.deleteImage(imageName)
            Result.failure(e)
        }
    }

    suspend fun updateQr(
        item: QrItem,
        newImageUri: Uri?,
        name: String,
        provider: String,
        description: String?,
    ): Result<Unit> {
        val oldPath = item.imagePath
        val newPath = if (newImageUri != null) {
            try {
                imageStorage.importImage(newImageUri)
            } catch (e: Exception) {
                return Result.failure(ImageImportException(e))
            }
        } else {
            oldPath
        }
        val updated = item.copy(
            imagePath = newPath,
            name = name.trim(),
            provider = provider.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            updatedAt = System.currentTimeMillis(),
        )
        return try {
            dao.update(updated)
            if (newImageUri != null) {
                imageStorage.deleteImage(oldPath)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (newImageUri != null) {
                imageStorage.deleteImage(newPath)
            }
            Result.failure(e)
        }
    }

    suspend fun deleteQr(item: QrItem): Result<Unit> {
        return try {
            dao.delete(item)
            imageStorage.deleteImage(item.imagePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}