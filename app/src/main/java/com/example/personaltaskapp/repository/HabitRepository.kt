package com.example.personaltaskapp.repository

import com.example.personaltaskapp.dao.HabitDao
import com.example.personaltaskapp.model.Habit
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: HabitDao) {

    // Flow of all habits (single source of truth)
    private fun getAllHabits(): Flow<List<Habit>> = dao.getAllHabits()

    // Convenience property if you prefer a property name
    val habits: Flow<List<Habit>> get() = getAllHabits()

    suspend fun addHabit(habit: Habit) = dao.insert(habit)

    suspend fun updateHabit(habit: Habit) = dao.update(habit)

    suspend fun deleteHabit(habit: Habit) = dao.delete(habit)

    suspend fun getHabitById(id: Int): Habit? = dao.getById(id)
}
