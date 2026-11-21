package com.example.workouttracker.data

import android.content.pm.ApplicationInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

data class ParsedItem(
    val name: String, val qty: String, val calories: Int, val proteinG: Int,
    val carbsG: Int, val fatG: Int, val confidence: String
) {
    companion object {
        fun from(raw: Map<String, Any>) = ParsedItem(
            name = raw["name"].toString(), qty = raw["qty"].toString(),
            calories = (raw["calories"] as? Number)?.toInt() ?: 0,
            proteinG = (raw["protein_g"] as? Number)?.toInt() ?: 0,
            carbsG = (raw["carbs_g"] as? Number)?.toInt() ?: 0,
            fatG = (raw["fat_g"] as? Number)?.toInt() ?: 0,
            confidence = raw["confidence"]?.toString() ?: "medium"
        )
    }
}

class FoodAiClient {
    private val functions = FirebaseFunctions.getInstance("asia-south1").also {
        if (FirebaseApp.getInstance().applicationContext.applicationInfo.flags and
            ApplicationInfo.FLAG_DEBUGGABLE != 0
        ) {
            // Android Emulator -> host machine.
            it.useEmulator("10.0.2.2", 5001)
        }
    }

    suspend fun parse(text: String, imageB64: String?): List<ParsedItem> {
        if (isDebugBuild()) {
            return listOf(
                ParsedItem("Roti", "2 pieces", 210, 6, 40, 3, "high"),
                ParsedItem("Dal tadka", "1 bowl, about 200 g", 180, 9, 24, 5, "medium"),
                ParsedItem("Curd", "150 g", 90, 6, 7, 4, "high")
            )
        }
        AiAuth.ensureSignedIn()
        val args = mutableMapOf<String, Any>("text" to text)
        if (imageB64 != null) args["imageBase64"] = imageB64
        val result = functions.getHttpsCallable("parseFood").withTimeout(40, TimeUnit.SECONDS).call(args).await()
        @Suppress("UNCHECKED_CAST")
        return ((result.getData() as Map<String, Any>)["items"] as List<Map<String, Any>>).map(ParsedItem::from)
    }

    suspend fun weeklySummary(stats: Map<String, Any>): AiSummary {
        if (isDebugBuild()) {
            fun number(key: String): Float = (stats[key] as? Number)?.toFloat() ?: 0f
            fun comparison(current: Float, previous: Float): String {
                if (previous <= 0f) return "no previous data"
                val change = ((current - previous) / previous * 100f).roundToInt()
                return when {
                    change > 0 -> "up ${change}%"
                    change < 0 -> "down ${abs(change)}%"
                    else -> "unchanged"
                }
            }
            val protein = number("dailyProteinG")
            val proteinGoal = number("proteinGoalG")
            val calories = number("dailyCaloriesKcal")
            val calorieGoal = number("dailyCalorieTargetKcal")
            val weight = number("averageWeightKg")
            val workouts = number("workoutsDone").toInt()
            val lastWeekWorkouts = number("workoutsLastWeek").toInt()
            val cardioDays = number("cardioDays").toInt()
            return AiSummary(
                weekStart = stats["weekStart"].toString(),
                headline = "Your week against your goals",
                bullets = listOf(
                    "Daily protein intake was ${protein.toInt()}g (${comparison(protein, number("previousDailyProteinG"))} vs last week); goal ${proteinGoal.toInt()}g, ${number("daysProteinGoalHit").toInt()} days met.",
                    if (calorieGoal > 0f) "Daily average calorie intake was ${calories.toInt()} kcal (${comparison(calories, number("previousDailyCaloriesKcal"))} vs last week); goal ${calorieGoal.toInt()} kcal." else "Daily average calorie intake was ${calories.toInt()} kcal (${comparison(calories, number("previousDailyCaloriesKcal"))} vs last week); set a calorie goal to track it against a target.",
                    "Average weight was ${"%.1f".format(weight)} kg (${comparison(weight, number("previousAverageWeightKg"))} vs last week).",
                    "You completed $workouts workouts, compared with $lastWeekWorkouts last week.",
                    "You did cardio $cardioDays/4 days."
                ),
                suggestion = "Use these comparisons to adjust next week's nutrition and training."
            )
        }
        AiAuth.ensureSignedIn()
        val result = functions.getHttpsCallable("weeklySummary").withTimeout(40, TimeUnit.SECONDS).call(mapOf("stats" to stats)).await()
        val data = result.getData() as Map<*, *>
        @Suppress("UNCHECKED_CAST")
        return AiSummary(stats["weekStart"].toString(), data["headline"].toString(), (data["bullets"] as? List<Any?>)?.map { it.toString() } ?: emptyList(), data["suggestion"].toString())
    }

    private fun isDebugBuild(): Boolean =
        FirebaseApp.getInstance().applicationContext.applicationInfo.flags and
            ApplicationInfo.FLAG_DEBUGGABLE != 0
}
