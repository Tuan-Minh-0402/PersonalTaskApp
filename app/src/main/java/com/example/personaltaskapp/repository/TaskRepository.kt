package com.example.personaltaskapp.repository

import com.example.personaltaskapp.dao.TaskDao
import com.example.personaltaskapp.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun getAllTasks(): Flow<List<Task>> = dao.getAllTasks()

    val tasks: Flow<List<Task>> get() = getAllTasks()

    fun getActiveTasks(): Flow<List<Task>> = dao.getActiveTasks()

    fun getFlexibleUnscheduledTasks(): Flow<List<Task>> = dao.getFlexibleUnscheduledTasks()

    fun getScheduledTasks(): Flow<List<Task>> = dao.getScheduledTasks()

    suspend fun addTask(task: Task) = dao.insert(task)

    suspend fun updateTask(task: Task) = dao.update(task)

    suspend fun deleteTask(task: Task) = dao.delete(task)
}
