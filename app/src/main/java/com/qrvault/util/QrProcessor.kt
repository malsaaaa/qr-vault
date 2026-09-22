package com.qrvault.util

import com.qrvault.data.local.database.QrItem
import java.util.Locale

object QrProcessor {

    fun normalize(query: String): String = query.trim().lowercase(Locale.ROOT)

    fun matches(item: QrItem, query: String): Boolean {
        val q = normalize(query)
        if (q.isEmpty()) return true
        if (item.name.contains(q, ignoreCase = true)) return true
        if (item.provider.contains(q, ignoreCase = true)) return true
        return item.description?.contains(q, ignoreCase = true) ?: false
    }

    fun apply(items: List<QrItem>, query: String, sort: SortOption): List<QrItem> {
        val filtered = items.filter { matches(it, query) }
        return filtered.sortedWith(comparator(sort))
    }

    private fun comparator(sort: SortOption): Comparator<QrItem> = when (sort) {
        SortOption.RECENT_ADDED -> compareByDescending<QrItem> { it.createdAt }
            .thenByDescending { it.id }
        SortOption.RECENT_UPDATED -> compareByDescending<QrItem> { it.updatedAt }
            .thenByDescending { it.id }
        SortOption.NAME_A_Z -> compareBy(String.CASE_INSENSITIVE_ORDER, QrItem::name)
            .thenBy(String.CASE_INSENSITIVE_ORDER, QrItem::provider)
        SortOption.NAME_Z_A -> compareByDescending(String.CASE_INSENSITIVE_ORDER, QrItem::name)
            .thenBy(String.CASE_INSENSITIVE_ORDER, QrItem::provider)
        SortOption.PROVIDER_A_Z -> compareBy(String.CASE_INSENSITIVE_ORDER, QrItem::provider)
            .thenBy(String.CASE_INSENSITIVE_ORDER, QrItem::name)
    }
}