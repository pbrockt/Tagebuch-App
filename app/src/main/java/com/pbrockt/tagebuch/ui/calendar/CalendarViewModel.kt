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

    // Anzahl Seiten pro Tag für die Dot-Anzeige
    val pageCountPerDay: StateFlow<Map<String, Int>> = diaryRepo.getAllPages()
        .map { pages -> pages.groupBy { it.dayDate }.mapValues { it.value.size } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    val pagesForSelectedDay: StateFlow<List<DiaryPage>> = _selectedDate
        .flatMapLatest { date ->
            if (date != null) diaryRepo.getPagesForDay(date.toString())
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Geburtstage: "MM-dd" → List<Name>
    private val _birthdayMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val birthdayMap: StateFlow<Map<String, List<String>>> = _birthdayMap

    val syncState = syncRepo.syncState

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile

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

    fun savePage(page: DiaryPage) {
        viewModelScope.launch { diaryRepo.savePage(page) }
    }

    fun deletePage(page: DiaryPage) {
        viewModelScope.launch { diaryRepo.deletePage(page) }
    }

    fun setMood(date: String, mood: String?) {
        viewModelScope.launch { diaryRepo.setMood(date, mood) }
    }

    fun setWeather(date: String, weather: String?) {
        viewModelScope.launch { diaryRepo.setWeather(date, weather) }
    }

    fun syncNow() {
        viewModelScope.launch { syncRepo.triggerSync() }
    }

    fun exportCurrentMonthPdf() {
        viewModelScope.launch {
            val month = _currentMonth.value.toString()
            val days = allDays.value.filter { it.date.startsWith(month) }
            val pagesMap = days.associate { day ->
                day.date to withContext(Dispatchers.IO) {
                    diaryRepo.getPagesForDay(day.date).first()
                }
            }
            val file = withContext(Dispatchers.IO) {
                PdfExporter.export(context, days, pagesMap, "Tagebuch-$month.pdf")
            }
            _exportedFile.value = file
        }
    }

    fun clearExportedFile() { _exportedFile.value = null }

    fun getOwnBirthdayMmDd(): String {
        val raw = prefs.ownBirthdayDate  // "TT.MM" Format
        if (raw.isBlank()) return ""
        val parts = raw.split(".")
        if (parts.size != 2) return ""
        return try {
            "${parts[1].padStart(2,'0')}-${parts[0].padStart(2,'0')}"
        } catch (e: Exception) { "" }
    }
}
