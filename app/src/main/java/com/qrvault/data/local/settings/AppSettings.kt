package com.qrvault.data.local.settings

import com.qrvault.util.SortOption
import com.qrvault.util.ThemeMode
import com.qrvault.util.ViewMode

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultViewMode: ViewMode = ViewMode.GRID,
    val confirmDelete: Boolean = true,
    val lockEnabled: Boolean = false,
    val autoLockMillis: Long? = 60_000L,
    val biometricEnabled: Boolean = false,
    val pinSet: Boolean = false,
)