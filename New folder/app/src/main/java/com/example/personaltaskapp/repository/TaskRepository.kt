package com.example.personaltaskapp.repository

import com.example.personaltaskapp.data.TaskDao
import com.example.personaltaskapp.model.Task

class TaskRepository(private val dao: TaskDao) {

    val tasks = dao.getAllTasks()

    suspend fun add(task: Task) = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
}
