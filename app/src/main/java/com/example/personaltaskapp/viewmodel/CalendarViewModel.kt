package com.example.personaltaskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarViewModel(
    private val calendarRepo: CalendarRepository,
    private val taskRepo: TaskRepository,
    private val habitRepo: HabitRepository
) : ViewModel() {
    companion object {
        private const val TAG = "CalendarViewModel"
    }

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
    private val appliedSuggestionKeys = MutableStateFlow(setOf<String>())

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

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val task = allTasks.value.find { it.id == taskId } ?: return@launch
            taskRepo.updateTask(task.copy(isCompleted = isCompleted))
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

    fun isHabitCompletedOnDate(habit: Habit, date: LocalDate): Boolean {
        val key = date.toString()
        return habit.completedDatesSet().contains(key)
    }

    fun updateHabitCompletionForDate(habitId: Int, date: LocalDate, isCompleted: Boolean) {
        viewModelScope.launch {
            val habit = allHabits.value.find { it.id == habitId } ?: return@launch
            val key = date.toString()
            val completedDates = habit.completedDatesSet().toMutableSet()

            if (isCompleted) {
                completedDates.add(key)
            } else {
                completedDates.remove(key)
            }

            val streakForMonth = completedDates.count { iso ->
                runCatching { YearMonth.from(LocalDate.parse(iso)) }.getOrNull() == YearMonth.from(date)
            }

            val lastCompletedIso = completedDates.maxOrNull()
            val updated = habit.copy(
                streak = streakForMonth,
                lastCompletedIso = lastCompletedIso,
                completedDatesIsoCsv = completedDates.sorted().joinToString(",")
            )
            habitRepo.updateHabit(updated)
        }
    }

    fun smartSuggestions(date: LocalDate): List<SmartSuggestion> {
        val schedulerInput = buildSchedulerInput(date)
        val schedulerResult = SmartScheduler.scheduleDay(schedulerInput)
        return mapScheduleResultToSuggestions(
            date = date,
            result = schedulerResult,
            sourceTasks = allTasks.value
        ).filterNot { suggestion ->
            buildSuggestionKey(suggestion) in appliedSuggestionKeys.value
        }
    }

    fun applySmartSuggestion(s: SmartSuggestion, date: LocalDate) {
        viewModelScope.launch {
            if (s.taskId > 0) {
                val task = allTasks.value.find { it.id == s.taskId }
                if (task != null) {
                    val startIso = s.suggestedStartIso ?: date.atStartOfDay().toString()
                    val updated = task.copy(
                        fixedStartIso = startIso
                    )
                    taskRepo.updateTask(updated)
                    appliedSuggestionKeys.value = appliedSuggestionKeys.value + buildSuggestionKey(s)
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
                        durationMinutes = habit.durationMinutes.coerceAtLeast(1),
                        type = "HABIT"
                    )
                    val duplicateExists = allEvents.value.any { existing ->
                        existing.title == evt.title &&
                                existing.dateIso == evt.dateIso &&
                                existing.startTimeIso == evt.startTimeIso &&
                                existing.type == evt.type
                    }
                    if (!duplicateExists) {
                        calendarRepo.insertEvent(evt)
                    }
                    appliedSuggestionKeys.value = appliedSuggestionKeys.value + buildSuggestionKey(s)
                }
            }
        }
    }

    fun addCalendarEvent(
        title: String,
        desc: String?,
        dateIso: String,
        timeIso: String,
        durationMinutes: Int,
        type: String
    ) {
        viewModelScope.launch {
            calendarRepo.insertEvent(
                CalendarEvent(
                    title = title,
                    description = desc,
                    dateIso = dateIso,
                    startTimeIso = timeIso,
                    durationMinutes = durationMinutes.coerceAtLeast(1),
                    type = type
                )
            )
        }
    }

    fun updateCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch { calendarRepo.updateEvent(event) }
    }

    fun deleteCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch { calendarRepo.deleteEvent(event) }
    }

    private fun buildSchedulerInput(selectedDate: LocalDate): SmartSchedulerInput {
        val schedulerTasks = allTasks.value
            .filter { task ->
                !task.isCompleted &&
                        task.fixedStartIso.isNullOrBlank()
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
            event.toBusyBlock(selectedDate)
        }

        val fixedTaskBlocks = allTasks.value.mapNotNull { task ->
            val fixedStartIso = task.fixedStartIso ?: return@mapNotNull null
            val durationMinutes = task.estimatedMinutes
            if (durationMinutes <= 0) return@mapNotNull null

            val start = runCatching { LocalDateTime.parse(fixedStartIso) }.getOrNull()
                ?: return@mapNotNull null
            if (start.toLocalDate() != selectedDate) return@mapNotNull null

            SmartSchedulerBusyBlock(
                start = start,
                end = start.plusMinutes(durationMinutes.toLong())
            )
        }
        Log.d(
            TAG,
            "buildSchedulerInput(${selectedDate}): fixedTaskBlocks=${fixedTaskBlocks.size}"
        )

        return SmartSchedulerInput(
            selectedDate = selectedDate,
            tasks = schedulerTasks,
            habitBlocks = habitBlocks,
            eventBlocks = eventBlocks + fixedTaskBlocks
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
                reason = buildSuggestionReason(
                    overdueLevel = block.overdueLevel,
                    urgencyLevel = block.urgencyLevel,
                    priority = block.priorityValue,
                    fitLevel = block.fitLevel,
                    baseScore = block.baseScore
                ),
                confidence = 0.9f,
                suggestedStartIso = block.start.toString(),
                suggestedEndIso = block.end.toString()
            )
        }
    }


    private fun buildSuggestionReason(
        overdueLevel: Int,
        urgencyLevel: Int,
        priority: Int,
        fitLevel: Int,
        baseScore: Int
    ): String {
        val reasons = mutableListOf<String>()
        if (overdueLevel > 0) reasons += "Overdue task"
        if (urgencyLevel >= 2) reasons += "Deadline is near"
        if (priority >= 3) reasons += "High priority"
        if (fitLevel >= 2) reasons += "Fits available time slot"

        if (reasons.isNotEmpty()) return reasons.take(2).joinToString(" • ")

        return if (baseScore >= 40) "Important task"
        else "Suggested by scheduler"
    }

    private fun buildSuggestionKey(s: SmartSuggestion): String {
        return "${s.taskId}|${s.suggestedDateIso}|${s.suggestedStartIso ?: ""}|${s.suggestedEndIso ?: ""}"
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
        selectedDate: LocalDate
    ): SmartSchedulerBusyBlock? {
        val startTime = runCatching { LocalTime.parse(startTimeIso.take(5)) }.getOrNull() ?: return null
        val start = LocalDateTime.of(selectedDate, startTime)
        val duration = durationMinutes.coerceAtLeast(1).toLong()
        return SmartSchedulerBusyBlock(
            start = start,
            end = start.plusMinutes(duration)
        )
    }

    private fun Habit.completedDatesSet(): Set<String> {
        if (completedDatesIsoCsv.isBlank()) return emptySet()
        return completedDatesIsoCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
    }
}