package com.example.lab08.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab08.ui.state.TaskEvent
import com.example.lab08.ui.state.TaskFilter
import com.example.lab08.ui.state.TaskUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    uiState: TaskUiState,
    onEvent: (TaskEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onEvent(TaskEvent.OnDismissError)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gestor de Tareas (MVVM + UDF)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (uiState.completedCount > 0) {
                        IconButton(onClick = { onEvent(TaskEvent.OnClearCompleted) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Limpiar completadas",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Formulario para agregar tarea
            AddTaskForm(
                title = uiState.titleInput,
                description = uiState.descriptionInput,
                onTitleChange = { onEvent(TaskEvent.OnTitleChange(it)) },
                onDescriptionChange = { onEvent(TaskEvent.OnDescriptionChange(it)) },
                onAddTask = { onEvent(TaskEvent.OnAddTask) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros de estado (UDF Event: OnFilterChange)
            FilterChipsRow(
                selectedFilter = uiState.currentFilter,
                pendingCount = uiState.pendingCount,
                completedCount = uiState.completedCount,
                totalCount = uiState.tasks.size,
                onFilterSelected = { onEvent(TaskEvent.OnFilterChange(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de tareas o indicador de carga
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (uiState.filteredTasks.isEmpty()) {
                    EmptyStateMessage(filter = uiState.currentFilter)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.filteredTasks,
                            key = { it.id }
                        ) { task ->
                            TaskItem(
                                task = task,
                                onToggle = { onEvent(TaskEvent.OnToggleTask(it)) },
                                onEdit = { onEvent(TaskEvent.OnEditTaskClick(it)) },
                                onDelete = { onEvent(TaskEvent.OnDeleteTask(it)) }
                            )
                        }
                    }
                }
            }

            // Diálogo para editar tarea
            if (uiState.editingTask != null) {
                EditTaskDialog(
                    title = uiState.editTitleInput,
                    description = uiState.editDescriptionInput,
                    onTitleChange = { onEvent(TaskEvent.OnEditTitleChange(it)) },
                    onDescriptionChange = { onEvent(TaskEvent.OnEditDescriptionChange(it)) },
                    onSave = { onEvent(TaskEvent.OnSaveEditTask) },
                    onDismiss = { onEvent(TaskEvent.OnCancelEditTask) }
                )
            }
        }
    }
}

@Composable
private fun AddTaskForm(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nueva Tarea",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Título de la tarea") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Descripción (opcional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddTask,
                enabled = title.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar Tarea")
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: TaskFilter,
    pendingCount: Int,
    completedCount: Int,
    totalCount: Int,
    onFilterSelected: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == TaskFilter.ALL,
            onClick = { onFilterSelected(TaskFilter.ALL) },
            label = { Text("Todas ($totalCount)") }
        )

        FilterChip(
            selected = selectedFilter == TaskFilter.PENDING,
            onClick = { onFilterSelected(TaskFilter.PENDING) },
            label = { Text("Pendientes ($pendingCount)") }
        )

        FilterChip(
            selected = selectedFilter == TaskFilter.COMPLETED,
            onClick = { onFilterSelected(TaskFilter.COMPLETED) },
            label = { Text("Completadas ($completedCount)") }
        )
    }
}

@Composable
private fun EmptyStateMessage(
    filter: TaskFilter,
    modifier: Modifier = Modifier
) {
    val message = when (filter) {
        TaskFilter.ALL -> "No hay tareas registradas. ¡Agrega una!"
        TaskFilter.PENDING -> "No tienes tareas pendientes 🎉"
        TaskFilter.COMPLETED -> "No hay tareas completadas aún"
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier.padding(16.dp)
    )
}

@Composable
private fun EditTaskDialog(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tarea") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Título de la tarea") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descripción (opcional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = title.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
