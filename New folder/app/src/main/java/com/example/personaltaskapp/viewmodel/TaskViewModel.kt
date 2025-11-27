package com.example.personaltaskapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.data.DatabaseModule
import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.repository.TaskRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    val tasks: StateFlow<List<Task>>

    init {
        val db = DatabaseModule.getDatabase(application)
        repository = TaskRepository(db.taskDao())

        tasks = repository.tasks.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    val pomodoro = PomodoroManager(this)

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.add(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }
}
