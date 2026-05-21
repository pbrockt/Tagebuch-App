package com.pbrockt.tagebuch.di

import android.content.Context
import com.pbrockt.tagebuch.data.local.TagebuchDatabase
import com.pbrockt.tagebuch.data.local.crypto.CryptoManager
import com.pbrockt.tagebuch.data.local.dao.DiaryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-Modul: Erklärt dem Dependency-Injection-Framework wie es bestimmte
 * Objekte erstellen soll.
 *
 * Warum brauchen wir das? Hilt kann nicht automatisch wissen wie es
 * TagebuchDatabase erstellen soll (die braucht Context und ein Passwort).
 * Hier beschreiben wir die Rezepte.
 *
 * @Module: Diese Klasse ist ein Hilt-Modul mit Erstellungs-Rezepten.
 * @InstallIn(SingletonComponent): Die bereitgestellten Objekte leben so lange
 * wie die App — werden nur einmal erstellt und dann wiederverwendet.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Rezept für die Datenbank.
     *
     * @Provides: Diese Methode wird aufgerufen wenn jemand eine
     * TagebuchDatabase benötigt und noch keine existiert.
     *
     * @Singleton: Nur eine Datenbankinstanz für die gesamte App-Laufzeit.
     * Mehrere Instanzen würden zu Konflikten führen.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager  // Hilt injiziert CryptoManager automatisch
    ): TagebuchDatabase {
        // Passwort aus dem Android Keystore holen — nie im Klartext gespeichert
        val passphrase = cryptoManager.generateDbPassphrase()
        return TagebuchDatabase.create(context, passphrase)
    }

    /**
     * Rezept für das DiaryDao.
     * Room generiert die Implementierung des Interfaces automatisch.
     */
    @Provides
    @Singleton
    fun provideDiaryDao(db: TagebuchDatabase): DiaryDao = db.diaryDao()
}
