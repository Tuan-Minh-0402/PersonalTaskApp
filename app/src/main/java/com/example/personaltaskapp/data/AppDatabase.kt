package com.example.personaltaskapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.personaltaskapp.dao.CalendarEventDao
import com.example.personaltaskapp.dao.HabitDao
import com.example.personaltaskapp.dao.TaskDao
import com.example.personaltaskapp.model.CalendarEvent
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.model.Task

@Database(
    entities = [Task::class, Habit::class, CalendarEvent::class],
    version = 3,              // 🔥 increase version (important)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun calendarEventDao(): CalendarEventDao
}
