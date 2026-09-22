package com.qrvault.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qrvault.BuildConfig
import com.qrvault.R
import com.qrvault.ui.LocalAppContainer
import com.qrvault.util.AutoLockOption
import com.qrvault.util.ThemeMode
import com.qrvault.util.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPinSetup: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val locked by container.lockController.locked.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val biometricAvailable = remember {
        com.qrvault.security.BiometricSupport.hasBiometric(context)
    }
    val deviceSecure = remember {
        com.qrvault.security.BiometricSupport.isDeviceSecure(context)
    }

    var privacyDialog by remember { mutableStateOf(false) }
    var licensesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { res ->
            snackbarHostState.showSnackbar(context.getString(res))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                SectionHeader(stringResource(R.string.section_security))
            }
            item {
                SwitchRow(
                    title = stringResource(R.string.app_lock),
                    description = stringResource(R.string.app_lock_desc),
                    checked = settings.lockEnabled,
                    onCheckedChange = { viewModel.setLockEnabled(it, context) },
                )
            }
            if (settings.lockEnabled) {
                item {
                    AutoLockRow(
                        selectedMillis = settings.autoLockMillis,
                        onSelect = viewModel::setAutoLockMillis,
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        OutlinedButton(onClick = { container.lockController.lockNow() }) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.lock_now))
                        }
                    }
                }
                item {
                    SwitchRow(
                        title = stringResource(R.string.use_biometric),
                        description = stringResource(R.string.use_biometric_desc),
                        checked = settings.biometricEnabled && biometricAvailable,
                        enabled = biometricAvailable,
                        onCheckedChange = viewModel::setBiometricEnabled,
                    )
                }
                if (!biometricAvailable) {
                    item {
                        Text(
                            text = stringResource(R.string.biometric_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
                if (!deviceSecure) {
                    item {
                        Text(
                            text = stringResource(R.string.security_not_available),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
            item {
                SettingRow(
                    title = if (settings.pinSet) stringResource(R.string.change_pin)
                    else stringResource(R.string.set_pin),
                    onClick = onPinSetup,
                    leading = { Icon(Icons.Filled.Key, contentDescription = null) },
                    trailing = { ChevronRight() },
                )
            }

            item {
                SectionHeader(stringResource(R.string.section_appearance))
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = stringResource(R.string.theme_mode),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    ThemeMode.entries.forEach { mode ->
                        RadioRow(
                            label = stringResource(mode.labelRes),
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                        )
                    }
                }
            }

            item {
                SectionHeader(stringResource(R.string.section_behavior))
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = stringResource(R.string.default_view),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    ViewMode.entries.forEach { mode ->
                        RadioRow(
                            label = stringResource(mode.labelRes),
                            selected = settings.defaultViewMode == mode,
                            onClick = { viewModel.setDefaultView(mode) },
                        )
                    }
                }
            }
            item {
                SwitchRow(
                    title = stringResource(R.string.confirm_before_delete),
                    description = stringResource(R.string.confirm_before_delete_desc),
                    checked = settings.confirmDelete,
                    onCheckedChange = viewModel::setConfirmDelete,
                )
            }

            item {
                SectionHeader(stringResource(R.string.section_about))
            }
            item {
                SettingRow(
                    title = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                    leading = { Icon(Icons.Filled.Info, contentDescription = null) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.privacy),
                    onClick = { privacyDialog = true },
                    leading = { Icon(Icons.Filled.PrivacyTip, contentDescription = null) },
                    trailing = { ChevronRight() },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.licenses),
                    onClick = { licensesDialog = true },
                    leading = { Icon(Icons.Filled.Info, contentDescription = null) },
                    trailing = { ChevronRight() },
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (privacyDialog) {
        AlertDialog(
            onDismissRequest = { privacyDialog = false },
            title = { Text(stringResource(R.string.privacy)) },
            text = { Text(stringResource(R.string.privacy_body)) },
            confirmButton = {
                TextButtonCompat { privacyDialog = false }
            },
        )
    }

    if (licensesDialog) {
        AlertDialog(
            onDismissRequest = { licensesDialog = false },
            title = { Text(stringResource(R.string.licenses)) },
            text = { Text(licenseText()) },
            confirmButton = {
                TextButtonCompat { licensesDialog = false }
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun AutoLockRow(
    selectedMillis: Long?,
    onSelect: (Long?) -> Unit,
) {
    val selected = AutoLockOption.fromMillis(selectedMillis)
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = stringResource(R.string.auto_lock), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(selected.labelRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AutoLockOption.ordered.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        expanded = false
                        onSelect(option.millis)
                    },
                )
            }
        }
    }
}

@Composable
private fun RadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingRow(
    title: String,
    onClick: (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) trailing()
    }
}

@Composable
private fun ChevronRight() {
    Icon(
        Icons.Filled.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TextButtonCompat(onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(stringResource(R.string.close))
    }
}

private fun licenseText(): String {
    return """
        QR Vault uses the following open-source libraries:

        • Jetpack Compose (Apache License 2.0)
        • Jetpack Compose Material 3 (Apache License 2.0)
        • AndroidX Core / Lifecycle / Navigation / Room / DataStore / Activity (Apache License 2.0)
        • CameraX (Apache License 2.0)
        • Coil (Apache License 2.0)
        • AndroidX Biometric (Apache License 2.0)
        • Kotlin (Apache License 2.0)
    """.trimIndent()
}