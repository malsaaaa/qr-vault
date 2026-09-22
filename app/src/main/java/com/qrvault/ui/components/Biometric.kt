package com.qrvault.ui.components

import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun rememberBiometricAuth(onSuccess: () -> Unit): () -> Unit {
    val activity = LocalContext.current as FragmentActivity
    val latestSuccess by rememberUpdatedState(onSuccess)

    val callback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                latestSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = Unit
        }
    }

    return remember(activity) {
        {
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(activity, executor, callback)
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(com.qrvault.R.string.app_locked_title))
                .setSubtitle(activity.getString(com.qrvault.R.string.app_locked_body))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                )
                .build()
            prompt.authenticate(info)
        }
    }
}