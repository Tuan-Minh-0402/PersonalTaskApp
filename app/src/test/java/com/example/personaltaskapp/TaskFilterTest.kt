package com.example.personaltaskapp

import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.utils.TaskFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskFilterTest {

    @Test
    fun filterByCompletion_returnsActiveOnly() {
        val tasks = listOf(
            Task(id = 1, title = "A", isCompleted = false),
            Task(id = 2, title = "B", isCompleted = true)
        )

        val active = TaskFilter.filterByCompletion(tasks, showCompleted = false)

        assertEquals(1, active.size)
        assertEquals(1, active.first().id)
    }

    @Test
    fun filterByDueDate_returnsOnlyFutureInWindow() {
        val today = LocalDate.now()
        val tasks = listOf(
            Task(id = 1, title = "today", dueDateIso = today.toString()),
            Task(id = 2, title = "soon", dueDateIso = today.plusDays(2).toString()),
            Task(id = 3, title = "outside", dueDateIso = today.plusDays(8).toString()),
            Task(id = 4, title = "past", dueDateIso = today.minusDays(1).toString())
        )

        val dueSoon = TaskFilter.filterByDueDate(tasks, withinDays = 7)

        val ids = dueSoon.map { it.id }
        assertTrue(ids.contains(1))
        assertTrue(ids.contains(2))
        assertTrue(!ids.contains(3))
        assertTrue(!ids.contains(4))
    }

    @Test
    fun filter_combinesDonePriorityAndPomodoroPending() {
        val tasks = listOf(
            Task(id = 1, title = "done pending", isCompleted = true, priority = 2, pomodoroCount = 2, completedPomodoros = 1),
            Task(id = 2, title = "done finished", isCompleted = true, priority = 2, pomodoroCount = 2, completedPomodoros = 2),
            Task(id = 3, title = "active pending", isCompleted = false, priority = 2, pomodoroCount = 2, completedPomodoros = 1)
        )

        val result = TaskFilter.filter(
            tasks = tasks,
            showCompleted = true,
            priority = 2,
            pomodoroPendingOnly = true
        )

        assertEquals(1, result.size)
        assertEquals(1, result.first().id)
    }
}