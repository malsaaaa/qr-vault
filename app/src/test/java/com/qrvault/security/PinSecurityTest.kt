package com.qrvault.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PinSecurityTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun dataStore(): DataStore<Preferences> {
        val file = File(tmp.newFolder("datastore"), "settings.preferences_pb")
        return PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO),
            produceFile = { file },
        )
    }

    @Test
    fun `no lockout until threshold reached`() = runBlocking {
        val pinSecurity = PinSecurity(dataStore())

        assertEquals(0L, pinSecurity.lockoutRemainingMillis())
        repeat(4) {
            assertEquals(0L, pinSecurity.registerFailedAttempt())
            assertEquals(0L, pinSecurity.lockoutRemainingMillis())
        }
    }

    @Test
    fun `fifth failed attempt triggers lockout`() = runBlocking {
        val pinSecurity = PinSecurity(dataStore())

        repeat(4) { pinSecurity.registerFailedAttempt() }
        val remaining = pinSecurity.registerFailedAttempt()

        assertTrue(remaining > 0L)
        assertTrue(pinSecurity.lockoutRemainingMillis() in 1L..remaining)
    }

    @Test
    fun `clearFailures resets lockout`() = runBlocking {
        val pinSecurity = PinSecurity(dataStore())

        repeat(5) { pinSecurity.registerFailedAttempt() }
        assertTrue(pinSecurity.lockoutRemainingMillis() > 0L)

        pinSecurity.clearFailures()

        assertEquals(0L, pinSecurity.lockoutRemainingMillis())
        assertEquals(0L, pinSecurity.registerFailedAttempt())
    }

    @Test
    fun `failures persist across instances`() = runBlocking {
        val store = dataStore()
        val first = PinSecurity(store)
        val second = PinSecurity(store)

        repeat(4) { first.registerFailedAttempt() }
        val afterSwap = second.registerFailedAttempt()

        assertTrue(afterSwap > 0L)
    }
}