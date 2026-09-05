package com.example.workouttracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.data.DatabaseHelper
import com.example.workouttracker.data.WeightEntry
import com.example.workouttracker.data.WaterEntry
import com.example.workouttracker.data.WorkoutEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseHelper(application)

    private val _todayWeight = mutableStateOf<Float?>(null)
    val todayWeight: State<Float?> = _todayWeight

    private val _todayWater = mutableStateOf(0)
    val todayWater: State<Int> = _todayWater

    private val _todayWorkouts = mutableStateOf(0)
    val todayWorkouts: State<Int> = _todayWorkouts

    private val _exerciseSuggestions = mutableStateOf<List<String>>(emptyList())
    val exerciseSuggestions: State<List<String>> = _exerciseSuggestions

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        refreshToday()
        refreshExercises()
    }

    fun refreshToday() {
        val today = LocalDate.now().format(formatter)
        viewModelScope.launch(Dispatchers.IO) {
            val weights = db.getWeightForDate(today)
            _todayWeight.value = weights.firstOrNull()?.weightKg
            _todayWater.value = db.totalWaterForDate(today)
            val workouts = db.getWorkoutsForDate(today)
            _todayWorkouts.value = workouts.map { it.exerciseName }.distinct().size
        }
    }

    fun refreshExercises() {
        viewModelScope.launch(Dispatchers.IO) {
            _exerciseSuggestions.value = db.getAllExerciseNames()
        }
    }

    fun addWeight(weight: Float, date: String = LocalDate.now().format(formatter)) {
        viewModelScope.launch(Dispatchers.IO) {
            db.insertWeight(WeightEntry(date = date, weightKg = weight))
            refreshToday()
        }
    }

    fun addWater(amountMl: Int = 250, date: String = LocalDate.now().format(formatter)) {
        viewModelScope.launch(Dispatchers.IO) {
            db.insertWater(WaterEntry(date = date, amountMl = amountMl))
            refreshToday()
        }
    }

    fun addWorkout(
        exerciseName: String,
        sets: Int? = null,
        reps: Int? = null,
        weightKg: Float? = null,
        durationMin: Int? = null,
        distanceKm: Float? = null,
        avgSpeed: Float? = null,
        incline: Float? = null,
        date: String = LocalDate.now().format(formatter)
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.insertWorkout(
                WorkoutEntry(
                    date = date,
                    exerciseName = exerciseName,
                    sets = sets,
                    reps = reps,
                    weightKg = weightKg,
                    durationMin = durationMin,
                    distanceKm = distanceKm,
                    avgSpeed = avgSpeed,
                    incline = incline
                )
            )
            refreshToday()
            refreshExercises()
        }
    }
}
