package com.qrvault.util

object QrFormValidator {

    enum class ValidationError {
        NAME_REQUIRED,
        PROVIDER_REQUIRED,
        IMAGE_REQUIRED,
    }

    fun validate(name: String, provider: String, hasImage: Boolean): Set<ValidationError> {
        val errors = mutableSetOf<ValidationError>()
        if (name.isBlank()) errors += ValidationError.NAME_REQUIRED
        if (provider.isBlank()) errors += ValidationError.PROVIDER_REQUIRED
        if (!hasImage) errors += ValidationError.IMAGE_REQUIRED
        return errors
    }
}