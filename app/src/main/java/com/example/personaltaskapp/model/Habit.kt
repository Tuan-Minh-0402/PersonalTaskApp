package com.example.personaltaskapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Advanced Habit entity (keeps prior DB fields and UI fields)
 * Minimal-change: retains frequency/streak/lastCompletedIso and also
 * provides scheduling fields used by HabitScreen (startTimeIso, durationMinutes, daysOfWeek).
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String? = null,

    // Scheduling fields (used by HabitScreen)
    val startTimeIso: String? = null,   // e.g. "2025-11-03T07:00:00" (we only care about time portion)
    val durationMinutes: Int = 30,
    val daysOfWeek: String? = null,     // "MON,WED,FRI" etc

    // Existing DB fields (kept, minimal-change)
    val frequency: String = "WEEKLY",   // DAILY / WEEKLY / CUSTOM
    val streak: Int = 0,
    val lastCompletedIso: String? = null
)
