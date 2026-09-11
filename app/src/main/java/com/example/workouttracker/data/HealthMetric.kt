package com.example.workouttracker.data

data class HealthMetric(
    val date: String,
    val water: Float = 0f,
    val protein: Float = 0f,
    val bodyWeight: Float = 0f
)
