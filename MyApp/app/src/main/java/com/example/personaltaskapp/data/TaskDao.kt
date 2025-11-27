package com.example.personaltaskapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.personaltaskapp.model.Task

class TaskDao(private val context: Context) {

    private val dbHelper = TaskDatabaseHelper(context)

    fun getAllTasks(): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM tasks", null)
        val tasks = mutableListOf<Task>()

        if (cursor.moveToFirst()) {
            do {
                tasks.add(
                    Task(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("durationMinutes")),
                        earliestStartIso = cursor.getString(cursor.getColumnIndexOrThrow("earliestStartIso")),
                        dueDateIso = cursor.getString(cursor.getColumnIndexOrThrow("dueDateIso")),
                        fixedStartIso = cursor.getString(cursor.getColumnIndexOrThrow("fixedStartIso")),
                        priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority")),
                        isFlexible = cursor.getInt(cursor.getColumnIndexOrThrow("isFlexible")) == 1,
                        isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")) == 1,
                        pomodoroCount = cursor.getInt(cursor.getColumnIndexOrThrow("pomodoroCount")),
                        completedPomodoros = cursor.getInt(cursor.getColumnIndexOrThrow("completedPomodoros")),
                        isPomodoroRunning = cursor.getInt(cursor.getColumnIndexOrThrow("isPomodoroRunning")) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tasks
    }

    fun insertTask(task: Task) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", task.title)
            put("description", task.description)
            put("durationMinutes", task.durationMinutes)
            put("earliestStartIso", task.earliestStartIso)
            put("dueDateIso", task.dueDateIso)
            put("fixedStartIso", task.fixedStartIso)
            put("priority", task.priority)
            put("isFlexible", if (task.isFlexible) 1 else 0)
            put("isCompleted", if (task.isCompleted) 1 else 0)
            put("pomodoroCount", task.pomodoroCount)
            put("completedPomodoros", task.completedPomodoros)
            put("isPomodoroRunning", if (task.isPomodoroRunning) 1 else 0)
        }
        db.insert("tasks", null, values)
        db.close()
    }

    fun updateTask(task: Task) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", task.title)
            put("description", task.description)
            put("durationMinutes", task.durationMinutes)
            put("dueDateIso", task.dueDateIso)
            put("priority", task.priority)
            put("isFlexible", if (task.isFlexible) 1 else 0)
            put("isCompleted", if (task.isCompleted) 1 else 0)
            put("pomodoroCount", task.pomodoroCount)
            put("completedPomodoros", task.completedPomodoros)
            put("isPomodoroRunning", if (task.isPomodoroRunning) 1 else 0)
        }
        db.update("tasks", values, "id = ?", arrayOf(task.id.toString()))
        db.close()
    }

    fun deleteTask(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete("tasks", "id = ?", arrayOf(id.toString()))
        db.close()
    }
}
