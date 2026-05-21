package com.pbrockt.tagebuch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Datenmodell für einen einzelnen Kalendertag.
 *
 * @Entity: Room speichert diese Klasse als Tabelle "diary_days" in der SQLite-Datenbank.
 * Jedes Objekt dieser Klasse = eine Zeile in der Tabelle.
 *
 * @Serializable: Diese Klasse kann in JSON umgewandelt werden (und zurück).
 * Das brauchen wir für den WebDAV-Sync — Daten werden als JSON verschlüsselt
 * auf dem Server gespeichert.
 *
 * data class: Kotlin generiert automatisch equals(), hashCode() und toString().
 * Ideal für Datencontainer.
 */
@Serializable
@Entity(tableName = "diary_days")
data class DiaryDay(
    /** Primärschlüssel: Das Datum im Format "2024-05-21" (ISO-8601). */
    @PrimaryKey val date: String,

    /** Stimmung des Tages: "great", "good", "okay", "bad", "awful" oder null */
    val mood: String? = null,

    /** Wetter: "sunny", "cloudy", "rainy", "snowy", "stormy" oder null */
    val weather: String? = null,

    /** Perioden-Status: "start", "active", "severe", "end" oder null */
    val period: String? = null,

    /** Zeitstempel der Erstellung in Millisekunden seit 1.1.1970 */
    val createdAt: Long = System.currentTimeMillis(),

    /** Zeitstempel der letzten Änderung — wird beim WebDAV-Sync für Konflikterkennung genutzt */
    val updatedAt: Long = System.currentTimeMillis(),

    /** Wann dieser Eintrag zuletzt mit WebDAV synchronisiert wurde. null = noch nie */
    val syncedAt: Long? = null
)
