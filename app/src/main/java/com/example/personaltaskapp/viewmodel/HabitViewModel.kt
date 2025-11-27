package com.example.personaltaskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(
    private val repo: HabitRepository
) : ViewModel() {

    val habits = repo.habits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addHabit(habit: Habit) {
        viewModelScope.launch { repo.addHabit(habit) }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch { repo.updateHabit(habit) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repo.deleteHabit(habit) }
    }

    fun deleteHabitById(id: Int) {
        viewModelScope.launch { repo.deleteHabitById(id) }
    }
}
