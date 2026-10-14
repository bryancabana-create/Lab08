package com.example.lab08.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lab08.data.local.Task
import com.example.lab08.data.repository.TaskRepository
import com.example.lab08.ui.state.TaskEvent
import com.example.lab08.ui.state.TaskUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState(isLoading = true))
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.getAllTasks()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.localizedMessage ?: "Error al cargar tareas"
                        )
                    }
                }
                .collect { taskList ->
                    _uiState.update {
                        it.copy(
                            tasks = taskList,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.OnTitleChange -> {
                _uiState.update { it.copy(titleInput = event.title) }
            }

            is TaskEvent.OnDescriptionChange -> {
                _uiState.update { it.copy(descriptionInput = event.description) }
            }

            TaskEvent.OnAddTask -> {
                addTask()
            }

            is TaskEvent.OnToggleTask -> {
                toggleTask(event.task)
            }

            is TaskEvent.OnDeleteTask -> {
                deleteTask(event.task)
            }

            is TaskEvent.OnEditTaskClick -> {
                _uiState.update {
                    it.copy(
                        editingTask = event.task,
                        editTitleInput = event.task.title,
                        editDescriptionInput = event.task.description
                    )
                }
            }

            is TaskEvent.OnEditTitleChange -> {
                _uiState.update { it.copy(editTitleInput = event.title) }
            }

            is TaskEvent.OnEditDescriptionChange -> {
                _uiState.update { it.copy(editDescriptionInput = event.description) }
            }

            TaskEvent.OnSaveEditTask -> {
                saveEditTask()
            }

            TaskEvent.OnCancelEditTask -> {
                _uiState.update {
                    it.copy(
                        editingTask = null,
                        editTitleInput = "",
                        editDescriptionInput = ""
                    )
                }
            }

            is TaskEvent.OnFilterChange -> {
                _uiState.update { it.copy(currentFilter = event.filter) }
            }

            TaskEvent.OnClearCompleted -> {
                clearCompleted()
            }

            TaskEvent.OnDismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun addTask() {
        val title = _uiState.value.titleInput.trim()
        if (title.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El título de la tarea no puede estar vacío") }
            return
        }

        val description = _uiState.value.descriptionInput.trim()

        viewModelScope.launch {
            try {
                repository.insertTask(Task(title = title, description = description))
                _uiState.update {
                    it.copy(
                        titleInput = "",
                        descriptionInput = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al guardar la tarea: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun saveEditTask() {
        val taskToEdit = _uiState.value.editingTask ?: return
        val newTitle = _uiState.value.editTitleInput.trim()
        if (newTitle.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El título de la tarea no puede estar vacío") }
            return
        }

        val newDescription = _uiState.value.editDescriptionInput.trim()

        viewModelScope.launch {
            try {
                repository.updateTask(
                    taskToEdit.copy(
                        title = newTitle,
                        description = newDescription
                    )
                )
                _uiState.update {
                    it.copy(
                        editingTask = null,
                        editTitleInput = "",
                        editDescriptionInput = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al editar la tarea: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun toggleTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.updateTask(task.copy(isCompleted = !task.isCompleted))
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al actualizar la tarea: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al eliminar la tarea: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun clearCompleted() {
        viewModelScope.launch {
            try {
                repository.deleteCompletedTasks()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error al eliminar tareas completadas: ${e.localizedMessage}")
                }
            }
        }
    }

    companion object {
        fun Factory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskViewModel(repository) as T
                }
            }
    }
}
