package com.qrvault.ui.screens.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrvault.R
import com.qrvault.data.local.settings.AppSettings
import com.qrvault.security.BiometricSupport
import com.qrvault.ui.LocalAppContainer
import com.qrvault.ui.components.PinPad
import com.qrvault.ui.components.rememberBiometricAuth
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val settings by container.settingsRepository.settings
        .collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()

    val hasBiometric = remember { BiometricSupport.hasBiometric(context) }
    val unlockAuth = rememberBiometricAuth(
        onSuccess = {
            scope.launch { container.pinSecurity.clearFailures() }
            onUnlocked()
        },
    )

    var showPinPad by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<Int?>(null) }
    var padNonce by remember { mutableIntStateOf(0) }
    var lockedUntil by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val remaining = container.pinSecurity.lockoutRemainingMillis()
        if (remaining > 0) {
            lockedUntil = System.currentTimeMillis() + remaining
            pinError = R.string.pin_too_many_attempts
        }
    }

    val verifyPin: (String) -> Unit = { pin ->
        val now = System.currentTimeMillis()
        if (now < lockedUntil) {
            pinError = R.string.pin_too_many_attempts
            padNonce++
        } else {
            scope.launch {
                if (container.pinSecurity.verify(pin)) {
                    container.pinSecurity.clearFailures()
                    lockedUntil = 0L
                    pinError = null
                    onUnlocked()
                } else {
                    val remaining = container.pinSecurity.registerFailedAttempt()
                    if (remaining > 0) {
                        lockedUntil = System.currentTimeMillis() + remaining
                        pinError = R.string.pin_too_many_attempts
                    } else {
                        pinError = R.string.pin_incorrect
                    }
                    padNonce++
                }
            }
        }
    }

    val unlockAvailable = hasBiometric || BiometricSupport.isDeviceSecure(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.QrCode,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_locked_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_locked_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        if (showPinPad) {
            pinError?.let { res ->
                Text(
                    text = stringResource(res),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
            }
            PinPad(
                key = padNonce,
                onPinSubmitted = verifyPin,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showPinPad = false }) {
                Text(stringResource(R.string.cancel))
            }
        } else {
            if (unlockAvailable) {
                Button(
                    onClick = { unlockAuth() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(
                            if (hasBiometric) R.string.unlock_with_biometrics
                            else R.string.use_device_credential,
                        ),
                    )
                }
                if (settings.pinSet) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showPinPad = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.unlock_with_pin))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.security_not_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}