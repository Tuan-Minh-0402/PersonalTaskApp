package com.example.personaltaskapp.scheduler

import java.time.LocalDate
import java.time.LocalDateTime

data class SmartSchedulerInput(
    val selectedDate: LocalDate,
    val tasks: List<SmartSchedulerTask>,
    val habitBlocks: List<SmartSchedulerBusyBlock>,
    val eventBlocks: List<SmartSchedulerBusyBlock>
)

data class SmartSchedulerTask(
    val id: String,
    val durationMinutes: Int,
    val priority: Int,
    val deadline: LocalDate?,
    val isCompleted: Boolean
)

data class SmartSchedulerBusyBlock(
    val start: LocalDateTime,
    val end: LocalDateTime
)

data class TimeBlock(
    val start: LocalDateTime,
    val end: LocalDateTime
)

data class ScheduledTaskBlock(
    val taskId: String,
    val start: LocalDateTime,
    val end: LocalDateTime
)

data class UnscheduledTask(
    val taskId: String,
    val reason: UnscheduledReason
)

enum class UnscheduledReason {
    INVALID_DURATION,
    NO_SLOT
}

data class SmartSchedulerResult(
    val scheduled: List<ScheduledTaskBlock>,
    val unscheduled: List<UnscheduledTask>
)