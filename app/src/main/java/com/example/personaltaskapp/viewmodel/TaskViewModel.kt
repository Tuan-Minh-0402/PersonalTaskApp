package com.example.personaltaskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repo: TaskRepository
) : ViewModel() {

    val tasks = repo.tasks
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val pomodoro = PomodoroManager(this)

    fun addTask(task: Task) {
        viewModelScope.launch { repo.addTask(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repo.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repo.deleteTask(task) }
    }
}
