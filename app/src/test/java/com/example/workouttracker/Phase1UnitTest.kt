package com.example.workouttracker

import com.example.workouttracker.data.FitnessGoal
import com.example.workouttracker.data.UserGoal
import com.example.workouttracker.data.WeightEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class Phase1UnitTest {

    @Test
    fun userGoal_defaultValues_areCorrect() {
        val goal = UserGoal()
        assertEquals(120f, goal.dailyProteinTarget, 0.01f)
        assertEquals(3.0f, goal.dailyWaterTarget, 0.01f)
        assertEquals(4, goal.workoutsPerWeek)
        assertEquals(FitnessGoal.GENERAL_FITNESS, goal.fitnessGoal)
    }

    @Test
    fun fitnessGoal_fromName_parsesCorrectly() {
        assertEquals(FitnessGoal.BUILD_MUSCLE, FitnessGoal.fromName("BUILD_MUSCLE"))
        assertEquals(FitnessGoal.LOSE_FAT, FitnessGoal.fromName("Lose Fat"))
        assertEquals(FitnessGoal.MAINTAIN_WEIGHT, FitnessGoal.fromName("Maintain Weight"))
        assertEquals(FitnessGoal.GENERAL_FITNESS, FitnessGoal.fromName("invalid_name"))
    }

    @Test
    fun proteinProgress_percentageAndRemaining_areAccurate() {
        val target = 120f
        val current = 60f
        val percentage = ((current / target) * 100).toInt()
        val remaining = (target - current).coerceAtLeast(0f)

        assertEquals(50, percentage)
        assertEquals(60f, remaining, 0.01f)
    }

    @Test
    fun weightDifference_calculation_isAccurate() {
        val entries = listOf(
            WeightEntry(1, "2026-09-01", 75.0f),
            WeightEntry(2, "2026-09-15", 73.8f)
        )
        val latest = entries.last().weightKg
        val first = entries.first().weightKg
        val diff = latest - first

        assertEquals(-1.2f, diff, 0.01f)
    }
}
