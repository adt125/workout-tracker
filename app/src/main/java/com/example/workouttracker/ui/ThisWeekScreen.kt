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
import com.example.workouttracker.data.WorkoutEntry
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisWeekScreen(viewModel: AppViewModel, onNavigateDayLog: (String) -> Unit, onBack: () -> Unit) {
    val workouts by viewModel.weekWorkouts
    
    LaunchedEffect(Unit) {
        viewModel.loadWeekWorkouts()
    }

    val groupedWorkouts = workouts.groupBy { it.first.date }
    val days = groupedWorkouts.keys.sortedDescending()

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
            Text(
                "This Week", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (days.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No workouts this week yet", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(days) { date ->
                    val dayWorkouts = groupedWorkouts[date] ?: emptyList()
                    WeekDayCard(date, dayWorkouts) {
                        onNavigateDayLog(date)
                    }
                }
            }
        }
    }
}

@Composable
fun WeekDayCard(date: String, workouts: List<Pair<WorkoutEntry, String>>, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMM")
    val displayDate = try { LocalDate.parse(date).format(formatter) } catch(e: Exception) { date }
    
    val workoutCount = workouts.filter { it.first.exerciseId != null }.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(displayDate, fontWeight = FontWeight.Bold, color = Color.White)
            Text("$workoutCount exercises logged", color = TextSecondary, fontSize = 13.sp)
            
            val exercises = workouts.map { it.second }.filter { it.isNotEmpty() }.distinct().joinToString(", ")
            if (exercises.isNotEmpty()) {
                Text(exercises, color = MutedText, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}
