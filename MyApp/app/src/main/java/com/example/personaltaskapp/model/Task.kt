package com.example.personaltaskapp.model

data class Task(
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,

    // Scheduling fields
    val durationMinutes: Int = 30,              // estimated duration in minutes
    val earliestStartIso: String? = null,       // e.g. "2025-10-31T09:00:00"
    val dueDateIso: String? = null,             // optional deadline
    val priority: Int = 1,                      // 1=low, 2=medium, 3=high
    val fixedStartIso: String? = null,          // non-null -> fixed appointment
    val isFlexible: Boolean = true,             // if false treat as fixed/manual

    //Pomodoro fields
    val pomodoroCount: Int = 4,         // total Pomodoro cycles per task
    val completedPomodoros: Int = 0,    // how many done
    val isPomodoroRunning: Boolean = false
)
