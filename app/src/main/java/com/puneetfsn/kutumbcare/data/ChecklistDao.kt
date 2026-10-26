package com.puneetfsn.kutumbcare.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items ORDER BY sortOrder, title")
    fun observeAll(): Flow<List<ChecklistItem>>

    @Query("SELECT * FROM checklist_items ORDER BY sortOrder, title")
    suspend fun getAll(): List<ChecklistItem>

    @Query("SELECT * FROM checklist_items WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ChecklistItem?>

    @Query("SELECT * FROM checklist_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ChecklistItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ChecklistItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ChecklistItem>)

    @Delete
    suspend fun delete(item: ChecklistItem)

    @Query("DELETE FROM checklist_items")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM checklist_items")
    suspend fun count(): Int
}
