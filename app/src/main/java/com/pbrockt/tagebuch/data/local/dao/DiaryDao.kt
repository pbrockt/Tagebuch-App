package com.pbrockt.tagebuch.data.local.dao

import androidx.room.*
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) — die Schnittstelle zur Datenbank.
 *
 * Room generiert automatisch die SQL-Implementierung für alle Methoden
 * die mit @Query, @Insert, @Delete etc. annotiert sind.
 *
 * Flow<T>: Ein reaktiver Datenstrom. Wenn sich Datenbankdaten ändern,
 * sendet Room automatisch neue Daten an alle Abonnenten — ohne dass
 * wir die Datenbank manuell neu abfragen müssen.
 *
 * suspend fun: Coroutinen-Funktion die pausieren kann ohne den
 * Haupt-Thread zu blockieren. Ideal für I/O-Operationen wie DB-Zugriffe.
 */
@Dao
interface DiaryDao {

    // ===== DiaryDay-Abfragen =====

    /** Liefert alle Tage als reaktiven Stream, neueste zuerst */
    @Query("SELECT * FROM diary_days ORDER BY date DESC")
    fun getAllDays(): Flow<List<DiaryDay>>

    /** Liefert nur die Datumswerte (effizient wenn wir nur Markierungen im Kalender brauchen) */
    @Query("SELECT date FROM diary_days")
    fun getAllDates(): Flow<List<String>>

    /** Sucht einen bestimmten Tag anhand des Datums */
    @Query("SELECT * FROM diary_days WHERE date = :date LIMIT 1")
    suspend fun getDayByDate(date: String): DiaryDay?

    /** Speichert oder aktualisiert einen Tag (REPLACE = überschreiben wenn vorhanden) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: DiaryDay)

    @Delete
    suspend fun deleteDay(day: DiaryDay)

    /** Aktualisiert nur die Stimmung eines Tages ohne alles andere zu ändern */
    @Query("UPDATE diary_days SET mood = :mood, updatedAt = :ts WHERE date = :date")
    suspend fun updateMood(date: String, mood: String?, ts: Long = System.currentTimeMillis())

    @Query("UPDATE diary_days SET weather = :weather, updatedAt = :ts WHERE date = :date")
    suspend fun updateWeather(date: String, weather: String?, ts: Long = System.currentTimeMillis())

    @Query("UPDATE diary_days SET period = :period, updatedAt = :ts WHERE date = :date")
    suspend fun updatePeriod(date: String, period: String?, ts: Long = System.currentTimeMillis())

    // ===== DiaryPage-Abfragen =====

    /** Alle Seiten eines bestimmten Tags, sortiert nach Seitennummer */
    @Query("SELECT * FROM diary_pages WHERE dayDate = :date ORDER BY pageIndex ASC")
    fun getPagesForDay(date: String): Flow<List<DiaryPage>>

    /** Alle Seiten aller Tage — für Statistiken und Suche */
    @Query("SELECT * FROM diary_pages ORDER BY updatedAt DESC")
    fun getAllPages(): Flow<List<DiaryPage>>

    /**
     * Volltext-Suche: Findet alle Seiten die den Suchbegriff enthalten.
     * LIKE '%term%' sucht den Begriff an jeder Position im Text.
     */
    @Query("SELECT * FROM diary_pages WHERE content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchPages(query: String): Flow<List<DiaryPage>>

    @Query("SELECT * FROM diary_pages WHERE id = :id LIMIT 1")
    suspend fun getPageById(id: String): DiaryPage?

    /**
     * Findet alle Seiten die noch nicht synchronisiert wurden oder
     * nach der letzten Synchronisation geändert wurden.
     */
    @Query("SELECT * FROM diary_pages WHERE syncedAt IS NULL OR updatedAt > syncedAt")
    suspend fun getUnsyncedPages(): List<DiaryPage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPage(page: DiaryPage)

    @Delete
    suspend fun deletePage(page: DiaryPage)

    @Query("DELETE FROM diary_pages WHERE dayDate = :date")
    suspend fun deletePagesForDay(date: String)

    /** Markiert eine Seite als erfolgreich synchronisiert */
    @Query("UPDATE diary_pages SET syncedAt = :timestamp WHERE id = :id")
    suspend fun markPageSynced(id: String, timestamp: Long)
}
