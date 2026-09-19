package com.example.workouttracker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.SetRecord
import com.example.workouttracker.data.WorkoutEntry
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayLogScreen(viewModel: AppViewModel, date: String, onBack: () -> Unit, onNavigateEditWorkout: (Long) -> Unit) {
    val workouts by viewModel.dayWorkoutsList
    val dayMetrics by viewModel.dayMetrics
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
    val displayDate = try { LocalDate.parse(date).format(formatter) } catch(e: Exception) { date }

    LaunchedEffect(date) {
        viewModel.loadWorkoutsForDate(date)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .padding(16.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                displayDate, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricSummaryItem("Weight", dayMetrics?.bodyWeight?.let { if (it > 0) "$it kg" else "--" } ?: "--", "⚖", Modifier.weight(1f))
                    MetricSummaryItem("Water", dayMetrics?.water?.let { "$it L" } ?: "0 L", "💧", Modifier.weight(1f))
                    MetricSummaryItem("Protein", dayMetrics?.protein?.let { "${it.toInt()} g" } ?: "0 g", "💊", Modifier.weight(1f))
                }
            }

            if (workouts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs for this day", color = TextSecondary)
                    }
                }
            } else {
                items(workouts) { pair ->
                    WorkoutLogCard(pair.first, pair.second, viewModel, onEdit = { onNavigateEditWorkout(pair.first.id) })
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun MetricSummaryItem(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = CardBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label.uppercase(), color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(icon, fontSize = 12.sp)
            }
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WorkoutLogCard(workout: WorkoutEntry, exName: String, viewModel: AppViewModel, onEdit: () -> Unit) {
    var previousSets by remember { mutableStateOf<List<SetRecord>>(emptyList()) }
    val sets = workout.sets
    
    LaunchedEffect(workout.exerciseId, workout.date) {
        workout.exerciseId?.let {
            previousSets = viewModel.getPreviousRecord(it, workout.date) 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onEdit)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(exName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            
            if (sets.isNotEmpty() && previousSets.isNotEmpty()) {
                val currentAvg = sets.mapNotNull { it.weight }.average()
                val prevAvg = previousSets.mapNotNull { it.weight }.average()

                if (!currentAvg.isNaN() && !prevAvg.isNaN()) {
                    val comparisonText = when {
                        currentAvg > prevAvg -> "Heavier"
                        currentAvg < prevAvg -> "Lighter"
                        else -> "Same"
                    }
                    val comparisonColor = when {
                        currentAvg > prevAvg -> AccentGreen
                        currentAvg < prevAvg -> Color(0xFFD32F2F)
                        else -> TextSecondary
                    }
                    
                    Surface(
                        color = comparisonColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = comparisonText,
                            color = comparisonColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        sets.forEachIndexed { index, record ->
            if (record.weight != null) {
                Text("Set ${index + 1}: ${record.weight} kg • ${record.reps} reps", fontSize = 13.sp, color = TextSecondary)
            } else if (record.duration != null) {
                Text("Duration: ${record.duration} min", fontSize = 13.sp, color = TextSecondary)
            }
        }
    }
}
