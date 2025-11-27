package com.example.personaltaskapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String? = null,

    // ISO LocalDate (yyyy-MM-dd)
    val dateIso: String,

    // ISO LocalTime (HH:mm)
    val startTimeIso: String,
    val type: String
)
