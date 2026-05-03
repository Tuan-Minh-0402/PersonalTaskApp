// app/src/main/java/com/example/personaltaskapp/model/Habit.kt
package com.example.personaltaskapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String? = null,

    /**
     * Frequency string used in your UI and Calendar logic:
     * e.g. "DAILY" or "MON,WED,FRI" etc.
     *
     */
    val frequency: String,

    /**
     * Time of day in minutes since midnight, e.g. 8*60 = 08:00.
     * Used by UI time picker and display.
     */
    val startMinutes: Int = 8 * 60,

    /**
     * Duration in minutes for habit session.
     */
    val durationMinutes: Int = 30,

    /**
     * Progress fields
     */
    val streak: Int = 0,
    val lastCompletedIso: String? = null,
    val completedDatesIsoCsv: String = ""
)