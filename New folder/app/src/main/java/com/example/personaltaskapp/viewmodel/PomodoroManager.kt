package com.example.personaltaskapp.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.example.personaltaskapp.model.PomodoroState
import com.example.personaltaskapp.model.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PomodoroManager(
    private val viewModel: TaskViewModel   // so it can update tasks
) {

    var isRunning by mutableStateOf(false)
        private set

    var state by mutableStateOf(PomodoroState.IDLE)
        private set

    var secondsLeft by mutableStateOf(0)
        private set

    private var job: Job? = null
    private var currentTask: Task? = null
    private var cyclesCompleted = 0

    // ─────────────────────────────────────────────
    // START POMODORO
    // ─────────────────────────────────────────────
    fun start(task: Task) {
        if (isRunning) return

        currentTask = task
        state = PomodoroState.WORK
        secondsLeft = 25 * 60
        isRunning = true

        runCycle()
    }

    // ─────────────────────────────────────────────
    // CANCEL POMODORO
    // ─────────────────────────────────────────────
    fun cancel() {
        job?.cancel()
        isRunning = false
        state = PomodoroState.IDLE
        secondsLeft = 0
        cyclesCompleted = 0
        currentTask = null
    }

    // ─────────────────────────────────────────────
    // INTERNAL TIMER ENGINE
    // ─────────────────────────────────────────────
    private fun runCycle() {
        job?.cancel()

        job = viewModel.viewModelScope.launch {
            while (secondsLeft > 0 && isRunning) {
                delay(1000)
                secondsLeft--
            }

            if (!isRunning) return@launch

            when (state) {

                PomodoroState.WORK -> completeWorkSession()

                PomodoroState.SHORT_BREAK -> {
                    state = PomodoroState.WORK
                    secondsLeft = 25 * 60
                    runCycle()
                }

                PomodoroState.LONG_BREAK -> {
                    state = PomodoroState.WORK
                    secondsLeft = 25 * 60
                    runCycle()
                }

                else -> {}
            }
        }
    }

    // ─────────────────────────────────────────────
    // ON WORK SESSION COMPLETE
    // ─────────────────────────────────────────────
    private fun completeWorkSession() {
        val task = currentTask ?: return

        // update pomodoro counter
        viewModel.updateTask(
            task.copy(completedPomodoros = task.completedPomodoros + 1)
        )

        cyclesCompleted++

        // decide break type
        state = if (cyclesCompleted % 4 == 0)
            PomodoroState.LONG_BREAK
        else
            PomodoroState.SHORT_BREAK

        secondsLeft = if (state == PomodoroState.LONG_BREAK)
            15 * 60
        else
            5 * 60

        runCycle()
    }
}
