package com.example.workouttracker

import com.example.workouttracker.data.SetRecord
import com.example.workouttracker.data.WeightEntry
import com.example.workouttracker.util.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class FitnessUtilsTest {

    @Test
    fun testFormatWeight() {
        assertEquals("71.5", formatWeight(71.45))
        assertEquals("71.4", formatWeight(71.44))
        assertEquals("71.4", formatWeight(71.40))
        assertEquals("70.0", formatWeight(70.00))
    }

    @Test
    fun testGoalProgress() {
        val p1 = goalProgress(69.0, 71.4, 73.5)
        assertEquals(0.5333f, p1!!, 0.01f)

        val p2 = goalProgress(69.0, 74.0, 73.5)
        assertEquals(1.0f, p2!!, 0.0f)

        val p3 = goalProgress(69.0, 68.0, 73.5)
        assertEquals(0.0f, p3!!, 0.0f)

        val p4 = goalProgress(70.0, 71.0, 70.0)
        assertNull(p4)
    }

    @Test
    fun testWeightChange7d() {
        val today = LocalDate.of(2026, 10, 12)
        val entries = listOf(
            WeightEntry(1, "2026-10-06", 71.6f),
            WeightEntry(2, "2026-10-09", 71.5f),
            WeightEntry(3, "2026-10-12", 71.4f)
        )
        val change = weightChange7d(entries, today)
        assertEquals(-0.2, change!!, 0.001)

        val insufficientEntries = listOf(
            WeightEntry(1, "2026-10-12", 71.4f)
        )
        assertNull(weightChange7d(insufficientEntries, today))
    }

    @Test
    fun testRelativeDay() {
        val today = LocalDate.of(2026, 10, 12)
        assertEquals("Today", relativeDay(LocalDate.of(2026, 10, 12), today))
        assertEquals("Yesterday", relativeDay(LocalDate.of(2026, 10, 11), today))
        assertEquals("3 days ago", relativeDay(LocalDate.of(2026, 10, 9), today))
    }

    @Test
    fun testProgressDelta() {
        val prev = listOf(
            SetRecord(weight = 20f, reps = 10),
            SetRecord(weight = 22.5f, reps = 8),
            SetRecord(weight = 25f, reps = 4)
        )
        val curr = listOf(
            SetRecord(weight = 20f, reps = 12),
            SetRecord(weight = 22.5f, reps = 10),
            SetRecord(weight = 25f, reps = 5)
        )
        val delta = progressDelta(curr, prev)
        assertEquals(ExerciseDelta.RepsUp(1), delta)

        val prevHeavier = listOf(SetRecord(weight = 20f, reps = 8))
        val currHeavier = listOf(SetRecord(weight = 25f, reps = 5))
        assertEquals(ExerciseDelta.TopWeightUp(5.0), progressDelta(currHeavier, prevHeavier))

        assertEquals(ExerciseDelta.None, progressDelta(curr, emptyList()))
    }
}
