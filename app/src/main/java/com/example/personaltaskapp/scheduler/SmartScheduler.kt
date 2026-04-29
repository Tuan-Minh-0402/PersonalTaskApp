package com.example.personaltaskapp.scheduler

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Legacy suggestion API (used by current UI) + MVP day scheduler API.
 */
object SmartScheduler {

    /**
     * MVP heuristic scheduler (single-day, deterministic).
     */
    fun scheduleDay(input: SmartSchedulerInput): SmartSchedulerResult {
        val referenceDate = input.selectedDate
        val dayStart = referenceDate.atTime(8, 0)
        val dayEndExclusive = referenceDate.atTime(22, 0)

        val unscheduled = mutableListOf<UnscheduledTask>()
        val validTasks = mutableListOf<SmartSchedulerTask>()

        input.tasks
            .asSequence()
            .filter { !it.isCompleted }
            .forEach { task ->
                if (task.durationMinutes <= 0) {
                    unscheduled += UnscheduledTask(task.id, UnscheduledReason.INVALID_DURATION)
                } else {
                    validTasks += task
                }
            }

        val mergedBusyBlocks = mergeBusyBlocks(
            blocks = input.habitBlocks + input.eventBlocks,
            dayStart = dayStart,
            dayEndExclusive = dayEndExclusive
        )

        val freeBlocks = buildFreeBlocks(
            mergedBusyBlocks = mergedBusyBlocks,
            dayStart = dayStart,
            dayEndExclusive = dayEndExclusive
        ).toMutableList()

        val scheduled = mutableListOf<ScheduledTaskBlock>()
        val remainingTasks = validTasks.toMutableList()

        while (remainingTasks.isNotEmpty()) {
            val sortedTasks = remainingTasks
                .map { task ->
                    ScoredTask(
                        task = task,
                        score = scoreTask(task, referenceDate),
                        fitLevel = calcFitLevel(task.durationMinutes, freeBlocks)
                    )
                }
                .sortedWith(
                    compareByDescending<ScoredTask> { it.score }
                        .thenBy { it.task.deadline == null }
                        .thenBy { deadlineSortKey(it.task.deadline) }
                        .thenByDescending { it.task.priority }
                        .thenByDescending { it.fitLevel }
                        .thenBy { it.task.durationMinutes }
                        .thenBy { it.task.id }
                )

            val chosenTask = sortedTasks.first().task
            val freeIndex = freeBlocks.indexOfFirst { freeBlock ->
                minutesBetween(freeBlock.start, freeBlock.end) >= chosenTask.durationMinutes
            }

            if (freeIndex == -1) {
                unscheduled += UnscheduledTask(chosenTask.id, UnscheduledReason.NO_SLOT)
                remainingTasks.remove(chosenTask)
                continue
            }

            val chosenBlock = freeBlocks[freeIndex]
            val scheduledStart = chosenBlock.start
            val scheduledEnd = scheduledStart.plusMinutes(chosenTask.durationMinutes.toLong())

            scheduled += ScheduledTaskBlock(
                taskId = chosenTask.id,
                start = scheduledStart,
                end = scheduledEnd
            )

            if (scheduledEnd == chosenBlock.end) {
                freeBlocks.removeAt(freeIndex)
            } else {
                freeBlocks[freeIndex] = TimeBlock(start = scheduledEnd, end = chosenBlock.end)
            }
            remainingTasks.remove(chosenTask)
        }

        return SmartSchedulerResult(
            scheduled = scheduled,
            unscheduled = unscheduled
        )
    }

