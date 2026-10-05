package com.example.workouttracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.data.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.abs

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseHelper(application)
    private val gson = Gson()

    private val _userGoal = mutableStateOf<UserGoal?>(null)
    val userGoal: State<UserGoal?> = _userGoal

    private val _hasCompletedOnboarding = mutableStateOf(false)
    val hasCompletedOnboarding: State<Boolean> = _hasCompletedOnboarding

    private val _todayWeight = mutableStateOf<Float?>(null)
    val todayWeight: State<Float?> = _todayWeight

    private val _lastKnownWeight = mutableStateOf<Float?>(null)
    val lastKnownWeight: State<Float?> = _lastKnownWeight

    private val _todayWater = mutableStateOf(0f)
    val todayWater: State<Float> = _todayWater

    private val _todayProtein = mutableStateOf(0f)
    val todayProtein: State<Float> = _todayProtein

    private val _todayWorkouts = mutableStateOf(0)
    val todayWorkouts: State<Int> = _todayWorkouts

    private val _todayNotes = mutableStateOf<String?>(null)
    val todayNotes: State<String?> = _todayNotes

    private val _todayTimeline = mutableStateOf<List<ActivityLogItem>>(emptyList())
    val todayTimeline: State<List<ActivityLogItem>> = _todayTimeline

    private val _weightHistory = mutableStateOf<List<WeightEntry>>(emptyList())
    val weightHistory: State<List<WeightEntry>> = _weightHistory

    private val _dayNotes = mutableStateOf<String?>(null)
    val dayNotes: State<String?> = _dayNotes

    private val _exerciseSuggestions = mutableStateOf<List<String>>(emptyList())
    val exerciseSuggestions: State<List<String>> = _exerciseSuggestions

    private val _weekWorkouts = mutableStateOf<List<Pair<WorkoutEntry, String>>>(emptyList())
    val weekWorkouts: State<List<Pair<WorkoutEntry, String>>> = _weekWorkouts

    private val _weekSummaries = mutableStateOf<List<DaySummary>>(emptyList())
    val weekSummaries: State<List<DaySummary>> = _weekSummaries

    private val _dayWorkoutsList = mutableStateOf<List<Pair<WorkoutEntry, String>>>(emptyList())
    val dayWorkoutsList: State<List<Pair<WorkoutEntry, String>>> = _dayWorkoutsList

    private val _dayMetrics = mutableStateOf<HealthMetric?>(null)
    val dayMetrics: State<HealthMetric?> = _dayMetrics

    private val _recentWorkouts = mutableStateOf<List<Pair<WorkoutEntry, String>>>(emptyList())
    val recentWorkouts: State<List<Pair<WorkoutEntry, String>>> = _recentWorkouts

    private val _weekMetrics = mutableStateOf<List<HealthMetric>>(emptyList())
    val weekMetrics: State<List<HealthMetric>> = _weekMetrics

    private val _weekWorkoutCounts = mutableStateOf<Map<String, Int>>(emptyMap())
    val weekWorkoutCounts: State<Map<String, Int>> = _weekWorkoutCounts

    private val _weekCardioCounts = mutableStateOf<Map<String, Int>>(emptyMap())
    val weekCardioCounts: State<Map<String, Int>> = _weekCardioCounts

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    init {
        loadUserGoal()
        refreshToday()
        refreshExercises()
        loadRecentWorkouts()
        loadWeekMetrics()
        loadWeightHistory(30)
    }

    fun loadUserGoal() {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = db.getUserGoal()
            withContext(Dispatchers.Main) {
                _userGoal.value = goal ?: UserGoal()
                _hasCompletedOnboarding.value = goal != null
            }
        }
    }

    fun saveUserGoal(goal: UserGoal) {
        viewModelScope.launch(Dispatchers.IO) {
            db.saveUserGoal(goal)
            withContext(Dispatchers.Main) {
                _userGoal.value = goal
                _hasCompletedOnboarding.value = true
            }
            refreshToday()
        }
    }

    fun refreshToday() {
        val today = LocalDate.now().format(formatter)
        viewModelScope.launch(Dispatchers.IO) {
            val metrics = db.getDailyMetrics(today)
            val workouts = db.getWorkoutsForDate(today)
            val workoutCount = workouts.filter { it.first.exerciseId != null }.size
            val lastWeight = db.getLatestWeight()
            val session = db.getSessionForDate(today)
            val activityLogs = db.getActivityLogsForDate(today)

            withContext(Dispatchers.Main) {
                _todayWater.value = metrics.water
                _todayProtein.value = metrics.protein
                _todayWeight.value = if (metrics.bodyWeight > 0) metrics.bodyWeight else null
                _lastKnownWeight.value = lastWeight
                _todayWorkouts.value = workoutCount
                _todayNotes.value = session?.notes
                _todayTimeline.value = activityLogs
            }
        }
    }

    fun loadWeightHistory(days: Int = 30) {
        val fromDate = LocalDate.now().minusDays(days.toLong()).format(formatter)
        viewModelScope.launch(Dispatchers.IO) {
            val history = db.getWeightHistory(fromDate)
            withContext(Dispatchers.Main) {
                _weightHistory.value = history
            }
        }
    }

    fun addWaterIncrement(amountL: Float, date: String = LocalDate.now().format(formatter)) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = db.getDailyMetrics(date)
            val newWater = (current.water + amountL).coerceAtLeast(0f)
            db.updateHealthMetrics(current.copy(water = newWater))

            val now = LocalTime.now().format(timeFormatter)
            val absAmount = abs(amountL)
            val detail = if (amountL >= 0) {
                if (amountL >= 1.0f) String.format(Locale.US, "+%.1f L", amountL)
                else String.format(Locale.US, "+%.0f ml", amountL * 1000)
            } else {
                if (absAmount >= 1.0f) String.format(Locale.US, "-%.1f L", absAmount)
                else String.format(Locale.US, "-%.0f ml", absAmount * 1000)
            }
            db.insertActivityLog(
                ActivityLogItem(
                    type = ActivityType.WATER,
                    timestamp = now,
                    date = date,
                    title = "Water",
                    detail = detail,
                    numericValue = amountL
                )
            )

            refreshToday()
            loadWorkoutsForDate(date)
        }
    }

    fun addProteinIncrement(amountG: Float, date: String = LocalDate.now().format(formatter)) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = db.getDailyMetrics(date)
            val newProtein = (current.protein + amountG).coerceAtLeast(0f)
            db.updateHealthMetrics(current.copy(protein = newProtein))

            val now = LocalTime.now().format(timeFormatter)
            val absAmount = abs(amountG)
            val detail = if (amountG >= 0) {
                String.format(Locale.US, "+%.0f g", amountG)
            } else {
                String.format(Locale.US, "-%.0f g", absAmount)
            }
            db.insertActivityLog(
                ActivityLogItem(
                    type = ActivityType.PROTEIN,
                    timestamp = now,
                    date = date,
                    title = "Protein",
                    detail = detail,
                    numericValue = amountG
                )
            )

            refreshToday()
            loadWorkoutsForDate(date)
        }
    }

    fun logWeight(weightKg: Float, date: String = LocalDate.now().format(formatter)) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = db.getDailyMetrics(date)
            db.updateHealthMetrics(current.copy(bodyWeight = weightKg))

            val now = LocalTime.now().format(timeFormatter)
            db.insertActivityLog(
                ActivityLogItem(
                    type = ActivityType.WEIGHT,
                    timestamp = now,
                    date = date,
                    title = "Weight",
                    detail = String.format(Locale.US, "%.1f kg", weightKg),
                    numericValue = weightKg
                )
            )

            // Update current goal currentWeight if set
            val goal = db.getUserGoal()
            if (goal != null) {
                db.saveUserGoal(goal.copy(currentWeight = weightKg))
                withContext(Dispatchers.Main) {
                    _userGoal.value = goal.copy(currentWeight = weightKg)
                }
            }

            refreshToday()
            loadWeightHistory(30)
            loadWorkoutsForDate(date)
        }
    }

    fun loadWeekWorkouts() {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val from = startOfWeek.format(formatter)
        val to = today.format(formatter)

        viewModelScope.launch(Dispatchers.IO) {
            val workouts = db.getWorkoutsBetween(from, to)
            val metrics = db.getMetricsBetween(from, to).associateBy { it.date }
            val sessions = db.getSessionsBetween(from, to).associateBy { it.date }

            val workoutGrouped = workouts.groupBy { hb -> hb.first.date }

            val allDates = mutableSetOf<String>()
            allDates.addAll(workoutGrouped.keys)
            allDates.addAll(metrics.keys)
            allDates.addAll(sessions.keys)

            val summaries = allDates.sortedDescending().map { date ->
                DaySummary(
                    date = date,
                    workouts = workoutGrouped[date] ?: emptyList(),
                    metrics = metrics[date],
                    notes = sessions[date]?.notes
                )
            }

            withContext(Dispatchers.Main) {
                _weekWorkouts.value = workouts
                _weekSummaries.value = summaries
            }
        }
    }

    suspend fun getDaySummary(date: String): DaySummary = withContext(Dispatchers.IO) {
        val workouts = db.getWorkoutsForDate(date)
        val metrics = db.getDailyMetrics(date)
        val session = db.getSessionForDate(date)
        DaySummary(date, workouts, metrics, session?.notes)
    }

    fun loadWorkoutsForDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val workouts = db.getWorkoutsForDate(date)
            val metrics = db.getDailyMetrics(date)
            val session = db.getSessionForDate(date)
            withContext(Dispatchers.Main) {
                _dayWorkoutsList.value = workouts
                _dayMetrics.value = metrics
                _dayNotes.value = session?.notes
            }
        }
    }

    fun updateSessionNotes(date: String, notes: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            db.updateSessionNotes(date, notes)
            refreshToday()
            loadWorkoutsForDate(date)
            loadRecentWorkouts()
        }
    }

    fun loadRecentWorkouts() {
        viewModelScope.launch(Dispatchers.IO) {
            val workouts = db.getRecentWorkouts(6)
            withContext(Dispatchers.Main) {
                _recentWorkouts.value = workouts
            }
        }
    }

    fun loadWeekMetrics(weekOffset: Int = 0) {
        val targetDate = LocalDate.now().plusWeeks(weekOffset.toLong())
        
        // Metrics use continuous rolling 7 days (baki k liye thik hai)
        val rollingEnd = targetDate
        val rollingStart = rollingEnd.minusDays(6)

        // Cardio and exercise use Monday to Sunday week
        val mondayOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sundayOfWeek = mondayOfWeek.plusDays(6)
        
        viewModelScope.launch(Dispatchers.IO) {
            val metrics = db.getMetricsBetween(rollingStart.format(formatter), rollingEnd.format(formatter))
            val workoutCounts = db.getWorkoutCountsBetween(mondayOfWeek.format(formatter), sundayOfWeek.format(formatter))
            val cardioCounts = db.getCardioWorkoutCountsBetween(mondayOfWeek.format(formatter), sundayOfWeek.format(formatter))
            withContext(Dispatchers.Main) {
                _weekMetrics.value = metrics
                _weekWorkoutCounts.value = workoutCounts
                _weekCardioCounts.value = cardioCounts
            }
        }
    }

    suspend fun getPreviousRecord(exerciseId: Int, date: String): List<SetRecord> = withContext(Dispatchers.IO) {
        db.getLatestSetDataForExercise(exerciseId, date)
    }

    fun refreshExercises() {
        viewModelScope.launch(Dispatchers.IO) {
            db.mergeDuplicateExercises()
            val exercises = db.getAllExercises().map { it.name }
            withContext(Dispatchers.Main) {
                _exerciseSuggestions.value = exercises
            }
        }
    }

    fun mergeDuplicateExercises() {
        viewModelScope.launch(Dispatchers.IO) {
            db.mergeDuplicateExercises()
            refreshExercises()
            loadRecentWorkouts()
        }
    }

    fun addWorkout(
        exerciseName: String,
        setRecords: List<SetRecord>,
        isCardio: Boolean = false,
        date: String = LocalDate.now().format(formatter),
        replaceEntryId: Long? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (replaceEntryId != null) {
                db.deleteWorkoutEntry(replaceEntryId)
            }
            
            val sessionId = db.getOrCreateSessionId(date)
            val exId = db.getOrCreateExerciseId(exerciseName)
            
            val sets = if (isCardio) listOfNotNull(setRecords.firstOrNull()) else setRecords
            
            db.insertWorkout(sessionId, exId, sets)

            val now = LocalTime.now().format(timeFormatter)
            val setCount = sets.size
            db.insertActivityLog(
                ActivityLogItem(
                    type = ActivityType.WORKOUT,
                    timestamp = now,
                    date = date,
                    title = "Workout",
                    detail = "$exerciseName ($setCount set${if (setCount > 1) "s" else ""})"
                )
            )
            
            refreshToday()
            refreshExercises()
            loadRecentWorkouts()
            loadWorkoutsForDate(date)
        }
    }

    fun deleteWorkout(entryId: Long, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.deleteWorkoutEntry(entryId)
            refreshToday()
            loadRecentWorkouts()
            loadWorkoutsForDate(date)
        }
    }

    fun updateDailyMetrics(
        water: Float? = null,
        protein: Float? = null,
        weight: Float? = null,
        date: String = LocalDate.now().format(formatter)
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = db.getDailyMetrics(date)
            val updated = HealthMetric(
                date = date,
                water = water ?: current.water,
                protein = protein ?: current.protein,
                bodyWeight = weight ?: current.bodyWeight
            )
            db.updateHealthMetrics(updated)

            if (weight != null && weight != current.bodyWeight) {
                logWeight(weight, date)
            } else {
                refreshToday()
                loadWorkoutsForDate(date)
            }
        }
    }

    fun getBackupDataJson(): String {
        val data = db.getAllDataForBackup()
        return gson.toJson(data)
    }

    suspend fun getWorkoutEntry(entryId: Long): Pair<WorkoutEntry, String>? = withContext(Dispatchers.IO) {
        db.getWorkoutEntryById(entryId)
    }
}
