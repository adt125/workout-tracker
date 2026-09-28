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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.DaySummary
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EmptyStateView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: String = "🏋️"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MutedText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisWeekScreen(viewModel: AppViewModel, onNavigateDayLog: (String) -> Unit, onBack: () -> Unit) {
    val summaries by viewModel.weekSummaries
    
    LaunchedEffect(Unit) {
        viewModel.loadWeekWorkouts()
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
            Text(
                "This Week", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (summaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = "No Activity Recorded",
                    subtitle = "Workouts, water, or weight logged this week will appear here.",
                    icon = "📅"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(summaries, key = { it.date }) { summary ->
                    WeekDayCard(summary) {
                        onNavigateDayLog(summary.date)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun WeekDayCard(summary: DaySummary, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMM")
    val displayDate = try { LocalDate.parse(summary.date).format(formatter) } catch(e: Exception) { summary.date }
    
    val workoutCount = summary.workouts.filter { it.first.exerciseId != null }.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(displayDate, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            
            if (workoutCount > 0) {
                Text("$workoutCount exercises logged", color = Color.White, fontSize = 13.sp)
                val exercises = summary.workouts.map { it.second }.filter { it.isNotEmpty() }.distinct().joinToString(", ")
                if (exercises.isNotEmpty()) {
                    Text(exercises, color = MutedText, fontSize = 12.sp, maxLines = 1)
                }
            } else {
                Text("No workouts logged", color = TextSecondary, fontSize = 13.sp)
            }

            val metrics = summary.metrics
            val hasMetrics = metrics != null && (metrics.bodyWeight > 0f || metrics.water > 0f || metrics.protein > 0f)
            if (hasMetrics) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (metrics!!.bodyWeight > 0f) {
                        Text("⚖ ${metrics.bodyWeight} kg", color = TextSecondary, fontSize = 12.sp)
                    }
                    if (metrics.water > 0f) {
                        Text("💧 ${metrics.water} L", color = TextSecondary, fontSize = 12.sp)
                    }
                    if (metrics.protein > 0f) {
                        Text("💊 ${metrics.protein.toInt()} g", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            if (!summary.notes.isNullOrBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("📝", fontSize = 12.sp)
                    Text(
                        text = summary.notes,
                        color = AccentPurple,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
