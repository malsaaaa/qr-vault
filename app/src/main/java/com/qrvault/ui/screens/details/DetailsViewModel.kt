package com.qrvault.ui.screens.details

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qrvault.R
import com.qrvault.data.local.database.QrItem
import com.qrvault.data.repository.QrRepository
import com.qrvault.ui.appContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repository: QrRepository,
    itemId: Long,
) : ViewModel() {

    private val itemFlow = repository.observeAll()
        .map { items -> items.firstOrNull { it.id == itemId } }

    val item: StateFlow<QrItem?> = itemFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var lastItem: QrItem? = null

    init {
        viewModelScope.launch {
            itemFlow.collect { current -> if (current != null) lastItem = current }
        }
    }

    private val _events = MutableSharedFlow<DetailsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DetailsEvent> = _events

    sealed interface DetailsEvent {
        data object Deleted : DetailsEvent
        data object Failed : DetailsEvent
    }

    fun createShareIntent(context: Context): Intent? {
        val current = lastItem ?: return null
        val uri = repository.shareUriFor(current)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun delete() {
        viewModelScope.launch {
            val current = lastItem
            if (current == null) {
                _events.tryEmit(DetailsEvent.Failed)
                return@launch
            }
            repository.deleteQr(current)
                .onSuccess { _events.tryEmit(DetailsEvent.Deleted) }
                .onFailure { _events.tryEmit(DetailsEvent.Failed) }
        }
    }

    companion object {
        fun factory(itemId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer { DetailsViewModel(appContainer().repository, itemId) }
        }
    }
}