package com.example.workouttracker.util

import com.example.workouttracker.data.SetRecord
import com.example.workouttracker.data.WeightEntry
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * Utility functions specified in Appendix A of Fitness App Redesign guide.
 */

/**
 * Formats weight to 1 decimal place rounded half up.
 */
fun formatWeight(kg: Double): String {
    return BigDecimal.valueOf(kg)
        .setScale(1, RoundingMode.HALF_UP)
        .toString()
}

/**
 * Calculates goal progress ratio clamped to 0..1.
 * Returns null if start == target (no target progression).
 */
fun goalProgress(start: Double, current: Double, target: Double): Float? {
    if (start == target) return null
    val progress = (current - start) / (target - start)
    return progress.coerceIn(0.0, 1.0).toFloat()
}

/**
 * Returns latest weight entry minus oldest weight entry in the last 7 days window (inclusive).
 * Returns null if fewer than 2 entries fall within the window.
 */
fun weightChange7d(entries: List<WeightEntry>, today: LocalDate): Double? {
    val windowStart = today.minusDays(6)
    val parsedEntries = entries.mapNotNull { entry ->
        try {
            val parsedDate = LocalDate.parse(entry.date)
            parsedDate to entry.weightKg.toDouble()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    val validEntries = parsedEntries
        .filter { (date, _) -> !date.isBefore(windowStart) && !date.isAfter(today) }
        .sortedBy { (date, _) -> date }

    if (validEntries.size < 2) return null

    val oldest = validEntries.first().second
    val latest = validEntries.last().second
    return latest - oldest
}

/**
 * Formats relative day description ("Today", "Yesterday", or "N days ago").
 */
fun relativeDay(date: LocalDate, today: LocalDate): String {
    val daysDiff = ChronoUnit.DAYS.between(date, today)
    return when {
        daysDiff == 0L -> "Today"
        daysDiff == 1L -> "Yesterday"
        daysDiff > 1L -> "$daysDiff days ago"
        else -> date.toString()
    }
}

sealed class ExerciseDelta {
    object None : ExerciseDelta()
    data class TopWeightUp(val diffKg: Double) : ExerciseDelta()
    data class RepsUp(val diffReps: Int) : ExerciseDelta()
    data class VolumeUp(val percent: Int) : ExerciseDelta()
    object Same : ExerciseDelta()
    data class Lower(val desc: String) : ExerciseDelta()
}

/**
 * Calculates progress delta between current session and previous session.
 */
fun progressDelta(currentSets: List<SetRecord>, previousSets: List<SetRecord>): ExerciseDelta {
    if (previousSets.isEmpty()) return ExerciseDelta.None

    val currentTop = currentSets.filter { it.weight != null && it.reps != null }
        .maxWithOrNull(compareBy<SetRecord> { it.weight!! }.thenBy { it.reps!! })

    val previousTop = previousSets.filter { it.weight != null && it.reps != null }
        .maxWithOrNull(compareBy<SetRecord> { it.weight!! }.thenBy { it.reps!! })

    if (currentTop == null || previousTop == null) return ExerciseDelta.None

    val curWeight = currentTop.weight!!.toDouble()
    val prevWeight = previousTop.weight!!.toDouble()
    val curReps = currentTop.reps!!
    val prevReps = previousTop.reps!!

    // 1. Top weight higher
    if (curWeight > prevWeight) {
        val diff = curWeight - prevWeight
        return ExerciseDelta.TopWeightUp(diff)
    }

    // 2. Same top weight, more reps at it
    if (curWeight == prevWeight && curReps > prevReps) {
        val diff = curReps - prevReps
        return ExerciseDelta.RepsUp(diff)
    }

    val curVolume = currentSets.sumOf { (it.weight?.toDouble() ?: 0.0) * (it.reps ?: 0) }
    val prevVolume = previousSets.sumOf { (it.weight?.toDouble() ?: 0.0) * (it.reps ?: 0) }

    // 3. Same top set, higher total volume
    if (curWeight == prevWeight && curReps == prevReps && curVolume > prevVolume) {
        val pct = if (prevVolume > 0) ((curVolume - prevVolume) / prevVolume * 100).toInt() else 0
        return ExerciseDelta.VolumeUp(pct.coerceAtLeast(1))
    }

    // 4. Identical
    if (curWeight == prevWeight && curReps == prevReps && curVolume == prevVolume) {
        return ExerciseDelta.Same
    }

    // 5. Lower
    if (curWeight < prevWeight) {
        val diff = prevWeight - curWeight
        return ExerciseDelta.Lower("-${formatWeight(diff)} kg")
    }
    if (curWeight == prevWeight && curReps < prevReps) {
        val diff = prevReps - curReps
        return ExerciseDelta.Lower("-$diff ${if (diff == 1) "rep" else "reps"}")
    }

    return ExerciseDelta.Lower("Lower")
}
