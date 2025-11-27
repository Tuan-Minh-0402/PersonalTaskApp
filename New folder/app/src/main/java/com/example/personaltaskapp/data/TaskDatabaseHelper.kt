package com.example.personaltaskapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_TASKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                durationMinutes INTEGER,
                earliestStartIso TEXT,
                dueDateIso TEXT,
                fixedStartIso TEXT,
                priority INTEGER NOT NULL DEFAULT 2,
                isFlexible INTEGER NOT NULL DEFAULT 0,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                pomodoroCount INTEGER DEFAULT 4,
                completedPomodoros INTEGER DEFAULT 0,
                isPomodoroRunning INTEGER DEFAULT 0
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migrate step-by-step for forward compatibility
        for (version in oldVersion until newVersion) {
            when (version) {
                1 -> upgradeFromV1ToV2(db)
                2 -> upgradeFromV2ToV3(db)
            }
        }
    }

    private fun upgradeFromV1ToV2(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN description TEXT;")
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN durationMinutes INTEGER;")
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN earliestStartIso TEXT;")
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN dueDateIso TEXT;")
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN fixedStartIso TEXT;")
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN priority INTEGER NOT NULL DEFAULT 2;")
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN isFlexible INTEGER NOT NULL DEFAULT 0;")
    }

    private fun upgradeFromV2ToV3(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN pomodoroCount INTEGER DEFAULT 4;")
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN completedPomodoros INTEGER DEFAULT 0;")
        db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN isPomodoroRunning INTEGER DEFAULT 0;")
    }

    companion object {
        const val DATABASE_NAME = "personal_tasks.db"
        const val DATABASE_VERSION = 3

        const val TABLE_TASKS = "tasks"
    }
}
