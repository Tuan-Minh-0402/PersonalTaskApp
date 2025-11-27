package com.example.personaltaskapp.repository

import com.example.personaltaskapp.data.HabitDao
import com.example.personaltaskapp.model.Habit

class HabitRepository(private val dao: HabitDao) {

    val habits = dao.getAllHabits()

    suspend fun add(habit: Habit) = dao.insert(habit)
    suspend fun update(habit: Habit) = dao.update(habit)
    suspend fun delete(habit: Habit) = dao.delete(habit)
}
