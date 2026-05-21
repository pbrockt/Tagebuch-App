package com.pbrockt.tagebuch

import androidx.lifecycle.ViewModel
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel für globale App-Einstellungen (Theme, Schriftart, Kalender-Modus).
 *
 * Was ist ein ViewModel?
 * → Es speichert UI-Daten und überlebt Konfigurationsänderungen wie
 *   Bildschirmrotationen. Die Activity wird bei Rotation neu erstellt,
 *   das ViewModel bleibt erhalten — keine Datenverluste.
 *
 * @HiltViewModel: Hilt erstellt und injiziert dieses ViewModel automatisch.
 */
@HiltViewModel
class MainViewModel @Inject constructor(private val prefs: SecurePrefs) : ViewModel() {

    // StateFlow: Datenstrom der immer den aktuellen Wert hält.
    // Compose-UIs abonnieren diesen und reagieren auf Änderungen.
    private val _themeChoice = MutableStateFlow(prefs.themeChoice)
    val themeChoice: StateFlow<String> = _themeChoice

    private val _accentColor = MutableStateFlow(prefs.accentColor)
    val accentColor: StateFlow<String> = _accentColor

    private val _calendarIconMode = MutableStateFlow(prefs.calendarIconMode)
    val calendarIconMode: StateFlow<String> = _calendarIconMode

    private val _fontChoice = MutableStateFlow(prefs.fontChoice)
    val fontChoice: StateFlow<String> = _fontChoice

    /**
     * Liest alle Einstellungen erneut aus den SecurePrefs.
     * Wird nach dem Verlassen des Einstellungs-Screens aufgerufen,
     * damit Änderungen sofort im Theme sichtbar werden.
     */
    fun refresh() {
        _themeChoice.value = prefs.themeChoice
        _accentColor.value = prefs.accentColor
        _calendarIconMode.value = prefs.calendarIconMode
        _fontChoice.value = prefs.fontChoice
    }
}
