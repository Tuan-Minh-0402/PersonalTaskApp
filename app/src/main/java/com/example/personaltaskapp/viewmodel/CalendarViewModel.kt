package com.example.personaltaskapp.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.model.CalendarEvent
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.repository.CalendarRepository
import com.example.personaltaskapp.repository.HabitRepository
import com.example.personaltaskapp.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarViewModel(
    private val calendarRepo: CalendarRepository,
    private val taskRepo: TaskRepository,
    private val habitRepo: HabitRepository
) : ViewModel() {

    // All flows (single source of truth)
    val allEvents: StateFlow<List<CalendarEvent>> = calendarRepo.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTasks: StateFlow<List<Task>> = taskRepo.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allHabits: StateFlow<List<Habit>> = habitRepo.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Formatter helpers
    private val isoDateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    // Query helpers -- return lists computed from the current caches
    fun eventsFor(date: LocalDate): List<CalendarEvent> {
        val keyIso = date.format(isoDateFmt)
        return allEvents.value.filter { it.dateIso.take(10) == keyIso } // assuming dateIso stores full ISO
    }

    fun tasksFor(date: LocalDate): List<Task> {
        val keyIso = date.format(isoDateFmt)
        return allTasks.value.filter { task ->
            // scheduled fixedStartIso (same day) OR dueDateIso equals this day
            val fixedSame = task.fixedStartIso?.take(10) == keyIso
            val dueSame = task.dueDateIso?.take(10) == keyIso
            fixedSame || dueSame
        }
    }

    fun habitsFor(date: LocalDate): List<Habit> {
        val dow3 = date.dayOfWeek.name.take(3).uppercase() // MON,TUE,...
        return allHabits.value.filter { habit ->
            // simple CSV parse; habit.daysOfWeek expected like "MON,WED,FRI" or "DAILY"
            habit.frequency.equals("DAILY", ignoreCase = true) ||
                    habit.frequency.split(",").map { it.trim().uppercase() }.contains(dow3)
        }
    }

    // Smart-scheduler stub: propose simple suggestions for the selected date
    @RequiresApi(Build.VERSION_CODES.O)
    fun suggestionsFor(date: LocalDate): List<Suggestion> {
        // Basic policy: find flexible tasks with no fixedStartIso and due after or equal to date
        val keyIso = date.format(isoDateFmt)
        val flexTasks = allTasks.value.filter { t ->
            t.isFlexible && t.fixedStartIso == null && (t.dueDateIso == null || t.dueDateIso.take(10) >= keyIso)
        }
        // Turn them into simple "suggestion" items (you can extend with time slots)
        return flexTasks.map { Suggestion(taskId = it.id, title = it.title, suggestedDateIso = keyIso) }
    }

    // Example actions
    fun scheduleTaskOn(task: Task, date: LocalDate) {
        viewModelScope.launch {
            val scheduled = task.copy(fixedStartIso = date.atStartOfDay().toString())
            taskRepo.updateTask(scheduled)
        }
    }

    fun markHabitDone(habit: Habit, date: LocalDate) {
        viewModelScope.launch {
            // Update lastCompletedIso and increment streak (simple heuristics)
            val newHabit = habit.copy(lastCompletedIso = date.toString(), streak = habit.streak + 1)
            habitRepo.updateHabit(newHabit)
        }
    }

    fun addCalendarEvent(title: String, date: LocalDate) {
        viewModelScope.launch {
            calendarRepo.insertEvent(CalendarEvent(title = title, dateIso = date.toString()))
        }
    }

    // Minimal data holder
    data class Suggestion(val taskId: Int, val title: String, val suggestedDateIso: String)
}
