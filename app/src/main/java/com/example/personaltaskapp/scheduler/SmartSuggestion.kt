package com.example.personaltaskapp.scheduler

data class SmartSuggestion(
    val taskId: Int,
    val title: String,
    val suggestedDateIso: String,
    val reason: String,
    val confidence: Float
)