package com.example.personaltaskapp.repository

import com.example.personaltaskapp.model.CalendarEvent

class CalendarRepository {

    private val localEvents = mutableListOf<CalendarEvent>()

    fun getAllEvents(): List<CalendarEvent> = localEvents

    fun addEvent(event: CalendarEvent): CalendarEvent {
        localEvents.add(event)
        return event
    }

    fun updateEvent(eventId: String, updatedEvent: CalendarEvent): Boolean {
        val index = localEvents.indexOfFirst { it.eventId == eventId }
        return if (index != -1) {
            localEvents[index] = updatedEvent
            true
        } else false
    }

    fun deleteEvent(eventId: String): Boolean {
        return localEvents.removeIf { it.eventId == eventId }
    }

    fun clearAllEvents() {
        localEvents.clear()
    }
}
