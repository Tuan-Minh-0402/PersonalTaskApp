package com.example.personaltaskapp.data

import android.content.Context
import androidx.room.Room

object DatabaseModule {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "personal_task_app_db"
            ).build().also { INSTANCE = it }
        }
    }
}
