package com.example.personaltaskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personaltaskapp.repository.HabitRepository

class HabitViewModelFactory(
    private val repo: HabitRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            return HabitViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
