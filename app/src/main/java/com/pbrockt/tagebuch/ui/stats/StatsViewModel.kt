package com.pbrockt.tagebuch.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

/** Container für alle berechneten Statistiken */
data class StatsData(
    val totalDays: Int = 0,          // Gesamtanzahl Tage mit Einträgen
    val currentStreak: Int = 0,      // Aktuelle Tage-Streak
    val longestStreak: Int = 0,      // Längste je erreichte Streak
    val totalWords: Int = 0,         // Gesamtanzahl geschriebener Wörter
    val monthCounts: Map<String, Int> = emptyMap(),   // Einträge pro Monat "YYYY-MM" → Anzahl
    val moodCounts: Map<String, Int> = emptyMap()     // Stimmungsverteilung "great" → Anzahl
)

/**
 * ViewModel für den Statistik-Screen.
 *
 * Berechnet alle Statistiken reaktiv aus den Datenbank-Flows.
 * combine() verknüpft mehrere Flows: Wenn sich Tage ODER Seiten ändern,
 * werden die Statistiken automatisch neu berechnet.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(repo: DiaryRepository) : ViewModel() {

    val stats: StateFlow<StatsData> = combine(
        repo.getAllDays(),
        repo.getAllPages()
    ) { days, pages ->
        val dates = days.map { LocalDate.parse(it.date) }.sortedDescending()
        StatsData(
            totalDays = days.size,
            currentStreak = calculateCurrentStreak(dates),
            longestStreak = calculateLongestStreak(dates),
            // Wörter zählen: Text splitten an Leerzeichen, leere Teile ignorieren
            totalWords = pages.sumOf { page ->
                page.content.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
            },
            // Einträge pro Monat gruppieren
            monthCounts = days.groupBy { it.date.substring(0, 7) }.mapValues { it.value.size },
            // Stimmungen zählen (nur Tage mit gesetzter Stimmung)
            moodCounts = days.filter { it.mood != null }
                .groupBy { it.mood!! }
                .mapValues { it.value.size }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsData())

    /**
     * Berechnet die aktuelle Streak (wie viele Tage in Folge geschrieben).
     *
     * Algorithmus: Beginne bei heute, gehe rückwärts.
     * Solange der nächste Eintrag genau einen Tag früher liegt → Streak erhöhen.
     * Sobald eine Lücke auftaucht → Streak endet.
     */
    private fun calculateCurrentStreak(sortedDates: List<LocalDate>): Int {
        var streak = 0
        var expected = LocalDate.now()
        for (date in sortedDates) {
            if (date == expected) {
                streak++
                expected = expected.minusDays(1)  // Nächsten Tag zurückgehen
            } else if (date < expected) {
                break  // Lücke gefunden — Streak endet hier
            }
        }
        return streak
    }

    /**
     * Berechnet die längste jemals erreichte Streak.
     * Durchläuft alle Einträge und findet die längste ununterbrochene Folge.
     */
    private fun calculateLongestStreak(sortedDates: List<LocalDate>): Int {
        if (sortedDates.isEmpty()) return 0
        var longest = 1
        var current = 1
        for (i in 1 until sortedDates.size) {
            // Aufeinanderfolgende Tage: gestern war der Tag vorher
            if (sortedDates[i - 1].minusDays(1) == sortedDates[i]) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 1  // Lücke → neu anfangen zählen
            }
        }
        return longest
    }
}
