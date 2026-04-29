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
import com.example.personaltaskapp.scheduler.SmartScheduler
import com.example.personaltaskapp.scheduler.SmartSchedulerBusyBlock
import com.example.personaltaskapp.scheduler.SmartSchedulerInput
import com.example.personaltaskapp.scheduler.SmartSchedulerResult
import com.example.personaltaskapp.scheduler.SmartSchedulerTask
import com.example.personaltaskapp.scheduler.SmartSuggestion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CalendarViewModel(
    private val calendarRepo: CalendarRepository,
    private val taskRepo: TaskRepository,
    private val habitRepo: HabitRepository
) : ViewModel() {

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
    private val defaultEventDurationMinutes = 60L

    fun eventsFor(date: LocalDate): List<CalendarEvent> {
        val key = date.format(isoDate)
        return allEvents.value.filter { it.dateIso.take(10) == key }
    }

    fun tasksFor(date: LocalDate): List<Task> {
        val key = date.format(isoDate)
        return allTasks.value.filter { task ->
            val fixed = task.fixedStartIso?.take(10) == key
            val earliest = task.earliestStartIso?.take(10) == key
            fixed || earliest
        }
    }

    fun habitsFor(date: LocalDate): List<Habit> {
        val dow3 = date.dayOfWeek.name.take(3).uppercase()
        return allHabits.value.filter { habit ->
            val freq = habit.frequency.uppercase()
            freq == "DAILY" ||
                    (freq == "WEEKDAYS" && dow3 in listOf("MON", "TUE", "WED", "THU", "FRI")) ||
                    (freq == "WEEKENDS" && dow3 in listOf("SAT", "SUN")) ||
                    (freq.isNotBlank() && freq.split(",").map { it.trim() }.contains(dow3))
        }
    }

    fun smartSuggestions(date: LocalDate): List<SmartSuggestion> {
        val schedulerInput = buildSchedulerInput(date)
        val schedulerResult = SmartScheduler.scheduleDay(schedulerInput)
        return mapScheduleResultToSuggestions(
            date = date,
            result = schedulerResult,
            sourceTasks = allTasks.value
        )
    }

    fun applySmartSuggestion(s: SmartSuggestion, date: LocalDate) {
        viewModelScope.launch {
            if (s.taskId > 0) {
                val task = allTasks.value.find { it.id == s.taskId }
                if (task != null) {
                    val updated = task.copy(
                        fixedStartIso = date.atStartOfDay().toString()
                    )
                    taskRepo.updateTask(updated)
                }
            }

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

    private fun buildSchedulerInput(selectedDate: LocalDate): SmartSchedulerInput {
        val schedulerTasks = allTasks.value
            .filter { task ->
                !task.isCompleted &&
                        task.fixedStartIso.isNullOrBlank() &&
                        task.earliestStartIso.isNullOrBlank()
            }
            .map { task -> task.toSchedulerTask() }

        val habitBlocks = habitsFor(selectedDate).map { habit ->
            val start = selectedDate.atTime(habit.startMinutes / 60, habit.startMinutes % 60)
            val duration = habit.durationMinutes.coerceAtLeast(1).toLong()
            SmartSchedulerBusyBlock(
                start = start,
                end = start.plusMinutes(duration)
            )
        }

        val eventBlocks = eventsFor(selectedDate).mapNotNull { event ->
            event.toBusyBlock(selectedDate, defaultEventDurationMinutes)
        }

        return SmartSchedulerInput(
            selectedDate = selectedDate,
            tasks = schedulerTasks,
            habitBlocks = habitBlocks,
            eventBlocks = eventBlocks
        )
    }

    private fun mapScheduleResultToSuggestions(
        date: LocalDate,
        result: SmartSchedulerResult,
        sourceTasks: List<Task>
    ): List<SmartSuggestion> {
        val taskById = sourceTasks.associateBy { it.id.toString() }

        return result.scheduled.mapNotNull { block ->
            val task = taskById[block.taskId] ?: return@mapNotNull null
            SmartSuggestion(
                taskId = task.id,
                title = task.title,
                suggestedDateIso = date.toString(),
                reason = "Scheduled ${block.start.toLocalTime()} - ${block.end.toLocalTime()}",
                confidence = 0.9f
            )
        }
    }

    private fun Task.toSchedulerTask(): SmartSchedulerTask {
        val duration = estimatedMinutes.coerceAtLeast(1)
        val parsedDeadline = dueDateIso
            ?.take(10)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        return SmartSchedulerTask(
            id = id.toString(),
            durationMinutes = duration,
            priority = priority,
            deadline = parsedDeadline,
            isCompleted = isCompleted
        )
    }

    private fun CalendarEvent.toBusyBlock(
        selectedDate: LocalDate,
        fallbackDurationMinutes: Long
    ): SmartSchedulerBusyBlock? {
        val startTime = runCatching { LocalTime.parse(startTimeIso.take(5)) }.getOrNull() ?: return null
        val start = LocalDateTime.of(selectedDate, startTime)
        return SmartSchedulerBusyBlock(
            start = start,
            end = start.plusMinutes(fallbackDurationMinutes)
        )
    }
}