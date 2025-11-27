package com.example.personaltaskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.model.CalendarEvent
import com.example.personaltaskapp.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CalendarViewModel : ViewModel() {

    private val repository = CalendarRepository()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> get() = _events

    init {
        loadEvents()
    }

    private fun loadEvents() {
        _events.value = repository.getAllEvents()
    }

    fun addEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.addEvent(event)
            loadEvents()
        }
    }

    fun updateEvent(eventId: String, updatedEvent: CalendarEvent) {
        viewModelScope.launch {
            repository.updateEvent(eventId, updatedEvent)
            loadEvents()
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            loadEvents()
        }
    }

    fun clearAllEvents() {
        viewModelScope.launch {
            repository.clearAllEvents()
            loadEvents()
        }
    }
}
