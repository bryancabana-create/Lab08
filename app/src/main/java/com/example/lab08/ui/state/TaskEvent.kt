package com.example.lab08.ui.state

import com.example.lab08.data.local.Task

sealed interface TaskEvent {
    data class OnTitleChange(val title: String) : TaskEvent
    data class OnDescriptionChange(val description: String) : TaskEvent
    data object OnAddTask : TaskEvent
    data class OnToggleTask(val task: Task) : TaskEvent
    data class OnDeleteTask(val task: Task) : TaskEvent
    data class OnEditTaskClick(val task: Task) : TaskEvent
    data class OnEditTitleChange(val title: String) : TaskEvent
    data class OnEditDescriptionChange(val description: String) : TaskEvent
    data object OnSaveEditTask : TaskEvent
    data object OnCancelEditTask : TaskEvent
    data class OnFilterChange(val filter: TaskFilter) : TaskEvent
    data object OnClearCompleted : TaskEvent
    data object OnDismissError : TaskEvent
}
