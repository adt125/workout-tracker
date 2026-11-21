package com.example.workouttracker.data

data class FoodEntry(
    val id: Long = 0,
    val date: String,
    val name: String,
    val qty: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val source: String,
    val createdAt: Long,
    val mealType: String = "Meal"
)

data class AiSummary(val weekStart: String, val headline: String, val bullets: List<String>, val suggestion: String)
