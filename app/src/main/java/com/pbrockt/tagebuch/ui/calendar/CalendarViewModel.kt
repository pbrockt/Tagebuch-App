package com.pbrockt.tagebuch.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import com.pbrockt.tagebuch.data.repository.DiaryRepository
import com.pbrockt.tagebuch.data.repository.SyncRepository
import com.pbrockt.tagebuch.export.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val diaryRepo: DiaryRepository,
    private val syncRepo: SyncRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    val allDays: StateFlow<List<DiaryDay>> = diaryRepo.getAllDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datesWithEntries: StateFlow<Set<String>> = diaryRepo.getAllDates()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    val pagesForSelectedDay: StateFlow<List<DiaryPage>> = _selectedDate
        .flatMapLatest { date ->
            if (date != null) diaryRepo.getPagesForDay(date.toString())
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncState = syncRepo.syncState

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile

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
                day.date to diaryRepo.getPagesForDay(day.date).first()
            }
            val file = PdfExporter.export(context, days, pagesMap, "Tagebuch-$month.pdf")
            _exportedFile.value = file
        }
    }

    fun clearExportedFile() { _exportedFile.value = null }
}
