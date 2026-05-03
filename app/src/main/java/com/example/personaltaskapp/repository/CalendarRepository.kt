package com.example.personaltaskapp.repository

import com.example.personaltaskapp.dao.CalendarEventDao
import com.example.personaltaskapp.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

class CalendarRepository(private val dao: CalendarEventDao) {

    fun getAllEvents(): Flow<List<CalendarEvent>> = dao.getAllEvents()

    suspend fun insertEvent(event: CalendarEvent) = dao.insert(event)
    suspend fun updateEvent(event: CalendarEvent) = dao.update(event)

    suspend fun deleteEvent(event: CalendarEvent) = dao.delete(event)
}
