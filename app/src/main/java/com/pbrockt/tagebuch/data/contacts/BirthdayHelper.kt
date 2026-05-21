package com.pbrockt.tagebuch.data.contacts

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

object BirthdayHelper {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED

    /**
     * Liest alle Kontakt-Geburtstage aus.
     * @return Map von "MM-dd" → List<Kontaktname>
     */
    fun getBirthdays(context: Context): Map<String, List<String>> {
        if (!hasPermission(context)) return emptyMap()

        val result = mutableMapOf<String, MutableList<String>>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        val selection = "${ContactsContract.Data.MIMETYPE} = '${ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE}'" +
                " AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ${ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY}"

        try {
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
                    result.getOrPut(mmdd) { mutableListOf() }.add(name)
                }
            }
        } catch (e: Exception) {
            // Kontakte nicht verfügbar — leer zurückgeben
        }

        return result
    }

    /**
     * Parst Kontakt-Datumsformate in "MM-dd":
     * - "1990-03-15" → "03-15"
     * - "--03-15"    → "03-15" (kein Jahr)
     */
    private fun parseToMmDd(raw: String): String? {
        return when {
            raw.startsWith("--") -> {
                // Format: --MM-dd
                val parts = raw.removePrefix("--").split("-")
                if (parts.size == 2) "${parts[0].padStart(2,'0')}-${parts[1].padStart(2,'0')}" else null
            }
            raw.contains("-") -> {
                // Format: yyyy-MM-dd oder MM-dd
                val parts = raw.split("-")
                when (parts.size) {
                    3 -> "${parts[1].padStart(2,'0')}-${parts[2].padStart(2,'0')}"
                    2 -> "${parts[0].padStart(2,'0')}-${parts[1].padStart(2,'0')}"
                    else -> null
                }
            }
            else -> null
        }
    }
}
