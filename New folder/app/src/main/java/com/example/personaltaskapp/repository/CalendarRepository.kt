package com.example.personaltaskapp.repository

import com.example.personaltaskapp.data.CalendarEventDao
import com.example.personaltaskapp.model.CalendarEvent

class CalendarRepository(private val dao: CalendarEventDao) {

    val events = dao.getAllEvents()

    suspend fun add(event: CalendarEvent) = dao.insert(event)
    suspend fun update(event: CalendarEvent) = dao.update(event)
    suspend fun delete(event: CalendarEvent) = dao.delete(event)
}
