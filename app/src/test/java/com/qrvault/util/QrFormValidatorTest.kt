package com.qrvault.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QrFormValidatorTest {

    @Test
    fun `valid input has no errors`() {
        val errors = QrFormValidator.validate("My QR", "Maybank", hasImage = true)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `blank name is required`() {
        val errors = QrFormValidator.validate("  ", "Maybank", hasImage = true)
        assertTrue(QrFormValidator.ValidationError.NAME_REQUIRED in errors)
    }

    @Test
    fun `blank provider is required`() {
        val errors = QrFormValidator.validate("My QR", "", hasImage = true)
        assertTrue(QrFormValidator.ValidationError.PROVIDER_REQUIRED in errors)
    }

    @Test
    fun `missing image is required`() {
        val errors = QrFormValidator.validate("My QR", "Maybank", hasImage = false)
        assertTrue(QrFormValidator.ValidationError.IMAGE_REQUIRED in errors)
    }

    @Test
    fun `all required errors present`() {
        val errors = QrFormValidator.validate("", "", hasImage = false)
        assertEquals(
            setOf(
                QrFormValidator.ValidationError.NAME_REQUIRED,
                QrFormValidator.ValidationError.PROVIDER_REQUIRED,
                QrFormValidator.ValidationError.IMAGE_REQUIRED,
            ),
            errors,
        )
    }
}