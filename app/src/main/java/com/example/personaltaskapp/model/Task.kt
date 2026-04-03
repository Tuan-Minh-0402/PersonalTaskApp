package com.example.personaltaskapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Basic
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,

    // Scheduling / Smart-scheduler fields
    val estimatedMinutes: Int = 30,
    val earliestStartIso: String? = null,
    val latestEndIso: String? = null,
    val mustFinishToday: Boolean = false,
    val minSessionMinutes: Int = 25,
    val maxSessionMinutes: Int = 60,

    // Fixed schedule (manual)
    val fixedStartIso: String? = null,

    // ⭐ NEWLY RESTORED FIELD ⭐
    val dueDateIso: String? = null,   // <------- ADD THIS BACK

    // Priority & flexibility
    val priority: Int = 2,
    val isFlexible: Boolean = true,

    // Progress
    val completedMinutes: Int = 0,

    // Pomodoro
    val pomodoroCount: Int = 0,
    val completedPomodoros: Int = 0
)
