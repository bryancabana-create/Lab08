package com.example.lab08.ui.state

import com.example.lab08.data.local.Task

enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED
}

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val titleInput: String = "",
    val descriptionInput: String = "",
    val editingTask: Task? = null,
    val editTitleInput: String = "",
    val editDescriptionInput: String = "",
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredTasks: List<Task>
        get() = when (currentFilter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }

    val pendingCount: Int
        get() = tasks.count { !it.isCompleted }

    val completedCount: Int
        get() = tasks.count { it.isCompleted }
}
