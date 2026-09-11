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

    private val _recentWorkouts = mutableStateOf<List<Pair<WorkoutEntry, String>>>(emptyList())
    val recentWorkouts: State<List<Pair<WorkoutEntry, String>>> = _recentWorkouts

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        refreshToday()
        refreshExercises()
        loadRecentWorkouts()
    }

    fun refreshToday() {
        val today = LocalDate.now().format(formatter)
        viewModelScope.launch(Dispatchers.IO) {
            val metrics = db.getDailyMetrics(today)
            val workouts = db.getWorkoutsForDate(today)
            val workoutCount = workouts.filter { it.first.exerciseId != null }.size

            withContext(Dispatchers.Main) {
                _todayWater.value = metrics.water
                _todayProtein.value = metrics.protein
                _todayWeight.value = if (metrics.bodyWeight > 0) metrics.bodyWeight else null
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
            withContext(Dispatchers.Main) {
                _dayWorkoutsList.value = workouts
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
        date: String = LocalDate.now().format(formatter)
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sessionId = db.getOrCreateSessionId(date)
            val exId = db.getOrCreateExerciseId(exerciseName)
            
            // For cardio, we just store the first record as a single set for now
            val sets = if (isCardio) listOfNotNull(setRecords.firstOrNull()) else setRecords
            
            db.insertWorkout(sessionId, exId, sets)
            
            refreshToday()
            refreshExercises()
            loadRecentWorkouts()
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
}
