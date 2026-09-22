package com.qrvault.ui.screens.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qrvault.data.local.database.QrItem
import com.qrvault.data.repository.QrRepository
import com.qrvault.ui.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ViewerViewModel(
    private val repository: QrRepository,
    itemId: Long,
) : ViewModel() {

    val item: StateFlow<QrItem?> = repository.observeAll()
        .map { items -> items.firstOrNull { it.id == itemId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        fun factory(itemId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer { ViewerViewModel(appContainer().repository, itemId) }
        }
    }
}