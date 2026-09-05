package com.example.workouttracker.data

// Simple data class representing a weight log
data class WeightEntry(
    val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val weightKg: Float
)
