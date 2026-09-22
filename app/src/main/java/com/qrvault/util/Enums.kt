package com.qrvault.util

import androidx.annotation.StringRes
import com.qrvault.R

enum class ThemeMode(@StringRes val labelRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark),
}

enum class ViewMode(@StringRes val labelRes: Int) {
    GRID(R.string.grid_view),
    LIST(R.string.list_view),
}

enum class SortOption(@StringRes val labelRes: Int) {
    RECENT_ADDED(R.string.sort_recent_added),
    RECENT_UPDATED(R.string.sort_recent_updated),
    NAME_A_Z(R.string.sort_name_az),
    NAME_Z_A(R.string.sort_name_za),
    PROVIDER_A_Z(R.string.sort_provider_az),
}

enum class AutoLockOption(val millis: Long?, @StringRes val labelRes: Int) {
    IMMEDIATE(0L, R.string.auto_lock_immediately),
    ONE_MINUTE(60_000L, R.string.auto_lock_1min),
    FIVE_MINUTES(300_000L, R.string.auto_lock_5min),
    NEVER(null, R.string.auto_lock_never);

    companion object {
        fun fromMillis(millis: Long?): AutoLockOption =
            entries.firstOrNull { it.millis == millis } ?: NEVER

        val ordered: List<AutoLockOption> = listOf(IMMEDIATE, ONE_MINUTE, FIVE_MINUTES, NEVER)
    }
}