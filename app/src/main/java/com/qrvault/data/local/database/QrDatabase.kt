package com.qrvault.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QrItem::class],
    version = 1,
    exportSchema = false,
)
abstract class QrDatabase : RoomDatabase() {

    abstract fun qrItemDao(): QrItemDao
}