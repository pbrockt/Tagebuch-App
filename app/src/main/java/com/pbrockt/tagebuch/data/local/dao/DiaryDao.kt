package com.pbrockt.tagebuch.data.local.dao

import androidx.room.*
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    // DiaryDay
    @Query("SELECT * FROM diary_days ORDER BY date DESC")
    fun getAllDays(): Flow<List<DiaryDay>>

    @Query("SELECT date FROM diary_days")
    fun getAllDates(): Flow<List<String>>

    @Query("SELECT * FROM diary_days WHERE date = :date LIMIT 1")
    suspend fun getDayByDate(date: String): DiaryDay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: DiaryDay)

    @Delete
    suspend fun deleteDay(day: DiaryDay)

    // DiaryPage
    @Query("SELECT * FROM diary_pages WHERE dayDate = :date ORDER BY pageIndex ASC")
    fun getPagesForDay(date: String): Flow<List<DiaryPage>>

    @Query("SELECT * FROM diary_pages WHERE id = :id LIMIT 1")
    suspend fun getPageById(id: String): DiaryPage?

    @Query("SELECT * FROM diary_pages WHERE syncedAt IS NULL OR updatedAt > syncedAt")
    suspend fun getUnsyncedPages(): List<DiaryPage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPage(page: DiaryPage)

    @Delete
    suspend fun deletePage(page: DiaryPage)

    @Query("DELETE FROM diary_pages WHERE dayDate = :date")
    suspend fun deletePagesForDay(date: String)

    @Query("UPDATE diary_pages SET syncedAt = :timestamp WHERE id = :id")
    suspend fun markPageSynced(id: String, timestamp: Long)
}
