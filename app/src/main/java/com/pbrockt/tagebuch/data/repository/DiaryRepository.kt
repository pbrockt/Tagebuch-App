package com.pbrockt.tagebuch.data.repository

import com.pbrockt.tagebuch.data.local.dao.DiaryDao
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository für alle Tagebuch-Daten.
 *
 * Das Repository-Muster ist eine Architektur-Konvention:
 * - ViewModels kennen nur das Repository, nicht die Datenbank direkt
 * - Das Repository entscheidet woher Daten kommen (DB, Netzwerk, Cache)
 * - Änderungen an der Datenspeicherung betreffen nur das Repository
 *
 * Vorteile:
 * - ViewModels sind testbarer (man kann das Repository mocken)
 * - Datenbankdetails sind versteckt hinter einer sauberen API
 * - Einheitliche Stelle für Geschäftslogik (z.B. "Tag automatisch erstellen")
 */
@Singleton
class DiaryRepository @Inject constructor(private val dao: DiaryDao) {

    // ===== Lese-Operationen (geben Flow zurück — reaktiv) =====

    fun getAllDates(): Flow<List<String>> = dao.getAllDates()
    fun getAllDays(): Flow<List<DiaryDay>> = dao.getAllDays()
    fun getAllPages(): Flow<List<DiaryPage>> = dao.getAllPages()
    fun getPagesForDay(date: String): Flow<List<DiaryPage>> = dao.getPagesForDay(date)

    /**
     * Sucht in allen Einträgen nach dem Suchbegriff.
     * Die Suche ist case-insensitiv (dank LIKE in SQL).
     */
    fun searchPages(query: String): Flow<List<DiaryPage>> = dao.searchPages(query)

    suspend fun getDayByDate(date: String): DiaryDay? = dao.getDayByDate(date)

    /**
     * Stellt sicher dass ein DiaryDay für das gegebene Datum existiert.
     * Falls nicht, wird automatisch einer erstellt.
     * Gibt den bestehenden oder neuen DiaryDay zurück.
     */
    suspend fun ensureDay(date: String): DiaryDay =
        dao.getDayByDate(date) ?: DiaryDay(date = date).also { dao.upsertDay(it) }

    // ===== Schreib-Operationen =====

    /**
     * Erstellt eine neue Seite für das angegebene Datum.
     * Der übergeordnete Tag wird automatisch erstellt falls er nicht existiert.
     * Die Seitennummer wird automatisch als nächste freie Nummer vergeben.
     */
    suspend fun createPage(date: String): DiaryPage {
        ensureDay(date)
        val pages = dao.getUnsyncedPages().filter { it.dayDate == date }
        val page = DiaryPage(
            id = UUID.randomUUID().toString(),  // Zufällige eindeutige ID
            dayDate = date,
            pageIndex = pages.size  // Nächste Seitennummer
        )
        dao.upsertPage(page)
        return page
    }

    /** Speichert Änderungen an einer Seite und aktualisiert den Zeitstempel */
    suspend fun savePage(page: DiaryPage) {
        dao.upsertPage(page.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePage(page: DiaryPage) = dao.deletePage(page)

    /** Löscht einen Tag und alle zugehörigen Seiten (durch CASCADE automatisch) */
    suspend fun deleteDay(date: String) {
        dao.deletePagesForDay(date)
        dao.getDayByDate(date)?.let { dao.deleteDay(it) }
    }

    suspend fun setMood(date: String, mood: String?) {
        ensureDay(date)  // Tag erstellen falls er noch nicht existiert
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
