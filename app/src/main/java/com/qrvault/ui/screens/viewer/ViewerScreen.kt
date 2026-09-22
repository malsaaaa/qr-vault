package com.qrvault.ui.screens.viewer

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.qrvault.R
import com.qrvault.ui.LocalAppContainer

@Composable
fun ViewerScreen(
    itemId: Long,
    onBack: () -> Unit,
    viewModel: ViewerViewModel = viewModel(factory = ViewerViewModel.factory(itemId)),
) {
    val container = LocalAppContainer.current
    val item by viewModel.item.collectAsStateWithLifecycle()
    var controlsVisible by remember { mutableStateOf(true) }

    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    BackHandler { onBack() }

    LaunchedEffect(controlsVisible) {
        if (window != null) {
            val controller = WindowInsetsControllerCompat(window, view)
            if (controlsVisible) {
                controller.show(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (window != null) {
                WindowInsetsControllerCompat(window, view).show(
                    WindowInsetsCompat.Type.systemBars(),
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { controlsVisible = !controlsVisible },
        contentAlignment = Alignment.Center,
    ) {
        item?.let { current ->
            AsyncImage(
                model = container.imageStorage.fileFor(current.imagePath),
                contentDescription = stringResource(
                    R.string.content_desc_qr_item,
                    current.provider,
                    current.name,
                ),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.30f)),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.35f),
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = Color.White,
                            )
                        }
                    }
                    item?.let { current ->
                        Text(
                            text = current.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}