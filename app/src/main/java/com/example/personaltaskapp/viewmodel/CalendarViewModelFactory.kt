package com.example.personaltaskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personaltaskapp.repository.CalendarRepository

class CalendarViewModelFactory(
    private val repo: CalendarRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
