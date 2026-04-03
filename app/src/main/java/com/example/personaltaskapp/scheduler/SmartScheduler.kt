package com.example.personaltaskapp.scheduler

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

/**
 * Input structure for generating smart suggestions.
 */

object SmartScheduler {

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSuggestions(input: SmartScheduleInput): List<SmartSuggestion> {
        val today = LocalDate.now()
        val date = input.targetDate

        // ❌ No suggestions for past days
        if (date.isBefore(today)) return emptyList()

        val keyIso = date.toString()

        val suggestions = mutableListOf<SmartSuggestion>()

        // ----------------------------------------------
        // 1) Suggest UNSCHEDULED FLEXIBLE TASKS
        // ----------------------------------------------
        val unscheduled = input.tasks.filter { t ->
            t.isFlexible &&
                    t.fixedStartIso == null &&
                    (t.dueDateIso == null || !LocalDate.parse(t.dueDateIso).isBefore(date))
        }

        unscheduled.forEach { t ->
            suggestions += SmartSuggestion(
                taskId = t.id,
                title = t.title,
                suggestedDateIso = keyIso,
                reason = "Flexible task can be done anytime before its due date",
                confidence = 0.6f
            )
        }

        // ----------------------------------------------
        // 2) Suggest tasks due soon
        // ----------------------------------------------
        val dueSoon = input.tasks.filter { t ->
            t.dueDateIso != null &&
                    !LocalDate.parse(t.dueDateIso).isBefore(date) &&
                    LocalDate.parse(t.dueDateIso).minusDays(2) <= date &&
                    !t.isCompleted
        }

        dueSoon.forEach { t ->
            suggestions += SmartSuggestion(
                taskId = t.id,
                title = t.title,
                suggestedDateIso = keyIso,
                reason = "Task is due soon",
                confidence = 0.85f
            )
        }

        // ----------------------------------------------
        // 3) Suggest habit reinforcement (optional)
        // Example: habit for this day exists? Encourage it.
        // ----------------------------------------------
        val habitsToday = input.habits.filter { h ->
            val freq = h.frequency.uppercase()
            if (freq == "DAILY") return@filter true

            val dow3 = date.dayOfWeek.name.take(3).uppercase()
            freq.split(",").map { it.trim() }.contains(dow3)
        }

        habitsToday.forEach { h ->
            suggestions += SmartSuggestion(
                taskId = -h.id, // negative id = habit suggestion
                title = h.title,
                suggestedDateIso = keyIso,
                reason = "Habit scheduled for this day",
                confidence = 0.7f
            )
        }

        return suggestions
    }
}
