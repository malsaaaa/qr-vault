package com.qrvault

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrvault.data.local.settings.AppSettings
import com.qrvault.ui.LocalAppContainer
import com.qrvault.ui.navigation.QrVaultNavHost
import com.qrvault.ui.screens.lock.LockScreen
import com.qrvault.ui.theme.QrVaultTheme
import com.qrvault.util.ThemeMode

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as QrVaultApplication).container
        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CompositionLocalProvider(LocalAppContainer provides container) {
                QrVaultTheme(darkTheme = darkTheme) {
                    val locked by container.lockController.locked.collectAsStateWithLifecycle()
                    if (locked) {
                        LockScreen(onUnlocked = { container.lockController.unlock() })
                    } else {
                        QrVaultNavHost()
                    }
                }
            }
        }
    }
}