package com.example.workouttracker.util

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
        } catch (e: DateTimeParseException) {
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
