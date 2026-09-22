package com.qrvault.data.repository

import android.net.Uri
import com.qrvault.data.local.database.QrItem
import com.qrvault.data.local.database.QrItemDao
import com.qrvault.data.local.storage.QrImageStorage
import java.io.File
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QrRepositoryTest {

    private class FakeDao : QrItemDao {
        private val list = mutableListOf<QrItem>()
        private val flow = MutableStateFlow<List<QrItem>>(emptyList())
        private var nextId = 1L

        fun store(items: List<QrItem>) {
            list.clear()
            list.addAll(items)
            flow.value = list.toList()
        }

        fun items(): List<QrItem> = list.toList()

        override fun observeAll(): Flow<List<QrItem>> = flow

        override suspend fun getById(id: Long): QrItem? = list.firstOrNull { it.id == id }

        override suspend fun insert(item: QrItem): Long {
            val copy = item.copy(id = nextId++)
            list.add(copy)
            flow.value = list.toList()
            return copy.id
        }

        override suspend fun update(item: QrItem) {
            val index = list.indexOfFirst { it.id == item.id }
            if (index >= 0) list[index] = item
            flow.value = list.toList()
        }

        override suspend fun delete(item: QrItem) {
            list.removeAll { it.id == item.id }
            flow.value = list.toList()
        }
    }

    private class FakeStorage : QrImageStorage {
        val imported = mutableListOf<String>()
        val deleted = mutableListOf<String>()
        var failImport = false

        override fun fileFor(name: String): File = File(name)

        override suspend fun importImage(uri: Uri): String {
            if (failImport) throw IOException("import failed")
            val name = "stored_${imported.size}.jpg"
            imported += name
            return name
        }

        override fun shareUriFor(name: String): Uri = Uri.EMPTY

        override suspend fun deleteImage(name: String) {
            deleted += name
        }
    }

    private fun item(
        id: Long = 0L,
        imagePath: String = "img.jpg",
        name: String = "Name",
    ) = QrItem(id = id, name = name, provider = "Provider", imagePath = imagePath)

    @Test
    fun `addQr imports image and inserts item`() = runBlocking {
        val dao = FakeDao()
        val storage = FakeStorage()
        val repository = QrRepository(dao, storage)

        val result = repository.addQr(Uri.parse("content://p"), "My QR", "Maybank", "Main")

        assertTrue(result.isSuccess)
        val saved = dao.items().single()
        assertEquals("My QR", saved.name)
        assertEquals("Maybank", saved.provider)
        assertEquals("Main", saved.description)
        assertEquals("stored_0.jpg", saved.imagePath)
        assertTrue(saved.createdAt > 0)
        assertEquals(saved.createdAt, saved.updatedAt)
        assertEquals(listOf("stored_0.jpg"), storage.imported)
    }

    @Test
    fun `addQr trims fields and drops blank description`() = runBlocking {
        val dao = FakeDao()
        val storage = FakeStorage()
        val repository = QrRepository(dao, storage)

        val result = repository.addQr(Uri.parse("content://p"), "  My QR  ", "  Maybank ", "   ")

        assertTrue(result.isSuccess)
        val saved = dao.items().single()
        assertEquals("My QR", saved.name)
        assertEquals("Maybank", saved.provider)
        assertEquals(null, saved.description)
    }

    @Test
    fun `addQr failure does not insert and cleans up imported image`() = runBlocking {
        val dao = FakeDao()
        val storage = FakeStorage().apply { failImport = true }
        val repository = QrRepository(dao, storage)

        val result = repository.addQr(Uri.parse("content://p"), "My QR", "Maybank", null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ImageImportException)
        assertTrue(dao.items().isEmpty())
    }

    @Test
    fun `updateQr updates fields and timestamp`() = runBlocking {
        val dao = FakeDao()
        val storage = FakeStorage()
        val repository = QrRepository(dao, storage)
        val original = item(id = 5, imagePath = "old.jpg")
        dao.store(listOf(original))

        val result = repository.updateQr(
            original,
            newImageUri = null,
            name = "Renamed",
            provider = "BCA",
            description = "New desc",
        )

        assertTrue(result.isSuccess)
        val saved = dao.items().single()
        assertEquals("Renamed", saved.name)
        assertEquals("BCA", saved.provider)
        assertEquals("New desc", saved.description)
        assertEquals("old.jpg", saved.imagePath)
        assertTrue(saved.updatedAt >= original.updatedAt)
        assertTrue(storage.imported.isEmpty())
    }

    @Test
    fun `updateQr with new image imports it and deletes the old one`() = runBlocking {
        val dao = FakeDao()
        val storage = FakeStorage()
        val repository = QrRepository(dao, storage)
        val original = item(id = 7, imagePath = "old.jpg")
        dao.store(listOf(original))

        val result = repository.updateQr(
            original,
            newImageUri = Uri.parse("content://new"),
            name = "Name",
            provider = "Provider",
            description = null,
        )

        assertTrue(result.isSuccess)
        val saved = dao.items().single()
        assertEquals("stored_0.jpg", saved.imagePath)
        assertEquals(listOf("old.jpg"), storage.deleted)
    }

    @Test
    fun `updateQr with failing import keeps old image`() = runBlocking {
        val dao = FakeDao()
        val storage = FakeStorage().apply { failImport = true }
        val repository = QrRepository(dao, storage)
        val original = item(id = 9, imagePath = "old.jpg")
        dao.store(listOf(original))

        val result = repository.updateQr(
            original,
            newImageUri = Uri.parse("content://new"),
            name = "Name",
            provider = "Provider",
            description = null,
        )

        assertTrue(result.isFailure)
        assertEquals("old.jpg", dao.items().single().imagePath)
        assertTrue(storage.deleted.isEmpty())
    }

    @Test
    fun `deleteQr removes record and image`() = runBlocking {
        val dao = FakeDao()
        val storage = FakeStorage()
        val repository = QrRepository(dao, storage)
        val original = item(id = 3, imagePath = "gone.jpg")
        dao.store(listOf(original))

        val result = repository.deleteQr(original)

        assertTrue(result.isSuccess)
        assertTrue(dao.items().isEmpty())
        assertEquals(listOf("gone.jpg"), storage.deleted)
    }

    @Test
    fun `getById returns matching item`() = runBlocking {
        val dao = FakeDao()
        val storage = FakeStorage()
        val repository = QrRepository(dao, storage)
        dao.store(listOf(item(id = 1), item(id = 2)))

        val found = repository.getById(2)
        assertEquals(2L, found?.id)
        val missing = repository.getById(99)
        assertFalse(missing != null)
    }
}