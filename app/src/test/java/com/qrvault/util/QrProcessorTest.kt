package com.qrvault.util

import com.qrvault.data.local.database.QrItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QrProcessorTest {

    private fun item(
        id: Long,
        name: String,
        provider: String,
        description: String? = null,
        createdAt: Long = 0L,
        updatedAt: Long = 0L,
    ) = QrItem(
        id = id,
        name = name,
        provider = provider,
        description = description,
        imagePath = "img_$id.jpg",
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    @Test
    fun `matches returns true for blank query`() {
        assertTrue(QrProcessor.matches(item(1, "A", "B"), ""))
        assertTrue(QrProcessor.matches(item(1, "A", "B"), "   "))
    }

    @Test
    fun `matches searches name case-insensitively`() {
        assertTrue(QrProcessor.matches(item(1, "Personal Maybank QR", "Maybank"), "maybank"))
    }

    @Test
    fun `matches searches provider`() {
        assertTrue(QrProcessor.matches(item(1, "Blue", "BCA"), "bca"))
    }

    @Test
    fun `matches searches description`() {
        assertTrue(
            QrProcessor.matches(item(1, "Blue", "BCA", description = "Main payment QR"), "payment"),
        )
    }

    @Test
    fun `matches returns false for unrelated query`() {
        assertTrue(!QrProcessor.matches(item(1, "Blue", "BCA"), "zzz"))
    }

    @Test
    fun `apply filters and sorts by name a-z`() {
        val items = listOf(
            item(1, "Zebra", "C", createdAt = 3),
            item(2, "apple", "A", createdAt = 1),
            item(3, "Mango", "B", createdAt = 2),
        )
        val result = QrProcessor.apply(items, "", SortOption.NAME_A_Z)
        assertEquals(listOf("apple", "Mango", "Zebra"), result.map { it.name })
    }

    @Test
    fun `apply sorts by name z-a`() {
        val items = listOf(
            item(1, "Zebra", "C"),
            item(2, "apple", "A"),
            item(3, "Mango", "B"),
        )
        val result = QrProcessor.apply(items, "", SortOption.NAME_Z_A)
        assertEquals(listOf("Zebra", "Mango", "apple"), result.map { it.name })
    }

    @Test
    fun `apply sorts by recently added`() {
        val items = listOf(
            item(1, "old", "A", createdAt = 100),
            item(2, "new", "B", createdAt = 200),
            item(3, "newest", "C", createdAt = 300),
        )
        val result = QrProcessor.apply(items, "", SortOption.RECENT_ADDED)
        assertEquals(listOf(3L, 2L, 1L), result.map { it.id })
    }

    @Test
    fun `apply sorts by recently updated`() {
        val items = listOf(
            item(1, "a", "A", updatedAt = 100),
            item(2, "b", "B", updatedAt = 300),
            item(3, "c", "C", updatedAt = 200),
        )
        val result = QrProcessor.apply(items, "", SortOption.RECENT_UPDATED)
        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `apply sorts by provider a-z then name`() {
        val items = listOf(
            item(1, "z", "Bank A"),
            item(2, "a", "Bank B"),
            item(3, "m", "Bank A"),
        )
        val result = QrProcessor.apply(items, "", SortOption.PROVIDER_A_Z)
        assertEquals(listOf(3L, 1L, 2L), result.map { it.id })
    }

    @Test
    fun `apply combines search and sort`() {
        val items = listOf(
            item(1, "Maybank Plan", "Maybank"),
            item(2, "BCA Everyday", "BCA"),
            item(3, "Maybank Promotion", "Maybank", description = "special"),
        )
        val result = QrProcessor.apply(items, "maybank", SortOption.NAME_A_Z)
        assertEquals(listOf(1L, 3L), result.map { it.id })
    }
}