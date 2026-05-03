package com.example.personaltaskapp.scheduler

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class SmartSchedulerDebugTest {

    @Test
    fun runAllCases_printResults() {
        SmartSchedulerTestDataProvider.allCases().forEach { testCase ->
            runCase(testCase)
        }
    }

    @Test
    fun basicScheduling_shouldScheduleTasks() {
        val result = runCase(SmartSchedulerTestDataProvider.basicSchedulingCase())
        assertEquals(2, result.scheduled.size)
        assertTrue(result.unscheduled.isEmpty())
        assertTrue(result.scheduled.all { it.start.toLocalTime() >= LocalTime.of(8, 0) })
        assertTrue(result.scheduled.all { it.end.toLocalTime() <= LocalTime.of(22, 0) })
    }

    @Test
    fun sleepWorkWindow_longTaskShouldBeNoSlot() {
        val result = runCase(SmartSchedulerTestDataProvider.sleepWorkWindowCase())
        assertTrue(result.scheduled.isEmpty())
        assertEquals(1, result.unscheduled.size)
        assertEquals(UnscheduledReason.NO_SLOT, result.unscheduled.first().reason)
    }

    @Test
    fun eventBusyBlock_shouldAvoidBusyTime() {
        val result = runCase(SmartSchedulerTestDataProvider.eventBusyBlockCase())
        val scheduled = result.scheduled.firstOrNull { it.taskId == "E1" }
        assertNotNull(scheduled)
        assertEquals(LocalTime.of(8, 0), scheduled?.start?.toLocalTime())
    }

    @Test
    fun mergeBusyBlocks_shouldUseMergedFreeSpace() {
        val result = runCase(SmartSchedulerTestDataProvider.mergeBusyBlocksCase())
        val scheduled = result.scheduled.firstOrNull { it.taskId == "M1" }
        assertNotNull(scheduled)
        assertEquals(LocalTime.of(11, 0), scheduled?.start?.toLocalTime())
    }

    @Test
    fun invalidDuration_shouldBeMarkedInvalid() {
        val result = runCase(SmartSchedulerTestDataProvider.invalidDurationCase())
        assertTrue(result.scheduled.isEmpty())
        assertEquals(1, result.unscheduled.size)
        assertEquals(UnscheduledReason.INVALID_DURATION, result.unscheduled.first().reason)
    }

    @Test
    fun noSlot_shouldBeMarkedNoSlot() {
        val result = runCase(SmartSchedulerTestDataProvider.noSlotCase())
        assertTrue(result.scheduled.isEmpty())
        assertEquals(1, result.unscheduled.size)
        assertEquals(UnscheduledReason.NO_SLOT, result.unscheduled.first().reason)
    }

    @Test
    fun overduePriority_overdueTaskShouldBeScheduledFirst() {
        val result = runCase(SmartSchedulerTestDataProvider.overduePriorityCase())
        assertEquals("OVERDUE", result.scheduled.first().taskId)
    }

    @Test
    fun sameDayDeadline_todayDeadlineShouldBeScheduledFirst() {
        val result = runCase(SmartSchedulerTestDataProvider.sameDayDeadlineCase())
        assertEquals("TODAY", result.scheduled.first().taskId)
    }

    @Test
    fun durationFit_tightTaskShouldBeScheduledFirst() {
        val result = runCase(SmartSchedulerTestDataProvider.durationFitCase())
        assertEquals("TIGHT", result.scheduled.first().taskId)
    }

            @Test
    fun habitBusyBlock_shouldTreatHabitAsBusy() {
        val result = runCase(SmartSchedulerTestDataProvider.habitBusyBlockCase())
        val scheduled = result.scheduled.firstOrNull { it.taskId == "H1" }
        assertNotNull(scheduled)
        assertEquals(LocalTime.of(9, 0), scheduled?.start?.toLocalTime())
    }

    @Test
    fun durationFitTightSlot_shouldUse45MinSlotBefore120MinSlot() {
        val result = runCase(SmartSchedulerTestDataProvider.durationFitTightSlotCase())

        val firstScheduled = result.scheduled.firstOrNull()
        assertNotNull(firstScheduled)
        assertEquals(LocalTime.of(8, 0), firstScheduled?.start?.toLocalTime())
        assertTrue(firstScheduled?.taskId == "A30" || firstScheduled?.taskId == "B40")

        val taskC = result.scheduled.firstOrNull { it.taskId == "C90" }
        assertNotNull(taskC)
        assertEquals(LocalTime.of(10, 0), taskC?.start?.toLocalTime())
    }

    @Test
    fun lowPriorityNoDeadline_shouldBeIgnoredByEligibility() {
        val result = runCase(SmartSchedulerTestDataProvider.lowPriorityNoDeadlineCase())
        assertTrue(result.scheduled.isEmpty())
        assertTrue(result.unscheduled.isEmpty())
    }

    @Test
    fun highPriorityNoDeadline_shouldStillSchedule() {
        val result = runCase(SmartSchedulerTestDataProvider.highPriorityNoDeadlineCase())
        assertEquals(1, result.scheduled.size)
        assertEquals("HIGH_NO_DEADLINE", result.scheduled.first().taskId)
    }

    @Test
    fun overdueLowPriority_shouldStillSchedule() {
        val result = runCase(SmartSchedulerTestDataProvider.overdueLowPriorityCase())
        assertEquals(1, result.scheduled.size)
        assertEquals("OVERDUE_LOW", result.scheduled.first().taskId)
    }

    @Test
    fun nearDeadlineLow_shouldStillSchedule() {
        val result = runCase(SmartSchedulerTestDataProvider.nearDeadlineLowCase())
        assertEquals(1, result.scheduled.size)
        assertEquals("NEAR_LOW", result.scheduled.first().taskId)
    }

    @Test
    fun eligibleNoSlot_shouldBeMarkedNoSlot() {
        val result = runCase(SmartSchedulerTestDataProvider.eligibleNoSlotCase())
        assertTrue(result.scheduled.isEmpty())
        assertEquals(1, result.unscheduled.size)
        assertEquals("ELIGIBLE_NO_SLOT", result.unscheduled.first().taskId)
        assertEquals(UnscheduledReason.NO_SLOT, result.unscheduled.first().reason)
    }

    @Test
    fun completedTask_shouldBeIgnored() {
        val result = runCase(SmartSchedulerTestDataProvider.completedTaskIgnoredCase())
        assertTrue(result.scheduled.isEmpty())
        assertTrue(result.unscheduled.isEmpty())
    }

    private fun runCase(testCase: SmartSchedulerTestDataProvider.SchedulerTestCase): SmartSchedulerResult {
        val result = SmartScheduler.scheduleDay(testCase.input)
        assertNoDuplicateOrConflict(result)
        printResult(testCase.name, result)
        return result
    }

    private fun assertNoDuplicateOrConflict(result: SmartSchedulerResult) {
        val scheduledIds = result.scheduled.map { it.taskId }
        val unscheduledIds = result.unscheduled.map { it.taskId }

        assertEquals(scheduledIds.size, scheduledIds.toSet().size)
        assertEquals(unscheduledIds.size, unscheduledIds.toSet().size)
        assertTrue(scheduledIds.intersect(unscheduledIds.toSet()).isEmpty())
    }

    private fun printResult(name: String, result: SmartSchedulerResult) {
        println("===== CASE: $name =====")
        if (result.scheduled.isEmpty()) {
            println("scheduled: []")
        } else {
            result.scheduled.forEach { block ->
                println("scheduled: taskId=${block.taskId}, start=${block.start}, end=${block.end}")
            }
        }

        if (result.unscheduled.isEmpty()) {
            println("unscheduled: []")
        } else {
            result.unscheduled.forEach { uns ->
                println("unscheduled: taskId=${uns.taskId}, reason=${uns.reason}")
            }
        }
    }
}