package com.example.personaltaskapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.data.DatabaseModule
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository
    val habits: StateFlow<List<Habit>>

    init {
        val db = DatabaseModule.getDatabase(application)
        repository = HabitRepository(db.habitDao())

        habits = repository.habits.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch { repository.add(habit) }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch { repository.update(habit) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.delete(habit) }
    }
}
