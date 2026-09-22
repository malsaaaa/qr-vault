package com.qrvault.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.qrvault.data.QrVaultContainer

val LocalAppContainer = staticCompositionLocalOf<QrVaultContainer> {
    error("QrVaultContainer not provided")
}