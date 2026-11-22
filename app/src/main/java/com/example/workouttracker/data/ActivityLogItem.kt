package com.example.workouttracker.data

enum class ActivityType {
    WEIGHT,
    WATER,
    PROTEIN,
    WORKOUT,
    NOTE
}

data class ActivityLogItem(
    val id: Long = 0,
    val type: ActivityType,
    val timestamp: String, // e.g. "07:45 AM"
    val date: String,      // "YYYY-MM-DD"
    val title: String,     // e.g. "Weight", "Water", "Protein", "Workout"
    val detail: String,    // e.g. "72.4 kg", "+500 ml", "+30 g"
    val numericValue: Float? = null
)
