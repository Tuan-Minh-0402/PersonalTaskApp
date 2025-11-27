package com.example.personaltaskapp.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.personaltaskapp.model.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * TaskFilter - provides reusable filters for the task list.
 * Used in TaskViewModel or directly in the UI to display only the relevant tasks.
 */
object TaskFilter {

    /**
     * Filter tasks by completion status.
     * @param tasks - list of all tasks
     * @param showCompleted - true to show completed, false to show active
     */
    fun filterByCompletion(tasks: List<Task>, showCompleted: Boolean): List<Task> {
        return tasks.filter { it.isCompleted == showCompleted }
    }

    /**
     * Filter tasks by priority level.
     * @param tasks - list of all tasks
     * @param level - 1 = High, 2 = Medium, 3 = Low
     */
    fun filterByPriority(tasks: List<Task>, level: Int): List<Task> {
        return tasks.filter { it.priority == level }
    }

    /**
     * Filter tasks that are due soon (e.g., within N days).
     * If dueDateIso is null, task is ignored.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun filterByDueDate(tasks: List<Task>, withinDays: Long = 3): List<Task> {
        val today = LocalDate.now()
        return tasks.filter { task ->
            task.dueDateIso?.let {
                val due = LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
                !due.isBefore(today) && due.isBefore(today.plusDays(withinDays))
            } ?: false
        }
    }

    /**
     * Filter tasks that have unfinished Pomodoro sessions.
     */
    fun filterByPomodoroPending(tasks: List<Task>): List<Task> {
        return tasks.filter { it.completedPomodoros < it.pomodoroCount }
    }

    /**
     * Combine multiple filters easily.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun filter(
        tasks: List<Task>,
        showCompleted: Boolean? = null,
        priority: Int? = null,
        dueWithinDays: Long? = null,
        pomodoroPendingOnly: Boolean = false
    ): List<Task> {
        var filtered = tasks
        showCompleted?.let { filtered = filterByCompletion(filtered, it) }
        priority?.let { filtered = filterByPriority(filtered, it) }
        dueWithinDays?.let { filtered = filterByDueDate(filtered, it) }
        if (pomodoroPendingOnly) filtered = filterByPomodoroPending(filtered)
        return filtered
    }
}
