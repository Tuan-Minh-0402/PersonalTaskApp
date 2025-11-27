package com.example.personaltaskapp.data

import android.content.ContentValues
import android.content.Context
import com.example.personaltaskapp.model.Habit

class HabitDao(context: Context) {
    private val dbHelper = HabitDatabaseHelper(context)

    fun getAllHabits(): List<Habit> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM habits", null)
        val habits = mutableListOf<Habit>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val startTimeIso = cursor.getString(cursor.getColumnIndexOrThrow("startTimeIso"))
            val durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("durationMinutes"))
            val daysOfWeek = cursor.getString(cursor.getColumnIndexOrThrow("daysOfWeek"))
            val isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1
            val lastCompletedIso = cursor.getString(cursor.getColumnIndexOrThrow("lastCompletedIso"))

            habits.add(
                Habit(
                    id = id,
                    title = title,
                    description = description,
                    startTimeIso = startTimeIso,
                    durationMinutes = durationMinutes,
                    daysOfWeek = daysOfWeek,
                    isActive = isActive,
                    lastCompletedIso = lastCompletedIso
                )
            )
        }

        cursor.close()
        db.close()
        return habits
    }

    fun insertHabit(habit: Habit) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", habit.title)
            put("description", habit.description)
            put("startTimeIso", habit.startTimeIso)
            put("durationMinutes", habit.durationMinutes)
            put("daysOfWeek", habit.daysOfWeek)
            put("isActive", if (habit.isActive) 1 else 0)
            put("lastCompletedIso", habit.lastCompletedIso)
        }
        db.insert("habits", null, values)
        db.close()
    }

    fun deleteHabit(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete("habits", "id = ?", arrayOf(id.toString()))
        db.close()
    }
}
