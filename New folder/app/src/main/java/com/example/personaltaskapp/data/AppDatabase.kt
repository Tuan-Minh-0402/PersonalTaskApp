package com.example.personaltaskapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.personaltaskapp.model.*

@Database(
    entities = [Task::class, Habit::class, CalendarEvent::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun calendarEventDao(): CalendarEventDao
}
