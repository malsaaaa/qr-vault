package com.qrvault.security

import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.qrvault.data.local.settings.AppSettings
import com.qrvault.data.local.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppLockController(
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
) : DefaultLifecycleObserver {

    private val _locked = MutableStateFlow(true)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private var cachedSettings = AppSettings()
    private var didUnlock = false
    private var lastBackgroundAt: Long? = null

    init {
        scope.launch {
            settingsRepository.settings.collect { settings ->
                cachedSettings = settings
                if (!settings.lockEnabled) {
                    _locked.value = false
                } else if (!didUnlock) {
                    _locked.value = true
                }
            }
        }
    }

    fun unlock() {
        didUnlock = true
        lastBackgroundAt = null
        _locked.value = false
    }

    fun lockNow() {
        didUnlock = false
        lastBackgroundAt = null
        _locked.value = true
    }

    override fun onStart(owner: LifecycleOwner) {
        val settings = cachedSettings
        if (!settings.lockEnabled || !didUnlock) return
        val last = lastBackgroundAt
        lastBackgroundAt = null
        if (last != null) {
            val timeout = settings.autoLockMillis
            if (timeout != null && SystemClock.elapsedRealtime() - last >= timeout) {
                lockNow()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        val settings = cachedSettings
        if (!settings.lockEnabled || !didUnlock) return
        if (settings.autoLockMillis == 0L) {
            lockNow()
        } else {
            lastBackgroundAt = SystemClock.elapsedRealtime()
        }
    }
}