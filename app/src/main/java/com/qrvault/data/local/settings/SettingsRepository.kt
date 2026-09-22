package com.qrvault.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.qrvault.util.ThemeMode
import com.qrvault.util.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object StoreKeys {
    const val NEVER_SENTINEL = Long.MIN_VALUE

    val themeMode = intPreferencesKey("theme_mode")
    val defaultView = intPreferencesKey("default_view")
    val confirmDelete = booleanPreferencesKey("confirm_delete")
    val lockEnabled = booleanPreferencesKey("lock_enabled")
    val autoLock = longPreferencesKey("auto_lock")
    val biometricEnabled = booleanPreferencesKey("biometric_enabled")
    val pinSet = booleanPreferencesKey("pin_set")
    val pinIv = stringPreferencesKey("pin_iv")
    val pinCipher = stringPreferencesKey("pin_cipher")
    val pinFailures = intPreferencesKey("pin_failures")
    val pinLockoutUntil = longPreferencesKey("pin_lockout_until")
}

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.entries
                .getOrElse(prefs[StoreKeys.themeMode] ?: ThemeMode.SYSTEM.ordinal) { ThemeMode.SYSTEM },
            defaultViewMode = ViewMode.entries
                .getOrElse(prefs[StoreKeys.defaultView] ?: ViewMode.GRID.ordinal) { ViewMode.GRID },
            confirmDelete = prefs[StoreKeys.confirmDelete] ?: true,
            lockEnabled = prefs[StoreKeys.lockEnabled] ?: false,
            autoLockMillis = if (prefs[StoreKeys.autoLock] == StoreKeys.NEVER_SENTINEL) {
                null
            } else {
                prefs[StoreKeys.autoLock] ?: 60_000L
            },
            biometricEnabled = prefs[StoreKeys.biometricEnabled] ?: false,
            pinSet = prefs[StoreKeys.pinSet] ?: false,
        )
    }

    suspend fun latest(): AppSettings = settings.first()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[StoreKeys.themeMode] = mode.ordinal }
    }

    suspend fun setDefaultView(mode: ViewMode) {
        dataStore.edit { it[StoreKeys.defaultView] = mode.ordinal }
    }

    suspend fun setConfirmDelete(value: Boolean) {
        dataStore.edit { it[StoreKeys.confirmDelete] = value }
    }

    suspend fun setLockEnabled(value: Boolean) {
        dataStore.edit { it[StoreKeys.lockEnabled] = value }
    }

    suspend fun setAutoLockMillis(millis: Long?) {
        dataStore.edit { it[StoreKeys.autoLock] = millis ?: StoreKeys.NEVER_SENTINEL }
    }

    suspend fun setBiometricEnabled(value: Boolean) {
        dataStore.edit { it[StoreKeys.biometricEnabled] = value }
    }
}