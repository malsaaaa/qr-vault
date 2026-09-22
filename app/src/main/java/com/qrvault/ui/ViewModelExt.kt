package com.qrvault.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.qrvault.QrVaultApplication
import com.qrvault.data.QrVaultContainer

fun CreationExtras.appContainer(): QrVaultContainer {
    return (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as QrVaultApplication).container
}