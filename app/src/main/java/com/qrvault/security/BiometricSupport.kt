package com.qrvault.security

import android.app.KeyguardManager
import android.content.Context
import androidx.biometric.BiometricManager

object BiometricSupport {

    fun hasBiometric(context: Context): Boolean {
        val result = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isDeviceSecure(context: Context): Boolean {
        val keyguard = context.getSystemService(KeyguardManager::class.java)
        return keyguard != null && keyguard.isDeviceSecure
    }

    fun isUnlockAvailable(context: Context): Boolean =
        hasBiometric(context) || isDeviceSecure(context)
}