package com.pbrockt.tagebuch.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class StatsData(
    val totalDays: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWords: Int = 0,
    val monthCounts: Map<String, Int> = emptyMap(),
    val moodCounts: Map<String, Int> = emptyMap()
)

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
            totalWords = pages.sumOf { it.content.trim().split(Regex("\\s+")).filter { w -> w.isNotEmpty() }.size },
            monthCounts = days.groupBy { it.date.substring(0, 7) }.mapValues { it.value.size },
            moodCounts = days.filter { it.mood != null }.groupBy { it.mood!! }.mapValues { it.value.size }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsData())

    private fun calculateCurrentStreak(sortedDates: List<LocalDate>): Int {
        var streak = 0
        var expected = LocalDate.now()
        for (date in sortedDates) {
            if (date == expected) { streak++; expected = expected.minusDays(1) }
            else if (date < expected) break
        }
        return streak
    }

    private fun calculateLongestStreak(sortedDates: List<LocalDate>): Int {
        if (sortedDates.isEmpty()) return 0
        var longest = 1; var current = 1
        for (i in 1 until sortedDates.size) {
            if (sortedDates[i - 1].minusDays(1) == sortedDates[i]) { current++; longest = maxOf(longest, current) }
            else current = 1
        }
        return longest
    }
}
