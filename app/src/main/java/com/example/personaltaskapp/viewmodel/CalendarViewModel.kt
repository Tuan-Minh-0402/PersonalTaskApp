package com.example.personaltaskapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.model.CalendarEvent
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.repository.CalendarRepository
import com.example.personaltaskapp.repository.HabitRepository
import com.example.personaltaskapp.repository.TaskRepository
import com.example.personaltaskapp.scheduler.SmartScheduleInput
import com.example.personaltaskapp.scheduler.SmartScheduler
import com.example.personaltaskapp.scheduler.SmartSuggestion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class   CalendarViewModel(
    private val calendarRepo: CalendarRepository,
    private val taskRepo: TaskRepository,
    private val habitRepo: HabitRepository
) : ViewModel() {

    // ------------------------
    // LIVE FLOWS
    // ------------------------
    val allEvents: StateFlow<List<CalendarEvent>> =
        calendarRepo.getAllEvents()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTasks: StateFlow<List<Task>> =
        taskRepo.tasks
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allHabits: StateFlow<List<Habit>> =
        habitRepo.habits
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE

    // ------------------------
    // FILTER HELPERS
    // ------------------------
    fun eventsFor(date: LocalDate): List<CalendarEvent> {
        val key = date.format(isoDate)
        return allEvents.value.filter { it.dateIso.take(10) == key }
    }

    fun tasksFor(date: LocalDate): List<Task> {
        val key = date.format(isoDate)
        return allTasks.value.filter { t ->
            val fixed = t.fixedStartIso?.take(10) == key
            val earliest = t.earliestStartIso?.take(10) == key
            fixed || earliest
        }
    }

    fun habitsFor(date: LocalDate): List<Habit> {
        val dow3 = date.dayOfWeek.name.take(3).uppercase() // "MON"
        return allHabits.value.filter { h ->
            val freq = h.frequency.uppercase()

            freq == "DAILY" ||
                    (freq == "WEEKDAYS" && dow3 in listOf("MON","TUE","WED","THU","FRI")) ||
                    (freq == "WEEKENDS" && dow3 in listOf("SAT","SUN")) ||
                    (freq.isNotBlank() && freq.split(",").map { it.trim() }.contains(dow3))
        }
    }

    // ------------------------
    // SMART SUGGESTIONS
    // ------------------------
    fun smartSuggestions(date: LocalDate): List<SmartSuggestion> {
        return SmartScheduler.generateSuggestions(
            SmartScheduleInput(
                tasks = allTasks.value,
                habits = allHabits.value,
                events = allEvents.value,
                targetDate = date
            )
        )
    }

    // ------------------------
    // APPLY A SMART SUGGESTION
    // ------------------------
    fun applySmartSuggestion(s: SmartSuggestion, date: LocalDate) {
        viewModelScope.launch {

            // If suggestion represents a TASK
            if (s.taskId > 0) {
                val task = allTasks.value.find { it.id == s.taskId }
                if (task != null) {
                    val updated = task.copy(
                        fixedStartIso = date.atStartOfDay().toString()
                    )
                    taskRepo.updateTask(updated)
                }
            }

            // If suggestion represents a HABIT → create event
            if (s.taskId < 0) {
                val habitId = -s.taskId
                val habit = allHabits.value.find { it.id == habitId }
                if (habit != null) {
                    val hour = habit.startMinutes / 60
                    val minute = habit.startMinutes % 60
                    val evt = CalendarEvent(
                        title = habit.title,
                        description = "Auto-scheduled habit",
                        dateIso = date.toString(),
                        startTimeIso = "%02d:%02d".format(hour, minute),
                        type = "HABIT"
                    )
                    calendarRepo.insertEvent(evt)
                }
            }
        }
    }

    // ------------------------
    // EVENT CREATION
    // ------------------------
    fun addCalendarEvent(
        title: String,
        desc: String?,
        dateIso: String,
        timeIso: String,
        type: String
    ) {
        viewModelScope.launch {
            calendarRepo.insertEvent(
                CalendarEvent(
                    title = title,
                    description = desc,
                    dateIso = dateIso,
                    startTimeIso = timeIso,
                    type = type
                )
            )
        }
    }
}
