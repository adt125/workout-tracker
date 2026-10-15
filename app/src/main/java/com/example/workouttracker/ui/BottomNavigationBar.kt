package com.example.workouttracker.ui

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.workouttracker.ui.components.FitBottomNav

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FitBottomNav(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        modifier = modifier
            .navigationBarsPadding()
    )
}
