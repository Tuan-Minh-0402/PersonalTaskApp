package com.example.personaltaskapp.scheduler

import java.time.LocalDate

object SmartSchedulerTestDataProvider {

    private val selectedDate: LocalDate = LocalDate.of(2026, 1, 15)

    data class SchedulerTestCase(
        val name: String,
        val input: SmartSchedulerInput
    )

    fun allCases(): List<SchedulerTestCase> {
        return listOf(
            basicSchedulingCase(),
            sleepWorkWindowCase(),
            eventBusyBlockCase(),
            mergeBusyBlocksCase(),
            invalidDurationCase(),
            noSlotCase(),
            overduePriorityCase(),
            sameDayDeadlineCase(),
            durationFitCase(),
            durationFitTightSlotCase(),
            habitBusyBlockCase()
        )
    }

    fun basicSchedulingCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "basic_scheduling",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(
                    task(id = "T1", duration = 60, priority = 1, deadline = selectedDate.plusDays(3)),
                    task(id = "T2", duration = 30, priority = 1, deadline = selectedDate.plusDays(3))
                ),
                habitBlocks = emptyList(),
                eventBlocks = emptyList()
            )
        )
    }

    fun sleepWorkWindowCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "sleep_work_window",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(
                    task(id = "LONG", duration = 900, priority = 1, deadline = selectedDate.plusDays(1))
                ),
                habitBlocks = emptyList(),
                eventBlocks = emptyList()
            )
        )
    }

    fun eventBusyBlockCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "event_busy_block",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(task(id = "E1", duration = 60, priority = 1, deadline = selectedDate.plusDays(2))),
                habitBlocks = emptyList(),
                eventBlocks = listOf(busy(9, 0, 10, 0))
            )
        )
    }

    fun mergeBusyBlocksCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "merge_busy_blocks",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(task(id = "M1", duration = 120, priority = 1, deadline = selectedDate.plusDays(2))),
                habitBlocks = emptyList(),
                eventBlocks = listOf(
                    busy(9, 0, 10, 0),
                    busy(9, 30, 11, 0)
                )
            )
        )
    }

    fun invalidDurationCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "invalid_duration",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(task(id = "BAD", duration = 0, priority = 1, deadline = selectedDate.plusDays(1))),
                habitBlocks = emptyList(),
                eventBlocks = emptyList()
            )
        )
    }

    fun noSlotCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "no_slot",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(task(id = "N1", duration = 30, priority = 1, deadline = selectedDate.plusDays(1))),
                habitBlocks = emptyList(),
                eventBlocks = listOf(busy(8, 0, 21, 45))
            )
        )
    }

    fun overduePriorityCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "overdue_priority",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(
                    task(id = "OVERDUE", duration = 60, priority = 1, deadline = selectedDate.minusDays(3)),
                    task(id = "FUTURE", duration = 60, priority = 3, deadline = selectedDate.plusDays(10))
                ),
                habitBlocks = emptyList(),
                eventBlocks = emptyList()
            )
        )
    }

    fun sameDayDeadlineCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "same_day_deadline",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(
                    task(id = "TODAY", duration = 60, priority = 1, deadline = selectedDate),
                    task(id = "TOMORROW", duration = 60, priority = 1, deadline = selectedDate.plusDays(1))
                ),
                habitBlocks = emptyList(),
                eventBlocks = emptyList()
            )
        )
    }

    fun durationFitCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "duration_fit",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(
                    task(id = "TIGHT", duration = 80, priority = 1, deadline = selectedDate.plusDays(2)),
                    task(id = "LOOSE", duration = 20, priority = 1, deadline = selectedDate.plusDays(2))
                ),
                habitBlocks = emptyList(),
                eventBlocks = listOf(busy(9, 30, 10, 0))
            )
        )
    }

    fun habitBusyBlockCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "habit_busy_block",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(task(id = "H1", duration = 60, priority = 1, deadline = selectedDate.plusDays(3))),
                habitBlocks = listOf(busy(8, 0, 9, 0)),
                eventBlocks = emptyList()
            )
        )
    }

    fun durationFitTightSlotCase(): SchedulerTestCase {
        return SchedulerTestCase(
            name = "duration_fit_tight_slot",
            input = SmartSchedulerInput(
                selectedDate = selectedDate,
                tasks = listOf(
                    task(id = "A30", duration = 30, priority = 1, deadline = selectedDate.plusDays(2)),
                    task(id = "B40", duration = 40, priority = 1, deadline = selectedDate.plusDays(2)),
                    task(id = "C90", duration = 90, priority = 1, deadline = selectedDate.plusDays(2))
                ),
                habitBlocks = emptyList(),
                eventBlocks = listOf(
                    busy(8, 45, 10, 0),
                    busy(12, 0, 22, 0)
                )
            )
        )
    }

    private fun task(
        id: String,
        duration: Int,
        priority: Int,
        deadline: LocalDate?,
        isCompleted: Boolean = false
    ): SmartSchedulerTask {
        return SmartSchedulerTask(
            id = id,
            durationMinutes = duration,
            priority = priority,
            deadline = deadline,
            isCompleted = isCompleted
        )
    }

    private fun busy(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): SmartSchedulerBusyBlock {
        return SmartSchedulerBusyBlock(
            start = selectedDate.atTime(startHour, startMinute),
            end = selectedDate.atTime(endHour, endMinute)
        )
    }
}