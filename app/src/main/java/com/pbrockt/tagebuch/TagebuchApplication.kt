package com.pbrockt.tagebuch

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.pbrockt.tagebuch.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Der Einstiegspunkt der gesamten Anwendung.
 *
 * Diese Klasse wird von Android genau einmal gestartet — bevor irgendein
 * Activity oder Service läuft. Sie lebt so lange wie der gesamte App-Prozess.
 *
 * @HiltAndroidApp: Diese Annotation aktiviert Hilt (das Dependency-Injection-
 * Framework). Hilt erstellt danach automatisch alle @Inject-annotierten
 * Objekte und verwaltet deren Lebenszeit.
 *
 * Was ist Dependency Injection? Statt Objekte selbst zu erstellen
 * (val db = TagebuchDatabase.create(...)) sagt man Hilt "ich brauche eine
 * Datenbank" und Hilt kümmert sich um die Erstellung und Weitergabe.
 */
@HiltAndroidApp
class TagebuchApplication : Application() {

    /**
     * @Inject: Hilt stellt automatisch eine Instanz von AppLockManager bereit.
     * Wir müssen ihn nicht selbst erstellen — Hilt erledigt das.
     */
    @Inject lateinit var appLockManager: AppLockManager

    /**
     * BroadcastReceiver lauscht auf System-Ereignisse.
     * Hier: Der Bildschirm wird ausgeschaltet (ACTION_SCREEN_OFF).
     *
     * Warum in der Application und nicht in der Activity?
     * → Die Application lebt immer, auch wenn keine Activity sichtbar ist.
     *   So wird der Receiver nie ausgehängt und verpasst keine Ereignisse.
     *
     * ::appLockManager.isInitialized prüft ob Hilt die Injection bereits
     * abgeschlossen hat — Sicherheitsnetz gegen seltene Timing-Probleme.
     */
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                if (::appLockManager.isInitialized) {
                    appLockManager.lockApp()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Nach super.onCreate() ist Hilt fertig mit der Injection
        NotificationHelper.createChannel(this)
        // Receiver permanent registrieren — bleibt aktiv für die gesamte App-Laufzeit
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }
}
