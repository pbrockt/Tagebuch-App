package com.pbrockt.tagebuch.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Eine einzelne Seite eines Tagebucheintrags.
 *
 * Ein DiaryDay kann mehrere DiaryPages haben (mehrere Seiten pro Tag).
 * Jede Seite hat einen Textinhalt, optionale Medien (Bilder/Emojis)
 * und eine Hintergrundfarbe.
 *
 * ForeignKey: Verknüpft diese Tabelle mit diary_days über das Datum.
 * onDelete = CASCADE bedeutet: wird ein Tag gelöscht, werden alle
 * zugehörigen Seiten automatisch mitgelöscht.
 *
 * Index: Macht Datenbankabfragen nach dayDate schneller.
 */
@Serializable
@Entity(
    tableName = "diary_pages",
    foreignKeys = [ForeignKey(
        entity = DiaryDay::class,
        parentColumns = ["date"],
        childColumns = ["dayDate"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("dayDate")]
)
data class DiaryPage(
    /** Eindeutige ID — wird als UUID generiert (z.B. "a3f2c1d0-...") */
    @PrimaryKey val id: String,

    /** Das Datum des übergeordneten DiaryDay — Fremdschlüssel */
    val dayDate: String,

    /** Position dieser Seite innerhalb des Tages (0 = erste Seite) */
    val pageIndex: Int = 0,

    /**
     * Der Textinhalt als JSON-String im Format FormattedContent.
     * Enthält den Klartext und Formatierungs-Spans (Fett, Kursiv, Farben).
     */
    val content: String = "",

    /**
     * Medien-Elemente als JSON-Array von PageMedia-Objekten.
     * Enthält Position, Größe und Rotation jedes Bildes/Emojis.
     */
    val mediaJson: String = "[]",

    /** Optionale Hintergrundfarbe als Hex-String, z.B. "#FFF9C4". null = Standard */
    val pageColor: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
