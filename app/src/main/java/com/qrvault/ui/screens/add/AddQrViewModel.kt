package com.qrvault.ui.screens.add

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qrvault.R
import com.qrvault.data.local.database.QrItem
import com.qrvault.data.repository.QrRepository
import com.qrvault.ui.appContainer
import com.qrvault.util.QrFormValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddQrUiState(
    val isEditing: Boolean = false,
    val item: QrItem? = null,
    val name: String = "",
    val provider: String = "",
    val description: String = "",
    val selectedImage: Uri? = null,
    val saving: Boolean = false,
    val errors: Set<QrFormValidator.ValidationError> = emptySet(),
) {
    val hasImage: Boolean get() = selectedImage != null || item != null
}

class AddQrViewModel(
    private val repository: QrRepository,
    itemId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddQrUiState(isEditing = itemId > 0))
    val uiState: StateFlow<AddQrUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddQrEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AddQrEvent> = _events

    sealed interface AddQrEvent {
        data class Saved(val id: Long) : AddQrEvent
        data class Failed(val messageRes: Int) : AddQrEvent
    }

    init {
        if (itemId > 0) {
            viewModelScope.launch {
                val current = repository.getById(itemId)
                if (current != null) {
                    _uiState.update {
                        it.copy(
                            isEditing = true,
                            item = current,
                            name = current.name,
                            provider = current.provider,
                            description = current.description.orEmpty(),
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                errors = it.errors - QrFormValidator.ValidationError.NAME_REQUIRED,
            )
        }
    }

    fun onProviderChange(value: String) {
        _uiState.update {
            it.copy(
                provider = value,
                errors = it.errors - QrFormValidator.ValidationError.PROVIDER_REQUIRED,
            )
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedImage = uri,
                errors = it.errors - QrFormValidator.ValidationError.IMAGE_REQUIRED,
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val errors = QrFormValidator.validate(state.name, state.provider, state.hasImage)
        _uiState.update { it.copy(errors = errors) }
        if (errors.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            val result = if (state.isEditing && state.item != null) {
                repository.updateQr(
                    item = state.item,
                    newImageUri = state.selectedImage,
                    name = state.name,
                    provider = state.provider,
                    description = state.description,
                ).map { state.item!!.id }
            } else {
                repository.addQr(
                    uri = checkNotNull(state.selectedImage),
                    name = state.name,
                    provider = state.provider,
                    description = state.description,
                )
            }
            _uiState.update { it.copy(saving = false) }
            result
                .onSuccess { id -> _events.tryEmit(AddQrEvent.Saved(id)) }
                .onFailure { _events.tryEmit(AddQrEvent.Failed(R.string.error_image_invalid)) }
        }
    }

    companion object {
        fun factory(itemId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer { AddQrViewModel(appContainer().repository, itemId) }
        }
    }
}