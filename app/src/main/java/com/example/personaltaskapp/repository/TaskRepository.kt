package com.example.personaltaskapp.repository

import com.example.personaltaskapp.dao.TaskDao
import com.example.personaltaskapp.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    // existing flow
    val tasks: Flow<List<Task>> = dao.getAllTasks()

    suspend fun addTask(task: Task) = dao.insert(task)

    suspend fun updateTask(task: Task) = dao.update(task)

    suspend fun deleteTask(task: Task) = dao.delete(task)

    // new helpers
    suspend fun getTaskById(id: Int): Task? = dao.getById(id)

    fun incompleteTasksFlow(): Flow<List<Task>> = dao.getIncompleteTasks()

    fun tasksBetween(fromIso: String, toIso: String): Flow<List<Task>> =
        dao.getTasksBetweenDates(fromIso, toIso)

    fun flexibleTasksFlow(): Flow<List<Task>> = dao.getFlexibleTasks()
}
