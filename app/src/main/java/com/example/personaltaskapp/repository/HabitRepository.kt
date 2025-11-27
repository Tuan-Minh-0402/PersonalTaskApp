package com.example.personaltaskapp.repository

import com.example.personaltaskapp.dao.HabitDao
import com.example.personaltaskapp.model.Habit
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val dao: HabitDao) {

    val habits: Flow<List<Habit>> = dao.getAllHabits()

    suspend fun addHabit(habit: Habit) = dao.insert(habit)

    suspend fun updateHabit(habit: Habit) = dao.update(habit)

    suspend fun deleteHabit(habit: Habit) = dao.delete(habit)

    // new helpers
    suspend fun getHabitById(id: Int): Habit? = dao.getById(id)

    // day example: "MON", "TUE", etc
    fun habitsForDay(day: String): Flow<List<Habit>> = dao.getHabitsForDay(day)

    suspend fun updateStreakAndLast(id: Int, streak: Int, iso: String) =
        dao.updateStreakAndLast(id, streak, iso)
}
