package com.qrvault.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qrvault.R
import com.qrvault.data.local.database.QrItem
import com.qrvault.data.local.settings.SettingsRepository
import com.qrvault.data.repository.QrRepository
import com.qrvault.ui.appContainer
import com.qrvault.util.QrProcessor
import com.qrvault.util.SortOption
import com.qrvault.util.ViewMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: QrRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(SortOption.RECENT_ADDED)

    val items: StateFlow<List<QrItem>> = combine(
        repository.observeAll(),
        query,
        sort,
    ) { items, query, sort -> QrProcessor.apply(items, query, sort) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val queryFlow: StateFlow<String> = query.asStateFlow()
    val sortFlow: StateFlow<SortOption> = sort.asStateFlow()

    val viewMode: StateFlow<ViewMode> = settingsRepository.settings
        .map { it.defaultViewMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ViewMode.GRID)

    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onSortChange(value: SortOption) {
        sort.value = value
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            val current = settingsRepository.latest().defaultViewMode
            val next = if (current == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
            settingsRepository.setDefaultView(next)
        }
    }

    fun delete(item: QrItem) {
        viewModelScope.launch {
            repository.deleteQr(item)
                .onSuccess { _messages.tryEmit(R.string.deleted_success) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                HomeViewModel(container.repository, container.settingsRepository)
            }
        }
    }
}