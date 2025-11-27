package com.example.personaltaskapp.dao

import androidx.room.*
import com.example.personaltaskapp.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Task?

    // incomplete tasks
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, id DESC")
    fun getIncompleteTasks(): Flow<List<Task>>

    // tasks with due date between (inclusive) - dueDateIso stored as 'YYYY-MM-DD' (or ISO)
    @Query("""
      SELECT * FROM tasks
      WHERE dueDateIso IS NOT NULL
        AND date(dueDateIso) BETWEEN date(:fromIso) AND date(:toIso)
      ORDER BY date(dueDateIso) ASC
    """)
    fun getTasksBetweenDates(fromIso: String, toIso: String): Flow<List<Task>>

    // tasks that are flexible (for scheduler)
    @Query("SELECT * FROM tasks WHERE isFlexible = 1 AND isCompleted = 0")
    fun getFlexibleTasks(): Flow<List<Task>>

}
