package com.example.personaltaskapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.data.DatabaseModule
import com.example.personaltaskapp.model.CalendarEvent
import com.example.personaltaskapp.repository.CalendarRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository
    val events: StateFlow<List<CalendarEvent>>

    init {
        val db = DatabaseModule.getDatabase(application)
        repository = CalendarRepository(db.calendarEventDao())

        events = repository.events.stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun addEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.add(event)
        }
    }

    fun updateEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.update(event)
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.delete(event)
        }
    }
}
