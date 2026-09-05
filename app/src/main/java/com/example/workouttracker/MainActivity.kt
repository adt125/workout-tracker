package com.example.workouttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.workouttracker.ui.AddWorkoutScreen
import com.example.workouttracker.ui.CompareScreen
import com.example.workouttracker.ui.DashboardScreen
import com.example.workouttracker.ui.theme.WorkoutTrackerTheme
import com.example.workouttracker.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                    AppNav(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNav(viewModel: AppViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(viewModel, onNavigateAddWorkout = { navController.navigate("add_workout") }, onNavigateCompare = { navController.navigate("compare") })
        }
        composable("add_workout") {
            AddWorkoutScreen(viewModel, onDone = { navController.popBackStack() })
        }
        composable("compare") {
            CompareScreen(viewModel, onBack = { navController.popBackStack() })
        }
    }
}
