package com.qrvault.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.qrvault.data.local.database.QrDatabase
import com.qrvault.data.local.settings.SettingsRepository
import com.qrvault.data.local.storage.PrivateImageStorage
import com.qrvault.data.local.storage.QrImageStorage
import com.qrvault.data.repository.QrRepository
import com.qrvault.security.AppLockController
import com.qrvault.security.PinSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class QrVaultContainer(context: Context) {

    private val appContext = context.applicationContext
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: QrDatabase = Room.databaseBuilder(
        appContext,
        QrDatabase::class.java,
        "qr_vault.db",
    ).build()

    val imageStorage: QrImageStorage = PrivateImageStorage(appContext)
    val repository: QrRepository = QrRepository(database.qrItemDao(), imageStorage)
    val settingsRepository: SettingsRepository = SettingsRepository(appContext.settingsDataStore)
    val pinSecurity: PinSecurity = PinSecurity(appContext.settingsDataStore)
    val lockController: AppLockController = AppLockController(settingsRepository, scope)

    val pendingImage = MutableStateFlow<Uri?>(null)

    fun clearPendingImage() {
        pendingImage.value = null
    }
}