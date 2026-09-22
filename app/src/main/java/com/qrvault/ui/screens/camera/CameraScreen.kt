package com.qrvault.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.qrvault.R
import java.io.File

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onCaptured: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
    }
    var capturedFile by remember { mutableStateOf<File?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (!hasPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.camera_permission_denied),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text(stringResource(R.string.camera_permission_title))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onBack) {
                    Text(stringResource(R.string.cancel))
                }
            }
            return@Box
        }

        val lifecycleOwner = LocalLifecycleOwner.current

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val executor = ContextCompat.getMainExecutor(ctx)
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = try {
                        providerFuture.get()
                    } catch (e: Exception) {
                        errorText = context.getString(R.string.camera_unavailable)
                        null
                    } ?: return@addListener
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture,
                        )
                        imageCapture.value = capture
                    } catch (e: Exception) {
                        errorText = context.getString(R.string.camera_unavailable)
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (capturedFile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    onClick = {
                        val capture = imageCapture.value ?: return@Surface
                        val file = File(context.cacheDir, "qr_capture_${System.currentTimeMillis()}.jpg")
                        val output = ImageCapture.OutputFileOptions.Builder(file).build()
                        capture.takePicture(
                            output,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    capturedFile = file
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    errorText = context.getString(R.string.camera_unavailable)
                                }
                            },
                        )
                    },
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(72.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = stringResource(R.string.capture),
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                AsyncImage(
                    model = capturedFile,
                    contentDescription = stringResource(R.string.content_desc_image_preview),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Fit,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { capturedFile = null },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.retake))
                    }
                    Button(
                        onClick = { onCaptured(Uri.fromFile(capturedFile)) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.use_photo))
                    }
                }
            }
        }

        errorText?.let { message ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = { errorText = null }) {
                    Text(stringResource(R.string.try_again))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onBack) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}