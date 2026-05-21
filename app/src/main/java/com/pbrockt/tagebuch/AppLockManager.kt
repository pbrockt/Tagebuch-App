package com.pbrockt.tagebuch

import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verwaltet den Sperr-Zustand der App.
 *
 * Dieses Singleton (es gibt immer nur eine Instanz) entscheidet, ob der
 * PIN-Screen angezeigt wird oder die echte App. Der Zustand wird als
 * StateFlow bereitgestellt — Compose-UIs können ihn beobachten und
 * reagieren sofort wenn sich der Wert ändert.
 *
 * @Singleton bedeutet: Hilt erstellt diese Klasse genau einmal für die gesamte
 * App-Laufzeit. Egal von wo aus darauf zugegriffen wird — es ist immer
 * die gleiche Instanz.
 */
@Singleton
class AppLockManager @Inject constructor(private val prefs: SecurePrefs) {

    /**
     * Interner, veränderbarer Zustand: true = gesperrt, false = entsperrt.
     *
     * MutableStateFlow ist ein "Datenstrom" der immer den aktuellen Wert
     * enthält und Beobachter sofort informiert wenn er sich ändert.
     *
     * Startwert: gesperrt wenn ein PIN konfiguriert ist.
     */
    private val _isLocked = MutableStateFlow(prefs.authMethod != SecurePrefs.AUTH_NONE)

    /**
     * Öffentlich lesbare Version des Sperr-Zustands.
     * Nur AppLockManager selbst kann schreiben (über _isLocked),
     * alle anderen können nur lesen.
     */
    val isLocked: StateFlow<Boolean> = _isLocked

    /**
     * Gibt zurück ob die App überhaupt eine Authentifizierung erfordert.
     * Wenn kein PIN gesetzt ist, wird nie gesperrt.
     */
    val requiresAuth: Boolean
        get() = prefs.authMethod != SecurePrefs.AUTH_NONE

    /**
     * Sperrt die App — zeigt beim nächsten Öffnen den PIN-Screen.
     * Wird aufgerufen wenn die App in den Hintergrund geht (onStop)
     * oder der Bildschirm ausgeschaltet wird.
     */
    fun lockApp() {
        if (requiresAuth) _isLocked.value = true
    }

    /**
     * Entsperrt die App nach erfolgreicher PIN-Eingabe.
     * Wird von AuthScreen aufgerufen wenn der PIN korrekt war.
     */
    fun unlockApp() {
        _isLocked.value = false
    }
}
