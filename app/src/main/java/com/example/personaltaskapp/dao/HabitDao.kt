package com.example.personaltaskapp.dao

import androidx.room.*
import com.example.personaltaskapp.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit)

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    // Convenience: delete by id (optional)
    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Habit?

    // example: store daysOfWeek as "MON,TUE" etc. Query by LIKE; for robust you can normalize days into separate table later
    @Query("SELECT * FROM habits WHERE daysOfWeek LIKE '%' || :day || '%'")
    fun getHabitsForDay(day: String): Flow<List<Habit>>

    // update streak and lastCompletedIso
    @Query("UPDATE habits SET streak = :streak, lastCompletedIso = :iso WHERE id = :id")
    suspend fun updateStreakAndLast(id: Int, streak: Int, iso: String)
}
