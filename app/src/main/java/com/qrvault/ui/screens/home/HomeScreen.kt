package com.qrvault.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qrvault.R
import com.qrvault.data.local.database.QrItem
import com.qrvault.ui.LocalAppContainer
import com.qrvault.ui.components.EmptyState
import com.qrvault.ui.components.QrGridCard
import com.qrvault.ui.components.QrListCard
import com.qrvault.util.SortOption
import com.qrvault.util.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val container = LocalAppContainer.current
    val settings by container.settingsRepository.settings
        .collectAsStateWithLifecycle(initialValue = com.qrvault.data.local.settings.AppSettings())
    val items by viewModel.items.collectAsStateWithLifecycle()
    val query by viewModel.queryFlow.collectAsStateWithLifecycle()
    val sort by viewModel.sortFlow.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.messages.collect { res ->
            snackbarHostState.showSnackbar(context.getString(res))
        }
    }

    var itemToDelete by remember { mutableStateOf<QrItem?>(null) }

    val deleteItem: (QrItem) -> Unit = { item ->
        if (settings.confirmDelete) {
            itemToDelete = item
        } else {
            viewModel.delete(item)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold) },
                actions = {
                    SortMenu(selected = sort, onSelect = viewModel::onSortChange)
                    ViewModeToggle(mode = viewMode, onToggle = { viewModel.toggleViewMode() })
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_qr))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(
                query = query,
                onQueryChange = viewModel::onQueryChange,
            )
            when {
                items.isEmpty() && query.isBlank() -> {
                    EmptyState(
                        title = stringResource(R.string.empty_state_title),
                        body = stringResource(R.string.empty_state_body),
                        actionLabel = stringResource(R.string.add_qr),
                        onAction = onAdd,
                    )
                }
                items.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.empty_search_title),
                        body = stringResource(R.string.empty_search_body, query),
                        actionLabel = null,
                        onAction = null,
                    )
                }
                viewMode == ViewMode.GRID -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 148.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            QrGridCard(
                                imageFile = container.imageStorage.fileFor(item.imagePath),
                                item = item,
                                onClick = { onOpen(item.id) },
                                onEdit = { onEdit(item.id) },
                                onDelete = { deleteItem(item) },
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            QrListCard(
                                imageFile = container.imageStorage.fileFor(item.imagePath),
                                item = item,
                                onClick = { onOpen(item.id) },
                                onEdit = { onEdit(item.id) },
                                onDelete = { deleteItem(item) },
                            )
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { item ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_body, item.name)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        itemToDelete = null
                        viewModel.delete(item)
                    },
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.clear_search),
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
        )
    }
}

@Composable
private fun SortMenu(
    selected: SortOption,
    onSelect: (SortOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.sort))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                    leadingIcon = {
                        if (option == selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.padding(0.dp),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ViewModeToggle(
    mode: ViewMode,
    onToggle: () -> Unit,
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (mode == ViewMode.GRID) Icons.AutoMirrored.Filled.List else Icons.Filled.GridView,
            contentDescription = if (mode == ViewMode.GRID) {
                stringResource(R.string.list_view)
            } else {
                stringResource(R.string.grid_view)
            },
        )
    }
}