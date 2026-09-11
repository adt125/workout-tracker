package com.example.workouttracker.data

data class WorkoutSession(
    val id: Long = 0,
    val date: String,
    val startTime: String? = null,
    val notes: String? = null
)

data class WorkoutEntry(
    val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Int?,
    val exerciseName: String = "",
    val date: String = "", // Added for UI convenience
    val sets: List<SetRecord> = emptyList()
)

data class WorkoutSet(
    val id: Long = 0,
    val entryId: Long,
    val weight: Float? = null,
    val reps: Int? = null,
    val duration: Int? = null,
    val order: Int = 0
)

data class SetRecord(
    val weight: Float? = null,
    val reps: Int? = null,
    val duration: Int? = null
)
