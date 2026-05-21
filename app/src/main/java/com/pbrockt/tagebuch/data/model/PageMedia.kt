package com.pbrockt.tagebuch.data.model

import kotlinx.serialization.Serializable

/**
 * Ein einzelnes Medium (Bild oder Emoji) auf einer Tagebuch-Seite.
 *
 * Medien werden frei auf der Seite platziert und können verschoben,
 * skaliert und gedreht werden. Die Position ist relativ zum Canvas-Bereich.
 *
 * Diese Klasse wird nicht direkt in der Datenbank gespeichert — stattdessen
 * wird sie als JSON in DiaryPage.mediaJson serialisiert.
 */
@Serializable
data class PageMedia(
    /** Eindeutige ID dieses Mediums */
    val id: String,

    /** Typ: Bild aus der Galerie oder Emoji */
    val type: MediaType,

    /**
     * Für Bilder: Content-URI als String (z.B. "content://media/...").
     * Für Emojis: Das Emoji-Zeichen selbst (z.B. "😊").
     */
    val uri: String,

    /** Horizontale Position in Pixeln vom linken Canvas-Rand */
    val positionX: Float = 0f,

    /** Vertikale Position in Pixeln vom oberen Canvas-Rand */
    val positionY: Float = 0f,

    /** Skalierungsfaktor: 1.0 = Originalgröße, 2.0 = doppelt so groß */
    val scale: Float = 1f,

    /** Drehwinkel in Grad (0–360) */
    val rotation: Float = 0f,

    /** Breite des Mediums in Pixeln (vor Skalierung) */
    val width: Float = 200f,

    /** Höhe des Mediums in Pixeln (vor Skalierung) */
    val height: Float = 200f
)

/** Unterscheidet zwischen Fotos aus der Galerie und eingetippten Emojis */
@Serializable
enum class MediaType { IMAGE, EMOJI }
