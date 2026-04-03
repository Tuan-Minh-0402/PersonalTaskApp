package com.example.personaltaskapp.scheduler

import com.example.personaltaskapp.model.CalendarEvent
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.model.Task
import java.time.LocalDate

data class SmartScheduleInput(
    val tasks: List<Task>,
    val habits: List<Habit>,
    val events: List<CalendarEvent>,
    val targetDate: LocalDate
)
