package com.example.workouttracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(viewModel: AppViewModel, onNavigateAddWorkout: () -> Unit, onNavigateCompare: () -> Unit) {
    val todayWeight by viewModel.todayWeight
    val water by viewModel.todayWater
    val workouts by viewModel.todayWorkouts

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(FigmaSurface)
                .padding(20.dp)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Today",
                color = FigmaTextSecondary,
                fontSize = 13.sp
            )
            
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM"))
            Text(
                text = today,
                color = FigmaTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Weight",
                    value = if (todayWeight != null) String.format("%.1f kg", todayWeight) else "—",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Water",
                    value = String.format("%.1f L", water / 1000.0),
                    modifier = Modifier.weight(1f)
                )
            }

            SuccessBanner(message = "Leg day logged, $workouts exercises")

            Text(
                text = "Quick add",
                color = FigmaTextSecondary,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddButton(text = "Weight", modifier = Modifier.weight(1f)) { /* TODO */ }
                QuickAddButton(text = "Water", modifier = Modifier.weight(1f)) { viewModel.addWater(250) }
                QuickAddButton(text = "Workout", modifier = Modifier.weight(1f)) { onNavigateAddWorkout() }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onNavigateCompare,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Compare weeks", color = FigmaAccent)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(FigmaBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = FigmaMutedText, fontSize = 11.sp)
            Text(value, color = FigmaTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SuccessBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FigmaBannerGreen)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("✓", color = FigmaAccentGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(message, color = FigmaAccentGreen, fontSize = 12.sp)
    }
}

@Composable
fun QuickAddButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FigmaBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = FigmaTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
