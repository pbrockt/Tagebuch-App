package com.pbrockt.tagebuch

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemeinsamer Zustand für tab-übergreifende Navigation.
 *
 * Problem: Wenn aus der Suche ein Eintrag geöffnet werden soll,
 * müssen wir den Kalender-Tab aktivieren UND dort einen bestimmten
 * Tag öffnen. Das geht nicht direkt weil die ViewModels der Tabs
 * voneinander isoliert sind.
 *
 * Lösung: Dieses Singleton als Brücke — SearchViewModel schreibt,
 * CalendarViewModel liest.
 *
 * SharedFlow: Ein "Ereignis-Kanal" — jeder Wert wird genau einmal
 * ausgeliefert, auch wenn er vor dem Abonnieren gesendet wurde
 * (extraBufferCapacity = 1 speichert das letzte Ereignis).
 */
@Singleton
class NavigationState @Inject constructor() {

    private val _pendingOpenDate = MutableSharedFlow<String>(extraBufferCapacity = 1)

    /** Datum-Ereignisse die den Kalender zu einem bestimmten Tag führen sollen */
    val pendingOpenDate: SharedFlow<String> = _pendingOpenDate

    /** Sendet ein Datum das der Kalender-Screen öffnen soll */
    fun navigateToDate(date: String) {
        _pendingOpenDate.tryEmit(date)
    }
}
