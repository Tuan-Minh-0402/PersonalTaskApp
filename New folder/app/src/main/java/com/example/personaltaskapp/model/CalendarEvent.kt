package com.example.personaltaskapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val dateIso: String,
    val fromTime: String? = null,   // HH:mm
    val toTime: String? = null      // HH:mm
)
