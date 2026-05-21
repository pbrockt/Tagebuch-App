package com.pbrockt.tagebuch.data.repository

import com.pbrockt.tagebuch.data.local.dao.DiaryDao
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(private val dao: DiaryDao) {

    fun getAllDates(): Flow<List<String>> = dao.getAllDates()
    fun getAllDays(): Flow<List<DiaryDay>> = dao.getAllDays()
    fun getAllPages(): Flow<List<DiaryPage>> = dao.getAllPages()
    fun getPagesForDay(date: String): Flow<List<DiaryPage>> = dao.getPagesForDay(date)
    fun searchPages(query: String): Flow<List<DiaryPage>> = dao.searchPages(query)

    suspend fun getDayByDate(date: String): DiaryDay? = dao.getDayByDate(date)

    suspend fun ensureDay(date: String): DiaryDay =
        dao.getDayByDate(date) ?: DiaryDay(date = date).also { dao.upsertDay(it) }

    suspend fun createPage(date: String): DiaryPage {
        ensureDay(date)
        val pages = dao.getUnsyncedPages().filter { it.dayDate == date }
        val page = DiaryPage(
            id = UUID.randomUUID().toString(),
            dayDate = date,
            pageIndex = pages.size
        )
        dao.upsertPage(page)
        return page
    }

    suspend fun savePage(page: DiaryPage) {
        dao.upsertPage(page.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePage(page: DiaryPage) = dao.deletePage(page)

    suspend fun deleteDay(date: String) {
        dao.deletePagesForDay(date)
        dao.getDayByDate(date)?.let { dao.deleteDay(it) }
    }

    suspend fun setMood(date: String, mood: String?) {
        ensureDay(date)
        dao.updateMood(date, mood)
    }

    suspend fun setWeather(date: String, weather: String?) {
        ensureDay(date)
        dao.updateWeather(date, weather)
    }

    suspend fun setPeriod(date: String, period: String?) {
        ensureDay(date)
        dao.updatePeriod(date, period)
    }
}
