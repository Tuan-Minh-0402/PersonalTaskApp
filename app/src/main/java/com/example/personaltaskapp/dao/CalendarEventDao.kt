package com.example.personaltaskapp.dao

import androidx.room.*
import com.example.personaltaskapp.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY dateIso ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE dateIso = :iso")
    fun getEventsOnDate(iso: String): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent)

    @Update
    suspend fun update(event: CalendarEvent)

    @Delete
    suspend fun delete(event: CalendarEvent)
}
