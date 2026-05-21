package com.pbrockt.tagebuch.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Mögliche Zustände des Authentifizierungs-Bildschirms.
 *
 * sealed class: Alle möglichen Zustände sind hier aufgelistet.
 * Der Compiler warnt wenn ein when-Ausdruck einen Zustand vergisst.
 */
sealed class AuthState {
    object Idle : AuthState()                        // Wartet auf Eingabe
    object Success : AuthState()                     // PIN war korrekt
    data class WrongPin(val attemptsLeft: Int) : AuthState()  // PIN falsch, Versuche übrig
    data class LockedOut(val secondsLeft: Int) : AuthState()  // Gesperrt nach zu vielen Versuchen
    object NoAuth : AuthState()                      // Kein PIN konfiguriert — direkt rein
}

/**
 * ViewModel für die PIN-Eingabe.
 *
 * Enthält die Geschäftslogik für:
 * - PIN-Verifikation via SHA-256 Hash-Vergleich
 * - Versuchslimit (5 Versuche, dann 30 Sekunden Sperre)
 * - Countdown-Timer für die Sperrzeit
 */
@HiltViewModel
class AuthViewModel @Inject constructor(private val prefs: SecurePrefs) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private var attemptCount = 0       // Anzahl Fehlversuche seit letztem Erfolg
    private var lockedUntil = 0L       // Zeitstempel bis wann die App gesperrt ist
    private var countdownJob: Job? = null  // Hintergrund-Job für den Countdown

    init {
        // Wenn kein PIN gesetzt ist, direkt als "erfolgreich authentifiziert" markieren
        if (prefs.authMethod == SecurePrefs.AUTH_NONE) {
            _state.value = AuthState.NoAuth
        }
    }

    /**
     * Überprüft den eingegebenen PIN.
     *
     * Warum wird der PIN gehasht?
     * Der PIN selbst wird nie gespeichert — nur sein SHA-256 Hash.
     * Ein Hash ist eine Einwegfunktion: Man kann den PIN nicht aus dem Hash
     * zurückrechnen. Selbst wenn jemand die SecurePrefs ausliest,
     * bekommt er nur den Hash, nicht den PIN.
     *
     * @param pin Der vierstellige PIN als String
     */
    fun verifyPin(pin: String) {
        val now = System.currentTimeMillis()

        // Prüfen ob die Sperre noch aktiv ist
        if (now < lockedUntil) {
            startCountdown()
            return
        }

        val hash = sha256(pin)
        if (hash == prefs.pinHash) {
            // Korrekt! Zähler zurücksetzen und Erfolg melden
            attemptCount = 0
            _state.value = AuthState.Success
        } else {
            attemptCount++
            if (attemptCount >= 5) {
                // 5 Fehlversuche: 30 Sekunden sperren
                lockedUntil = now + 30_000L
                attemptCount = 0
                startCountdown()
            } else {
                _state.value = AuthState.WrongPin(5 - attemptCount)
            }
        }
    }

    /**
     * Startet einen Countdown der jede Sekunde die verbleibende Sperrzeit aktualisiert.
     * viewModelScope.launch erstellt eine Coroutine die an den ViewModel-Lifecycle
     * gebunden ist — wird automatisch beendet wenn das ViewModel zerstört wird.
     */
    private fun startCountdown() {
        countdownJob?.cancel()  // Alten Countdown beenden falls vorhanden
        countdownJob = viewModelScope.launch {
            while (true) {
                val remaining = ((lockedUntil - System.currentTimeMillis()) / 1000).toInt()
                if (remaining <= 0) {
                    _state.value = AuthState.Idle  // Sperre abgelaufen
                    break
                }
                _state.value = AuthState.LockedOut(remaining)
                delay(1000)  // Eine Sekunde warten
            }
        }
    }

    /**
     * Berechnet den SHA-256 Hash eines Strings.
     *
     * MessageDigest ist die Standard-Java-Klasse für kryptografische Hashfunktionen.
     * Das Ergebnis ist ein Hexadezimal-String (64 Zeichen für SHA-256).
     */
    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }  // Bytes → Hex-String
    }
}
