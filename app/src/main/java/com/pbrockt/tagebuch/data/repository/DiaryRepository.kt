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

    fun getPagesForDay(date: String): Flow<List<DiaryPage>> = dao.getPagesForDay(date)

    suspend fun getDayByDate(date: String): DiaryDay? = dao.getDayByDate(date)

    suspend fun createPage(date: String): DiaryPage {
        val day = dao.getDayByDate(date) ?: DiaryDay(date = date).also { dao.upsertDay(it) }
        val existingPages = dao.getUnsyncedPages().filter { it.dayDate == date }
        val nextIndex = existingPages.size
        val page = DiaryPage(
            id = UUID.randomUUID().toString(),
            dayDate = day.date,
            pageIndex = nextIndex
        )
        dao.upsertPage(page)
        return page
    }

    suspend fun savePage(page: DiaryPage) {
        dao.upsertPage(page.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePage(page: DiaryPage) {
        dao.deletePage(page)
    }

    suspend fun deleteDay(date: String) {
        dao.deletePagesForDay(date)
        dao.getDayByDate(date)?.let { dao.deleteDay(it) }
    }
}
