package com.example.personaltaskapp.repository


import com.example.personaltaskapp.data.HabitDatabaseHelper
import com.example.personaltaskapp.model.Habit

class HabitRepository(private val db: HabitDatabaseHelper) {

    fun getHabits(): List<Habit> = db.getAllHabits()

    fun addHabit(habit: Habit) = db.insertHabit(habit)

    fun updateHabit(habit: Habit) = db.updateHabit(habit)

    fun deleteHabit(id: Int) = db.deleteHabit(id)
}
