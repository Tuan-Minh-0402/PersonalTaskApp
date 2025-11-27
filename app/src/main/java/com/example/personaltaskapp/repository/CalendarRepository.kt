package com.example.personaltaskapp.repository

import com.example.personaltaskapp.dao.CalendarEventDao
import com.example.personaltaskapp.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

class CalendarRepository(private val dao: CalendarEventDao) {
    val events: Flow<List<CalendarEvent>> = dao.getAllEvents()

    fun eventsOnDate(iso: String): Flow<List<CalendarEvent>> = dao.getEventsOnDate(iso)

    suspend fun addEvent(e: CalendarEvent) = dao.insert(e)
    suspend fun updateEvent(e: CalendarEvent) = dao.update(e)
    suspend fun deleteEvent(e: CalendarEvent) = dao.delete(e)
}
