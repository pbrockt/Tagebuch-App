package com.pbrockt.tagebuch.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbrockt.tagebuch.data.contacts.BirthdayHelper
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import com.pbrockt.tagebuch.data.repository.DiaryRepository
import com.pbrockt.tagebuch.data.repository.SyncRepository
import com.pbrockt.tagebuch.export.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class MonthSummary(
    val entryCount: Int = 0,
    val dominantMood: String? = null,
    val currentStreak: Int = 0
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val diaryRepo: DiaryRepository,
    private val syncRepo: SyncRepository,
    private val prefs: SecurePrefs,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _calendarIconMode = MutableStateFlow(prefs.calendarIconMode)
    val calendarIconMode: StateFlow<String> = _calendarIconMode

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    val allDays: StateFlow<List<DiaryDay>> = diaryRepo.getAllDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datesWithEntries: StateFlow<Set<String>> = diaryRepo.getAllDates()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val pageCountPerDay: StateFlow<Map<String, Int>> = diaryRepo.getAllPages()
        .map { pages -> pages.groupBy { it.dayDate }.mapValues { it.value.size } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Monats-Zusammenfassung (Einträge, Stimmung, Streak)
    val monthSummary: StateFlow<MonthSummary> = combine(
        _currentMonth, allDays, datesWithEntries
    ) { month, days, dates ->
        val monthStr = month.toString()
        val monthDays = days.filter { it.date.startsWith(monthStr) }
        val entryCount = dates.count { it.startsWith(monthStr) }
        val dominantMood = monthDays
            .mapNotNull { it.mood }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key
        val streak = calculateCurrentStreak(dates.toList())
        MonthSummary(entryCount, dominantMood, streak)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthSummary())

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    val pagesForSelectedDay: StateFlow<List<DiaryPage>> = _selectedDate
        .flatMapLatest { date ->
            if (date != null) diaryRepo.getPagesForDay(date.toString())
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Geburtstage
    private val _birthdayMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val birthdayMap: StateFlow<Map<String, List<String>>> = _birthdayMap

    // Streak-Meilenstein der angezeigt werden soll (null = keiner)
    private val _streakMilestone = MutableStateFlow<Int?>(null)
    val streakMilestone: StateFlow<Int?> = _streakMilestone

    val syncState = syncRepo.syncState

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile

    val periodTrackingEnabled: Boolean get() = prefs.periodTrackingEnabled

    init {
        // Prüfe Streak-Meilenstein beim Start
        viewModelScope.launch {
            datesWithEntries.collect { dates ->
                checkStreakMilestone(dates)
            }
        }
    }

    private fun calculateCurrentStreak(dates: List<String>): Int {
        val sorted = dates.map { LocalDate.parse(it) }.sortedDescending()
        var streak = 0
        var expected = LocalDate.now()
        for (date in sorted) {
            if (date == expected) { streak++; expected = expected.minusDays(1) }
            else if (date < expected) break
        }
        return streak
    }

    private fun checkStreakMilestone(dates: Set<String>) {
        val streak = calculateCurrentStreak(dates.toList())
        val milestones = listOf(7, 30, 100)
        val shown = prefs.shownStreakMilestones.split(",").filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }.toSet()
        val newMilestone = milestones.firstOrNull { it == streak && it !in shown }
        if (newMilestone != null) {
            _streakMilestone.value = newMilestone
        }
    }

    fun onStreakMilestoneDismissed(milestone: Int) {
        val shown = prefs.shownStreakMilestones.split(",").filter { it.isNotEmpty() }.toMutableList()
        shown.add(milestone.toString())
        prefs.shownStreakMilestones = shown.joinToString(",")
        _streakMilestone.value = null
    }

    fun refreshSettings() {
        _calendarIconMode.value = prefs.calendarIconMode
        loadBirthdays()
    }

    fun loadBirthdays() {
        if (!BirthdayHelper.hasPermission(context)) return
        viewModelScope.launch(Dispatchers.IO) {
            _birthdayMap.value = BirthdayHelper.getBirthdays(context)
        }
    }

    fun previousMonth() { _currentMonth.update { it.minusMonths(1) } }
    fun nextMonth() { _currentMonth.update { it.plusMonths(1) } }
    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun closePopup() { _selectedDate.value = null }

    fun addPageToSelectedDay() {
        val date = _selectedDate.value ?: return
        viewModelScope.launch { diaryRepo.createPage(date.toString()) }
    }

    fun savePage(page: DiaryPage) { viewModelScope.launch { diaryRepo.savePage(page) } }
    fun deletePage(page: DiaryPage) { viewModelScope.launch { diaryRepo.deletePage(page) } }
    fun setMood(date: String, mood: String?) { viewModelScope.launch { diaryRepo.setMood(date, mood) } }
    fun setWeather(date: String, weather: String?) { viewModelScope.launch { diaryRepo.setWeather(date, weather) } }
    fun setPeriod(date: String, period: String?) { viewModelScope.launch { diaryRepo.setPeriod(date, period) } }
    fun syncNow() { viewModelScope.launch { syncRepo.triggerSync() } }

    fun getOwnBirthdayMmDd(): String {
        val raw = prefs.ownBirthdayDate
        if (raw.isBlank()) return ""
        val parts = raw.split(".")
        if (parts.size != 2) return ""
        return try { "${parts[1].padStart(2,'0')}-${parts[0].padStart(2,'0')}" } catch (e: Exception) { "" }
    }

    fun exportCurrentMonthPdf() {
        viewModelScope.launch {
            val month = _currentMonth.value.toString()
            val days = allDays.value.filter { it.date.startsWith(month) }
            val pagesMap = days.associate { day ->
                day.date to withContext(Dispatchers.IO) { diaryRepo.getPagesForDay(day.date).first() }
            }
            val file = withContext(Dispatchers.IO) {
                PdfExporter.export(context, days, pagesMap, "Tagebuch-$month.pdf")
            }
            _exportedFile.value = file
        }
    }

    fun clearExportedFile() { _exportedFile.value = null }

    /** Liefert Einträge desselben Kalendertags in früheren Jahren */
    suspend fun getPastYearEntries(date: LocalDate): List<Pair<Int, List<DiaryPage>>> =
        withContext(Dispatchers.IO) {
            val mmdd = "-${date.monthValue.toString().padStart(2,'0')}-${date.dayOfMonth.toString().padStart(2,'0')}"
            val pastDates = datesWithEntries.value.filter { it.endsWith(mmdd) && it != date.toString() }
            pastDates.mapNotNull { dateStr ->
                val year = dateStr.take(4).toIntOrNull() ?: return@mapNotNull null
                val pages = diaryRepo.getPagesForDay(dateStr).first()
                if (pages.isEmpty()) null else Pair(year, pages)
            }.sortedByDescending { it.first }
        }
}
