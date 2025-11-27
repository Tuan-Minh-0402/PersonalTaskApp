package com.example.personaltaskapp.model

data class CalendarEvent(
    val eventId: String = "",          // ID from Google Calendar or local database
    val title: String,                 // Event title (e.g., "Morning Run")
    val description: String? = null,   // Optional description
    val startTime: Long,               // In milliseconds
    val endTime: Long,                 // In milliseconds
    val recurrenceRule: String? = null,// e.g., "FREQ=DAILY" for habits
    val sourceType: String = "habit",  // "habit", "task", "pomodoro"
    val linkedItemId: Int? = null      // Link back to habit/task id
)
