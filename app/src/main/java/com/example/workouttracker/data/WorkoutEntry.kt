package com.example.workouttracker.data

// Data class for workout logs
data class WorkoutEntry(
    val id: Long = 0,
    val date: String,
    val exerciseName: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Float? = null,
    val durationMin: Int? = null,
    val distanceKm: Float? = null,
    val avgSpeed: Float? = null,
    val incline: Float? = null,
    val notes: String? = null
)
