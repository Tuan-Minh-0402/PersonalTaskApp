package com.example.personaltaskapp.model

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    WEEKDAYS,
    WEEKENDS,
    CUSTOM,
    MONTHLY
}

data class Habit(

    // Identity
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val isActive: Boolean = true,

    // Scheduling similarity with Task ---------------------------------------

    // Flexible or fixed?
    val isFlexible: Boolean = true,               // SAME as Task.isFlexible
    val durationMinutes: Int? = null,             // optional (Q2=optional)
    val earliestStartIso: String? = null,         // SAME as Task.earliestStartIso
    val fixedStartIso: String? = null,            // SAME as Task.fixedStartIso (when fixed)
    val preferredTimeBucket: String? = null,      // "morning", "afternoon", "evening"

    // Smart schedule result (assigned time)
    val assignedStartIso: String? = null,         // SAME naming as Task
    val assignedEndIso: String? = null,

    // Habit frequency --------------------------------------------------------
    val frequency: HabitFrequency = HabitFrequency.DAILY,

    // Only used when frequency = CUSTOM
    // 1 = Monday … 7 = Sunday
    val customWeekdays: List<Int>? = null,

    // Tracking ---------------------------------------------------------------
    val lastCompletedIso: String? = null,
    val streak: Int = 0,

    // Meta ------------------------------------------------------------------
    val createdIso: String? = null
)
