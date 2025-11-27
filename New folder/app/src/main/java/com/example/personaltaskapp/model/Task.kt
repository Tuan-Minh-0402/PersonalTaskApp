package com.example.personaltaskapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,

    // Scheduling
    val durationMinutes: Int = 30,
    val earliestStartIso: String? = null,
    val dueDateIso: String? = null,
    val priority: Int = 1,
    val fixedStartIso: String? = null,
    val isFlexible: Boolean = true,

    // Pomodoro
    val pomodoroCount: Int = 4,
    val completedPomodoros: Int = 0,
    val isPomodoroRunning: Boolean = false
)
