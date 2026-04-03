package com.example.personaltaskapp.dao

import androidx.room.*
import com.example.personaltaskapp.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, id DESC")
    fun getActiveTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    // Helper queries useful for scheduler
    @Query("SELECT * FROM tasks WHERE isFlexible = 1 AND fixedStartIso IS NULL")
    fun getFlexibleUnscheduledTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE fixedStartIso IS NOT NULL")
    fun getScheduledTasks(): Flow<List<Task>>
}
