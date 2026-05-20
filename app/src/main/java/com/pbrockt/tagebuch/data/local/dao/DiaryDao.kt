package com.pbrockt.tagebuch.data.local.dao

import androidx.room.*
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

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

    @Query("UPDATE diary_days SET mood = :mood, updatedAt = :ts WHERE date = :date")
    suspend fun updateMood(date: String, mood: String?, ts: Long = System.currentTimeMillis())

    @Query("UPDATE diary_days SET weather = :weather, updatedAt = :ts WHERE date = :date")
    suspend fun updateWeather(date: String, weather: String?, ts: Long = System.currentTimeMillis())

    @Query("SELECT * FROM diary_pages WHERE dayDate = :date ORDER BY pageIndex ASC")
    fun getPagesForDay(date: String): Flow<List<DiaryPage>>

    @Query("SELECT * FROM diary_pages ORDER BY updatedAt DESC")
    fun getAllPages(): Flow<List<DiaryPage>>

    @Query("SELECT * FROM diary_pages WHERE content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchPages(query: String): Flow<List<DiaryPage>>

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
