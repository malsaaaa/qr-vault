package com.qrvault.ui.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qrvault.R
import com.qrvault.security.PinSecurity
import com.qrvault.ui.appContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinSetupViewModel(
    private val pinSecurity: PinSecurity,
) : ViewModel() {

    enum class Stage {
        CURRENT,
        NEW,
        CONFIRM,
    }

    data class UiState(
        val stage: Stage = Stage.NEW,
        val errorRes: Int? = null,
        val pinLengthTargetHint: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _newPin = MutableStateFlow("")
    private val _padNonce = MutableStateFlow(0L)

    private val _events = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val events: SharedFlow<Int> = _events

    val padNonce: StateFlow<Long> = _padNonce.asStateFlow()

    init {
        viewModelScope.launch {
            if (pinSecurity.isPinSet()) {
                _uiState.update { it.copy(stage = Stage.CURRENT, errorRes = null) }
            }
        }
    }

    fun onPinSubmitted(pin: String) {
        when (_uiState.value.stage) {
            Stage.CURRENT -> viewModelScope.launch {
                if (pinSecurity.verify(pin)) {
                    _newPin.value = ""
                    _uiState.update {
                        it.copy(stage = Stage.NEW, errorRes = null)
                    }
                } else {
                    _uiState.update { it.copy(errorRes = R.string.pin_incorrect) }
                }
                resetPad()
            }
            Stage.NEW -> {
                _newPin.value = pin
                _uiState.update { it.copy(stage = Stage.CONFIRM, errorRes = null) }
                resetPad()
            }
            Stage.CONFIRM -> viewModelScope.launch {
                if (pin == _newPin.value) {
                    pinSecurity.setPin(pin)
                    _events.tryEmit(R.string.pin_saved)
                } else {
                    _uiState.update {
                        it.copy(stage = Stage.NEW, errorRes = R.string.pin_mismatch)
                    }
                }
                resetPad()
            }
        }
    }

    private fun resetPad() {
        _padNonce.update { it + 1 }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { PinSetupViewModel(appContainer().pinSecurity) }
        }
    }
}