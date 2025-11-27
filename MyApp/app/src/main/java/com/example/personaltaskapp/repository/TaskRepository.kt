package com.example.personaltaskapp.repository

import android.content.Context
import com.example.personaltaskapp.data.TaskDao
import com.example.personaltaskapp.model.Task

class TaskRepository(context: Context) {
    private val dao = TaskDao(context)

    fun getAllTasks(): List<Task> = dao.getAllTasks()
    fun insert(task: Task) = dao.insertTask(task)
    fun update(task: Task) = dao.updateTask(task)
    fun delete(id: Int) = dao.deleteTask(id)
}
