package com.qrvault.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qrvault.R
import com.qrvault.data.local.settings.AppSettings
import com.qrvault.data.local.settings.SettingsRepository
import com.qrvault.security.BiometricSupport
import com.qrvault.ui.appContainer
import com.qrvault.util.ThemeMode
import com.qrvault.util.ViewMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDefaultView(mode: ViewMode) {
        viewModelScope.launch { settingsRepository.setDefaultView(mode) }
    }

    fun setConfirmDelete(value: Boolean) {
        viewModelScope.launch { settingsRepository.setConfirmDelete(value) }
    }

    fun setAutoLockMillis(millis: Long?) {
        viewModelScope.launch { settingsRepository.setAutoLockMillis(millis) }
    }

    fun setBiometricEnabled(value: Boolean) {
        viewModelScope.launch { settingsRepository.setBiometricEnabled(value) }
    }

    fun setLockEnabled(value: Boolean, context: Context) {
        if (value && !BiometricSupport.isUnlockAvailable(context)) {
            _messages.tryEmit(R.string.security_not_available)
            return
        }
        viewModelScope.launch {
            settingsRepository.setLockEnabled(value)
            if (!value) settingsRepository.setBiometricEnabled(false)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(appContainer().settingsRepository) }
        }
    }
}