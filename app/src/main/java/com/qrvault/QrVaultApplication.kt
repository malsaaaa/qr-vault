package com.qrvault

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.qrvault.data.QrVaultContainer

class QrVaultApplication : Application() {

    lateinit var container: QrVaultContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = QrVaultContainer(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(container.lockController)
    }
}