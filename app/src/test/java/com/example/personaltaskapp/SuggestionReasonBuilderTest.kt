package com.example.personaltaskapp.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestionReasonBuilderTest {

    @Test
    fun overdueAndHighPriority_returnsTop2Reasons() {
        val reason = buildSuggestionReason(
            overdueLevel = 1,
            urgencyLevel = 0,
            priority = 3,
            fitLevel = 0,
            baseScore = 130
        )

        assertEquals("Overdue task • High priority", reason)
    }

    @Test
    fun nearDeadlineAndGoodFit_returnsTop2Reasons() {
        val reason = buildSuggestionReason(
            overdueLevel = 0,
            urgencyLevel = 2,
            priority = 1,
            fitLevel = 2,
            baseScore = 80
        )

        assertEquals("Deadline is near • Fits available time slot", reason)
    }

    @Test
    fun moreThanTwoSignals_limitsToTwoReasons() {
        val reason = buildSuggestionReason(
            overdueLevel = 2,
            urgencyLevel = 3,
            priority = 5,
            fitLevel = 2,
            baseScore = 390
        )

        assertEquals("Overdue task • Deadline is near", reason)
    }

    @Test
    fun noReasonSignals_highBaseScore_returnsImportantTask() {
        val reason = buildSuggestionReason(
            overdueLevel = 0,
            urgencyLevel = 0,
            priority = 2,
            fitLevel = 1,
            baseScore = 40
        )

        assertEquals("Important task", reason)
    }

    @Test
    fun noReasonSignals_lowBaseScore_returnsSchedulerFallback() {
        val reason = buildSuggestionReason(
            overdueLevel = 0,
            urgencyLevel = 0,
            priority = 1,
            fitLevel = 0,
            baseScore = 10
        )

        assertEquals("Suggested by scheduler", reason)
    }
}