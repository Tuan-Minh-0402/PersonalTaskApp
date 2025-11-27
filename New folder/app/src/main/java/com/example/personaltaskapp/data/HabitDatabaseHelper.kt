package com.example.personaltaskapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.model.HabitFrequency
import org.json.JSONArray

class HabitDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "habit_db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                isActive INTEGER,

                -- scheduling fields
                isFlexible INTEGER,
                durationMinutes INTEGER,
                earliestStartIso TEXT,
                fixedStartIso TEXT,
                preferredTimeBucket TEXT,
                assignedStartIso TEXT,
                assignedEndIso TEXT,

                -- frequency fields
                frequency TEXT,
                customWeekdays TEXT,

                -- tracking
                lastCompletedIso TEXT,
                streak INTEGER,

                createdIso TEXT
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    // Convert list<Int> → JSON
    private fun listToJson(list: List<Int>?): String? {
        return list?.let { JSONArray(it).toString() }
    }

    // Convert JSON → list<Int>
    private fun jsonToList(str: String?): List<Int>? {
        if (str.isNullOrEmpty()) return null
        val arr = JSONArray(str)
        val list = mutableListOf<Int>()
        for (i in 0 until arr.length()) list.add(arr.getInt(i))
        return list
    }

    fun insertHabit(h: Habit): Long {
        val db = writableDatabase
        val cv = ContentValues()

        cv.put("title", h.title)
        cv.put("description", h.description)
        cv.put("isActive", if (h.isActive) 1 else 0)

        cv.put("isFlexible", if (h.isFlexible) 1 else 0)
        cv.put("durationMinutes", h.durationMinutes)
        cv.put("earliestStartIso", h.earliestStartIso)
        cv.put("fixedStartIso", h.fixedStartIso)
        cv.put("preferredTimeBucket", h.preferredTimeBucket)
        cv.put("assignedStartIso", h.assignedStartIso)
        cv.put("assignedEndIso", h.assignedEndIso)

        cv.put("frequency", h.frequency.name)
        cv.put("customWeekdays", listToJson(h.customWeekdays))

        cv.put("lastCompletedIso", h.lastCompletedIso)
        cv.put("streak", h.streak)
        cv.put("createdIso", h.createdIso)

        return db.insert("habits", null, cv)
    }

    fun getAllHabits(): List<Habit> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM habits", null)
        val list = mutableListOf<Habit>()

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Habit(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1,

                        isFlexible = cursor.getInt(cursor.getColumnIndexOrThrow("isFlexible")) == 1,
                        durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("durationMinutes")),
                        earliestStartIso = cursor.getString(cursor.getColumnIndexOrThrow("earliestStartIso")),
                        fixedStartIso = cursor.getString(cursor.getColumnIndexOrThrow("fixedStartIso")),
                        preferredTimeBucket = cursor.getString(cursor.getColumnIndexOrThrow("preferredTimeBucket")),
                        assignedStartIso = cursor.getString(cursor.getColumnIndexOrThrow("assignedStartIso")),
                        assignedEndIso = cursor.getString(cursor.getColumnIndexOrThrow("assignedEndIso")),

                        frequency = HabitFrequency.valueOf(
                            cursor.getString(cursor.getColumnIndexOrThrow("frequency"))
                        ),
                        customWeekdays = jsonToList(cursor.getString(cursor.getColumnIndexOrThrow("customWeekdays"))),

                        lastCompletedIso = cursor.getString(cursor.getColumnIndexOrThrow("lastCompletedIso")),
                        streak = cursor.getInt(cursor.getColumnIndexOrThrow("streak")),
                        createdIso = cursor.getString(cursor.getColumnIndexOrThrow("createdIso"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateHabit(h: Habit) {
        val db = writableDatabase
        val cv = ContentValues()

        cv.put("title", h.title)
        cv.put("description", h.description)
        cv.put("isActive", if (h.isActive) 1 else 0)

        cv.put("isFlexible", if (h.isFlexible) 1 else 0)
        cv.put("durationMinutes", h.durationMinutes)
        cv.put("earliestStartIso", h.earliestStartIso)
        cv.put("fixedStartIso", h.fixedStartIso)
        cv.put("preferredTimeBucket", h.preferredTimeBucket)
        cv.put("assignedStartIso", h.assignedStartIso)
        cv.put("assignedEndIso", h.assignedEndIso)

        cv.put("frequency", h.frequency.name)
        cv.put("customWeekdays", listToJson(h.customWeekdays))

        cv.put("lastCompletedIso", h.lastCompletedIso)
        cv.put("streak", h.streak)
        cv.put("createdIso", h.createdIso)

        db.update("habits", cv, "id=?", arrayOf(h.id.toString()))
    }

    fun deleteHabit(id: Int) {
        writableDatabase.delete("habits", "id=?", arrayOf(id.toString()))
    }
}
