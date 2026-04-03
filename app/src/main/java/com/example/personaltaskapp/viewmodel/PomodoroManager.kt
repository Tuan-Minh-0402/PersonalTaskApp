package com.example.personaltaskapp.viewmodel

import com.example.personaltaskapp.model.PomodoroState
import com.example.personaltaskapp.model.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PomodoroManager(
    private val updateTaskPomodoro: (Task) -> Unit       // <-- callback from ViewModel
) {

    private var job: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _secondsLeft = MutableStateFlow(0)
    val secondsLeft: StateFlow<Int> = _secondsLeft

    private val _state = MutableStateFlow(PomodoroState.IDLE)
    val state: StateFlow<PomodoroState> = _state

    private var currentTask: Task? = null


    fun start(task: Task) {
        if (_isRunning.value) return

        currentTask = task
        _state.value = PomodoroState.WORK
        _secondsLeft.value = 25 * 60
        _isRunning.value = true

        job = kotlinx.coroutines.GlobalScope.launch {
            while (_secondsLeft.value > 0 && _isRunning.value) {
                delay(1000)
                _secondsLeft.value -= 1
            }

            if (_isRunning.value) finish()
        }
    }

    fun cancel() {
        job?.cancel()
        _isRunning.value = false
        _state.value = PomodoroState.IDLE
    }

    private fun finish() {
        val t = currentTask ?: return
        val updated = t.copy(completedPomodoros = t.completedPomodoros + 1)
        updateTaskPomodoro(updated)     // <-- ViewModel updates DB

        _isRunning.value = false
        _state.value = PomodoroState.IDLE
    }
}
