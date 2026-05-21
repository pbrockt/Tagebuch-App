package com.pbrockt.tagebuch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pbrockt.tagebuch.data.local.dao.DiaryDao
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import net.sqlcipher.database.SupportFactory

/**
 * Die verschlüsselte SQLite-Datenbank der App.
 *
 * Room ist Googles Abstraktion über SQLite — es generiert automatisch
 * den Datenbankzugriffs-Code aus unseren Klassen und Interfaces.
 *
 * SQLCipher verschlüsselt die gesamte .db-Datei mit AES-256.
 * Ohne das richtige Passwort ist die Datei komplett unlesbar —
 * auch wenn jemand physischen Zugriff auf das Gerät hat.
 *
 * version = 3: Wenn wir das Datenbankschema ändern (neue Felder, Tabellen),
 * erhöhen wir die Version. Bei fallbackToDestructiveMigration() wird die
 * alte Datenbank gelöscht und neu erstellt (Daten gehen verloren!).
 * Für Produktions-Apps sollte man stattdessen Migration-Skripte schreiben.
 */
@Database(
    entities = [DiaryDay::class, DiaryPage::class],
    version = 3,
    exportSchema = false  // Kein Schema-Export in eine Datei — vereinfacht das Projekt
)
abstract class TagebuchDatabase : RoomDatabase() {

    /** Room generiert die Implementierung dieses abstrakten DAO automatisch */
    abstract fun diaryDao(): DiaryDao

    companion object {
        /**
         * Erstellt die verschlüsselte Datenbankinstanz.
         *
         * @param context Android-Kontext für den Dateipfad
         * @param passphrase 32-Byte Schlüssel aus dem Android Keystore —
         *                   wird nie direkt angezeigt oder gespeichert
         */
        fun create(context: Context, passphrase: ByteArray): TagebuchDatabase {
            // SupportFactory übergibt das Passwort an SQLCipher
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                TagebuchDatabase::class.java,
                "tagebuch.db"  // Dateiname im internen App-Speicher
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration() // Bei Schema-Änderung: neu aufbauen
                .build()
        }
    }
}
