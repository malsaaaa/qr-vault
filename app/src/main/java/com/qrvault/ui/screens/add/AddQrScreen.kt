package com.qrvault.ui.screens.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.qrvault.R
import com.qrvault.ui.LocalAppContainer
import com.qrvault.ui.screens.add.AddQrViewModel.AddQrEvent
import com.qrvault.util.QrFormValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQrScreen(
    itemId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onTakePhoto: () -> Unit,
    viewModel: AddQrViewModel = viewModel(key = "add_$itemId", factory = AddQrViewModel.factory(itemId)),
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onImageSelected(uri)
    }

    LaunchedEffect(Unit) {
        container.pendingImage.collect { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
                container.clearPendingImage()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddQrEvent.Saved -> onSaved()
                is AddQrEvent.Failed -> snackbarHostState.showSnackbar(
                    context.getString(event.messageRes),
                )
            }
        }
    }

    val imageToShow: Any? = uiState.selectedImage ?: uiState.item?.let {
        container.imageStorage.fileFor(it.imagePath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.isEditing) R.string.edit_qr_title else R.string.add_qr_title,
                        ),
                    )
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(
                            androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            ImagePreviewBox(image = imageToShow)

            if (uiState.errors.contains(QrFormValidator.ValidationError.IMAGE_REQUIRED)) {
                Text(
                    text = stringResource(R.string.error_image_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.import_from_gallery))
                }
                OutlinedButton(
                    onClick = onTakePhoto,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.take_photo))
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_name)) },
                placeholder = { Text(stringResource(R.string.field_name_hint)) },
                singleLine = true,
                isError = uiState.errors.contains(QrFormValidator.ValidationError.NAME_REQUIRED),
                supportingText = {
                    if (uiState.errors.contains(QrFormValidator.ValidationError.NAME_REQUIRED)) {
                        Text(stringResource(R.string.error_name_required))
                    }
                },
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.provider,
                onValueChange = viewModel::onProviderChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_provider)) },
                placeholder = { Text(stringResource(R.string.field_provider_hint)) },
                singleLine = true,
                isError = uiState.errors.contains(QrFormValidator.ValidationError.PROVIDER_REQUIRED),
                supportingText = {
                    if (uiState.errors.contains(QrFormValidator.ValidationError.PROVIDER_REQUIRED)) {
                        Text(stringResource(R.string.error_provider_required))
                    }
                },
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_description)) },
                placeholder = { Text(stringResource(R.string.field_description_hint)) },
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.saving,
            ) {
                if (uiState.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.save_qr))
                }
            }
        }
    }
}

@Composable
private fun ImagePreviewBox(image: Any?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (image == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = stringResource(R.string.content_desc_no_image),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.no_image_selected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            AsyncImage(
                model = image,
                contentDescription = stringResource(R.string.content_desc_image_preview),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f),
                contentScale = ContentScale.Fit,
            )
        }
    }
}