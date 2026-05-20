package com.pbrockt.tagebuch

import androidx.lifecycle.ViewModel
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val prefs: SecurePrefs) : ViewModel() {

    private val _themeChoice = MutableStateFlow(prefs.themeChoice)
    val themeChoice: StateFlow<String> = _themeChoice

    private val _accentColor = MutableStateFlow(prefs.accentColor)
    val accentColor: StateFlow<String> = _accentColor

    private val _calendarIconMode = MutableStateFlow(prefs.calendarIconMode)
    val calendarIconMode: StateFlow<String> = _calendarIconMode

    fun refresh() {
        _themeChoice.value = prefs.themeChoice
        _accentColor.value = prefs.accentColor
        _calendarIconMode.value = prefs.calendarIconMode
    }
}
