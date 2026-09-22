package com.qrvault.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QrItemDao {

    @Query("SELECT * FROM qr_items")
    fun observeAll(): Flow<List<QrItem>>

    @Query("SELECT * FROM qr_items WHERE id = :id")
    suspend fun getById(id: Long): QrItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QrItem): Long

    @Update
    suspend fun update(item: QrItem)

    @Delete
    suspend fun delete(item: QrItem)
}