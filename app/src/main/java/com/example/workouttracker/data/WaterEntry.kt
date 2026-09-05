package com.example.workouttracker.data

// Data class for water logs
data class WaterEntry(
    val id: Long = 0,
    val date: String,
    val amountMl: Int
)
