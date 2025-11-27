package com.example.personaltaskapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String? = null,
    val frequency: String,        // daily / weekly etc
    val streak: Int = 0,
    val lastCompletedIso: String? = null
)
