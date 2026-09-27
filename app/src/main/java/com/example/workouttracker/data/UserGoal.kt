package com.example.workouttracker.data

enum class FitnessGoal(val displayName: String) {
    BUILD_MUSCLE("Build muscle"),
    LOSE_FAT("Lose fat"),
    MAINTAIN_WEIGHT("Maintain weight"),
    GENERAL_FITNESS("General fitness");

    companion object {
        fun fromName(name: String?): FitnessGoal {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: GENERAL_FITNESS
        }
    }
}

data class UserGoal(
    val currentWeight: Float? = null,
    val targetWeight: Float? = null,
    val fitnessGoal: FitnessGoal = FitnessGoal.GENERAL_FITNESS,
    val targetDate: String? = null,
    val dailyProteinTarget: Float = 120f,
    val dailyWaterTarget: Float = 3.0f,
    val dailyCalorieTarget: Float? = null,
    val workoutsPerWeek: Int = 4,
    val targetWorkoutDurationMinutes: Int? = null
)
