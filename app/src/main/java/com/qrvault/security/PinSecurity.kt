package com.qrvault.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.qrvault.data.local.settings.StoreKeys
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class PinSecurity(
    private val dataStore: DataStore<Preferences>,
) {

    private val alias = "qr_vault_pin_key"
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    suspend fun isPinSet(): Boolean =
        dataStore.data.first()[StoreKeys.pinSet] ?: false

    suspend fun setPin(pin: String) {
        withContext(Dispatchers.IO) {
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
            val cipherText = Base64.encodeToString(
                cipher.doFinal(pin.toByteArray(Charsets.UTF_8)),
                Base64.NO_WRAP,
            )
            dataStore.edit { prefs ->
                prefs[StoreKeys.pinIv] = iv
                prefs[StoreKeys.pinCipher] = cipherText
                prefs[StoreKeys.pinSet] = true
            }
        }
    }

    suspend fun verify(pin: String): Boolean = withContext(Dispatchers.IO) {
        val prefs = dataStore.data.first()
        val ivText = prefs[StoreKeys.pinIv] ?: return@withContext false
        val cipherText = prefs[StoreKeys.pinCipher] ?: return@withContext false
        try {
            val key = keyStore.getKey(alias, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(
                    TAG_LENGTH_BITS,
                    Base64.decode(ivText, Base64.NO_WRAP),
                ),
            )
            val decrypted = String(
                cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP)),
                Charsets.UTF_8,
            )
            decrypted == pin
        } catch (_: Exception) {
            false
        }
    }

    suspend fun clearPin() {
        dataStore.edit { prefs ->
            prefs.remove(StoreKeys.pinIv)
            prefs.remove(StoreKeys.pinCipher)
            prefs[StoreKeys.pinSet] = false
        }
    }

    suspend fun lockoutRemainingMillis(): Long = withContext(Dispatchers.IO) {
        val prefs = dataStore.data.first()
        (prefs[StoreKeys.pinLockoutUntil] ?: 0L)
            .let { until -> (until - System.currentTimeMillis()).coerceAtLeast(0L) }
    }

    suspend fun registerFailedAttempt(): Long = withContext(Dispatchers.IO) {
        var lockout = 0L
        dataStore.edit { prefs ->
            val failures = (prefs[StoreKeys.pinFailures] ?: 0) + 1
            if (failures >= MAX_FAILED_ATTEMPTS) {
                prefs[StoreKeys.pinFailures] = 0
                lockout = LOCKOUT_MILLIS
                prefs[StoreKeys.pinLockoutUntil] = System.currentTimeMillis() + lockout
            } else {
                prefs[StoreKeys.pinFailures] = failures
            }
        }
        lockout
    }

    suspend fun clearFailures() {
        withContext(Dispatchers.IO) {
            dataStore.edit { prefs ->
                prefs.remove(StoreKeys.pinFailures)
                prefs.remove(StoreKeys.pinLockoutUntil)
            }
        }
    }

    private fun getOrCreateKey(): SecretKey {
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_LENGTH_BITS = 128
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_MILLIS = 30_000L
    }
}