    fun mergeBusyBlocks(
        blocks: List<SmartSchedulerBusyBlock>,
        dayStart: LocalDateTime,
        dayEndExclusive: LocalDateTime
    ): List<TimeBlock> {
        if (blocks.isEmpty()) return emptyList()

        val clipped = blocks
            .mapNotNull { block ->
                val start = if (block.start.isBefore(dayStart)) dayStart else block.start
                val end = if (block.end.isAfter(dayEndExclusive)) dayEndExclusive else block.end
                if (!start.isBefore(end)) null else TimeBlock(start = start, end = end)
            }
            .sortedBy { it.start }

        if (clipped.isEmpty()) return emptyList()

        val merged = mutableListOf<TimeBlock>()
        var current = clipped.first()

        for (index in 1 until clipped.size) {
            val next = clipped[index]
            if (!next.start.isAfter(current.end)) {
                val mergedEnd = if (next.end.isAfter(current.end)) next.end else current.end
                current = TimeBlock(start = current.start, end = mergedEnd)
            } else {
                merged += current
                current = next
            }
        }

        merged += current
        return merged
    }

    fun buildFreeBlocks(
        mergedBusyBlocks: List<TimeBlock>,
        dayStart: LocalDateTime,
        dayEndExclusive: LocalDateTime
    ): List<TimeBlock> {
        if (!dayStart.isBefore(dayEndExclusive)) return emptyList()
        if (mergedBusyBlocks.isEmpty()) return listOf(TimeBlock(dayStart, dayEndExclusive))

        val free = mutableListOf<TimeBlock>()
        var cursor = dayStart

        mergedBusyBlocks.forEach { busy ->
            if (cursor.isBefore(busy.start)) {
                free += TimeBlock(start = cursor, end = busy.start)
            }
            if (cursor.isBefore(busy.end)) {
                cursor = busy.end
            }
        }

        if (cursor.isBefore(dayEndExclusive)) {
            free += TimeBlock(start = cursor, end = dayEndExclusive)
        }

        return free
    }

    fun calcOverdueLevel(deadline: LocalDate?, referenceDate: LocalDate): Int {
        if (deadline == null) return 0

        val daysLate = ChronoUnit.DAYS.between(deadline, referenceDate).toInt()
        return when {
            daysLate <= 0 -> 0
            daysLate <= 2 -> 1
            daysLate <= 7 -> 2
            else -> 3
        }
    }

    fun calcUrgencyLevel(deadline: LocalDate?, referenceDate: LocalDate): Int {
        if (deadline == null) return 0

        val daysToDeadline = ChronoUnit.DAYS.between(referenceDate, deadline).toInt()
        return when {
            daysToDeadline <= 0 -> 3
            daysToDeadline <= 2 -> 2
            daysToDeadline <= 7 -> 1
            else -> 0
        }
    }

    fun scoreTask(task: SmartSchedulerTask, referenceDate: LocalDate): Int {
        val overdueLevel = calcOverdueLevel(task.deadline, referenceDate)
        val urgencyLevel = calcUrgencyLevel(task.deadline, referenceDate)
        return 100 * overdueLevel + 30 * urgencyLevel + 10 * task.priority
    }

    fun calcFitLevel(durationMinutes: Int, freeBlocks: List<TimeBlock>): Int {
        val smallestFittingBlock = freeBlocks
            .map { block -> minutesBetween(block.start, block.end) }
            .filter { blockMinutes -> blockMinutes >= durationMinutes }
            .minOrNull() ?: return 0

        val leftover = smallestFittingBlock - durationMinutes
        return when {
            leftover <= 15 -> 2
            leftover <= 60 -> 1
            else -> 0
        }
    }

    private fun deadlineSortKey(deadline: LocalDate?): LocalDate {
        return deadline ?: LocalDate.of(9999, 12, 31)
    }

    private fun minutesBetween(start: LocalDateTime, end: LocalDateTime): Int {
        return Duration.between(start, end).toMinutes().toInt()
    }

    private data class ScoredTask(
        val task: SmartSchedulerTask,
        val score: Int,
        val fitLevel: Int
    )

}

data class SmartSuggestion(
    val taskId: Int,
    val title: String,
    val suggestedDateIso: String,
    val reason: String,
    val confidence: Float,
    val suggestedStartIso: String? = null,
    val suggestedEndIso: String? = null
)