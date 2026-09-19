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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseHelper(application)
    private val gson = Gson()

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

    private val _exerciseSuggestions = mutableStateOf<List<String>>(emptyList())
    val exerciseSuggestions: State<List<String>> = _exerciseSuggestions

    private val _weekWorkouts = mutableStateOf<List<Pair<WorkoutEntry, String>>>(emptyList())
    val weekWorkouts: State<List<Pair<WorkoutEntry, String>>> = _weekWorkouts

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

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        refreshToday()
        refreshExercises()
        loadRecentWorkouts()
        loadWeekMetrics()
    }

    fun refreshToday() {
        val today = LocalDate.now().format(formatter)
        viewModelScope.launch(Dispatchers.IO) {
            val metrics = db.getDailyMetrics(today)
            val workouts = db.getWorkoutsForDate(today)
            val workoutCount = workouts.filter { it.first.exerciseId != null }.size
            val lastWeight = db.getLatestWeight()

            withContext(Dispatchers.Main) {
                _todayWater.value = metrics.water
                _todayProtein.value = metrics.protein
                _todayWeight.value = if (metrics.bodyWeight > 0) metrics.bodyWeight else null
                _lastKnownWeight.value = lastWeight
                _todayWorkouts.value = workoutCount
            }
        }
    }

    fun loadWeekWorkouts() {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        viewModelScope.launch(Dispatchers.IO) {
            val workouts = db.getWorkoutsBetween(startOfWeek.format(formatter), today.format(formatter))
            withContext(Dispatchers.Main) {
                _weekWorkouts.value = workouts
            }
        }
    }

    fun loadWorkoutsForDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val workouts = db.getWorkoutsForDate(date)
            val metrics = db.getDailyMetrics(date)
            withContext(Dispatchers.Main) {
                _dayWorkoutsList.value = workouts
                _dayMetrics.value = metrics
            }
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
        val endOfWeek = LocalDate.now().plusWeeks(weekOffset.toLong())
        val startOfWeek = endOfWeek.minusDays(6)
        
        viewModelScope.launch(Dispatchers.IO) {
            val metrics = db.getMetricsBetween(startOfWeek.format(formatter), endOfWeek.format(formatter))
            val workoutCounts = db.getWorkoutCountsBetween(startOfWeek.format(formatter), endOfWeek.format(formatter))
            withContext(Dispatchers.Main) {
                _weekMetrics.value = metrics
                _weekWorkoutCounts.value = workoutCounts
            }
        }
    }

    suspend fun getPreviousRecord(exerciseId: Int, date: String): List<SetRecord> = withContext(Dispatchers.IO) {
        db.getLatestSetDataForExercise(exerciseId, date)
    }

    fun refreshExercises() {
        viewModelScope.launch(Dispatchers.IO) {
            val exercises = db.getAllExercises().map { it.name }
            withContext(Dispatchers.Main) {
                _exerciseSuggestions.value = exercises
            }
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
            
            // For cardio, we just store the first record as a single set for now
            val sets = if (isCardio) listOfNotNull(setRecords.firstOrNull()) else setRecords
            
            db.insertWorkout(sessionId, exId, sets)
            
            refreshToday()
            refreshExercises()
            loadRecentWorkouts()
            
            // If we replaced an entry, we might need to refresh the day log if it's currently showing
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
            db.updateHealthMetrics(
                HealthMetric(
                    date = date,
                    water = water ?: current.water,
                    protein = protein ?: current.protein,
                    bodyWeight = weight ?: current.bodyWeight
                )
            )
            refreshToday()
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
