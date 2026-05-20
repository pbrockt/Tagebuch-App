package com.pbrockt.tagebuch.ui.search

import androidx.lifecycle.ViewModel
import com.pbrockt.tagebuch.data.model.DiaryPage
import com.pbrockt.tagebuch.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: DiaryRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<DiaryPage>> = _query
        .debounce(300)
        .flatMapLatest { q ->
            if (q.length < 2) flowOf(emptyList())
            else repo.searchPages(q)
        }
        .stateIn(kotlinx.coroutines.MainScope(), SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }
}
