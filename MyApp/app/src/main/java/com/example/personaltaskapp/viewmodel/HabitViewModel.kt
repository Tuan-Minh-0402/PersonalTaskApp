package com.example.personaltaskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HabitViewModel(private val repo: HabitRepository) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits

    init {
        loadHabits()
    }

    fun loadHabits() {
        viewModelScope.launch {
            _habits.value = repo.getHabits()
        }
    }

    fun addHabit(habit: Habit) {
        repo.addHabit(habit)
        loadHabits()
    }

    fun updateHabit(habit: Habit) {
        repo.updateHabit(habit)
        loadHabits()
    }

    fun deleteHabit(id: Int) {
        repo.deleteHabit(id)
        loadHabits()
    }
}
