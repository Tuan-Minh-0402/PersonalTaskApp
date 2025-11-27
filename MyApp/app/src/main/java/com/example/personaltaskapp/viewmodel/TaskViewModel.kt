package com.example.personaltaskapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(application.applicationContext)
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> get() = _tasks

    val pomodoro = PomodoroManager(this)

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = repository.getAllTasks()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insert(task)
            loadTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
            loadTasks()
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
            loadTasks()
        }
    }


}


