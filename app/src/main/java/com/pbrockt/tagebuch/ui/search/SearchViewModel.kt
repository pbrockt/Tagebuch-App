package com.pbrockt.tagebuch.ui.search

import androidx.lifecycle.ViewModel
import com.pbrockt.tagebuch.data.model.DiaryPage
import com.pbrockt.tagebuch.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * ViewModel für die Volltext-Suche.
 *
 * Besonderheit: Die Suche verwendet "debounce" — eine kurze Verzögerung
 * nach dem letzten Tastendruck. Das verhindert dass bei jedem einzelnen
 * Zeichen eine Datenbankabfrage ausgelöst wird.
 *
 * Ablauf: Nutzer tippt "Tag" →
 * - "T" eingegeben → 300ms warten
 * - "a" eingegeben → Timer reset → 300ms warten
 * - "g" eingegeben → Timer reset → 300ms warten
 * - Keine Eingabe → Suche startet mit "Tag"
 */
@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: DiaryRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    /**
     * Suchergebnisse als reaktiver Flow.
     *
     * flatMapLatest: Wenn eine neue Suche startet, wird die alte abgebrochen.
     * So gibt es keine veralteten Ergebnisse wenn der Nutzer schnell tippt.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<DiaryPage>> = _query
        .debounce(300)  // 300ms nach letzter Eingabe warten
        .flatMapLatest { q ->
            if (q.length < 2) flowOf(emptyList())  // Weniger als 2 Zeichen → keine Suche
            else repo.searchPages(q)
        }
        .stateIn(kotlinx.coroutines.MainScope(), SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }
}
