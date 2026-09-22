package com.qrvault.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_items")
data class QrItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val provider: String,
    val description: String? = null,
    val imagePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)