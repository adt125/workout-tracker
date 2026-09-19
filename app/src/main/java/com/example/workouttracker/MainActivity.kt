package com.example.workouttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workouttracker.ui.*
import com.example.workouttracker.ui.theme.WorkoutTrackerTheme
import com.example.workouttracker.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutTrackerTheme {
                AppNav(viewModel)
            }
        }
    }
}

@Composable
fun AppNav(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("dashboard", "this_week", "history", "report")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute ?: "dashboard",
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateAddWorkout = { navController.navigate("add_workout") },
                        onNavigateReport = { navController.navigate("report") },
                        onNavigateThisWeek = { navController.navigate("this_week") },
                        onNavigateHistory = { navController.navigate("history") },
                        onNavigateEditWorkout = { entryId -> navController.navigate("edit_workout/$entryId") }
                    )
                }
                composable("add_workout") {
                    AddWorkoutScreen(viewModel, onDone = { navController.popBackStack() })
                }
                composable(
                    route = "edit_workout/{entryId}",
                    arguments = listOf(navArgument("entryId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getLong("entryId")
                    AddWorkoutScreen(viewModel, entryId = entryId, onDone = { navController.popBackStack() })
                }
                composable("report") {
                    ReportScreen(viewModel, onBack = { navController.popBackStack() })
                }
                composable("this_week") {
                    ThisWeekScreen(viewModel, onNavigateDayLog = { date -> navController.navigate("day_log/$date") }, onBack = { navController.popBackStack() })
                }
                composable("history") {
                    HistoryScreen(viewModel, onNavigateDayLog = { date -> navController.navigate("day_log/$date") }, onBack = { navController.popBackStack() })
                }
                composable(
                    route = "day_log/{date}",
                    arguments = listOf(navArgument("date") { type = NavType.StringType })
                ) { backStackEntry ->
                    val date = backStackEntry.arguments?.getString("date") ?: ""
                    DayLogScreen(
                        viewModel = viewModel,
                        date = date,
                        onBack = { navController.popBackStack() },
                        onNavigateEditWorkout = { entryId -> navController.navigate("edit_workout/$entryId") }
                    )
                }
            }
        }
    }
}
