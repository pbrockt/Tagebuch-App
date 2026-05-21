package com.pbrockt.tagebuch.data.contacts

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

/**
 * Hilfsfunktionen für den Zugriff auf Kontakt-Geburtstage.
 *
 * Android ContactsContract ist die API für den Zugriff auf das Adressbuch.
 * Dafür ist die READ_CONTACTS-Berechtigung nötig, die der Nutzer erteilen muss.
 *
 * object statt class: BirthdayHelper ist ein Singleton — keine Instanz nötig,
 * Methoden können direkt aufgerufen werden: BirthdayHelper.getBirthdays(context)
 */
object BirthdayHelper {

    /** Prüft ob die App Zugriff auf Kontakte hat */
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED

    /**
     * Liest alle Kontakt-Geburtstage aus dem Adressbuch.
     *
     * @return Map von "MM-dd" (z.B. "03-15") → Liste von Kontaktnamen an diesem Tag
     *         Ein Tag kann mehrere Geburtstage haben!
     */
    fun getBirthdays(context: Context): Map<String, List<String>> {
        if (!hasPermission(context)) return emptyMap()

        val result = mutableMapOf<String, MutableList<String>>()

        // Welche Felder wir aus der Kontakte-Datenbank abfragen wollen
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        // Nur Geburtstage (TYPE_BIRTHDAY), keine anderen Ereignisse
        val selection = "${ContactsContract.Data.MIMETYPE} = '${ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE}'" +
                " AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ${ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY}"

        try {
            // Datenbankabfrage auf die Kontakte-Datenbank
            context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection, selection, null, null
            )?.use { cursor ->
                val dateIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val rawDate = cursor.getString(dateIdx) ?: continue
                    val name = cursor.getString(nameIdx) ?: "Unbekannt"
                    val mmdd = parseToMmDd(rawDate) ?: continue

                    // Geburtstag zur Map hinzufügen (getOrPut erstellt neue Liste falls nötig)
                    result.getOrPut(mmdd) { mutableListOf() }.add(name)
                }
            }
        } catch (e: Exception) {
            // Kontakte nicht verfügbar — leere Map zurückgeben
        }

        return result
    }

    /**
     * Wandelt verschiedene Datumsformate aus den Kontakten in "MM-dd" um.
     *
     * Kontakte können verschiedene Formate haben:
     * - "1990-03-15" → mit Jahr → "03-15"
     * - "--03-15"    → ohne Jahr (häufigstes Format) → "03-15"
     *
     * @return "MM-dd" oder null wenn das Format nicht erkannt wird
     */
    private fun parseToMmDd(raw: String): String? {
        return when {
            raw.startsWith("--") -> {
                // Format: --MM-dd (kein Jahr bekannt)
                val parts = raw.removePrefix("--").split("-")
                if (parts.size == 2)
                    "${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}"
                else null
            }
            raw.contains("-") -> {
                val parts = raw.split("-")
                when (parts.size) {
                    3 -> "${parts[1].padStart(2, '0')}-${parts[2].padStart(2, '0')}"  // yyyy-MM-dd
                    2 -> "${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}"  // MM-dd
                    else -> null
                }
            }
            else -> null
        }
    }
}
