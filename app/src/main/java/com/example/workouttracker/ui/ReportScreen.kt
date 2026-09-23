package com.example.workouttracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.HealthMetric
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReportScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val weekMetrics by viewModel.weekMetrics
    val weekWorkoutCounts by viewModel.weekWorkoutCounts
    val weekCardioCounts by viewModel.weekCardioCounts
    var weekOffset by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(weekOffset) {
        viewModel.loadWeekMetrics(weekOffset)
    }

    val today = LocalDate.now().plusWeeks(weekOffset.toLong())
    val last7Days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
    val fullWeekMetrics = last7Days.map { date ->
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        weekMetrics.find { it.date == dateString } ?: HealthMetric(date = dateString)
    }

    val rangeStart = last7Days.first().format(DateTimeFormatter.ofPattern("d MMM"))
    val rangeEnd = last7Days.last().format(DateTimeFormatter.ofPattern("d MMM"))

    val avgProtein = if (weekMetrics.isNotEmpty()) weekMetrics.map { it.protein }.average().toFloat() else 0f
    val avgWater = if (weekMetrics.isNotEmpty()) weekMetrics.map { it.water }.average().toFloat() else 0f
    val avgWeight = if (weekMetrics.isNotEmpty()) weekMetrics.filter { it.bodyWeight > 0 }.map { it.bodyWeight }.average().toFloat() else 0f
    val totalWorkouts = weekWorkoutCounts.values.sum()
    val totalCardio = weekCardioCounts.values.sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reports",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { weekOffset-- },
                    modifier = Modifier
                        .size(32.dp)
                        .background(CardBackground, RoundedCornerShape(8.dp))
                ) {
                    Text("<", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (weekOffset == 0) "This week" else if (weekOffset == -1) "Last week" else "$rangeStart - $rangeEnd",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (weekOffset != 0 && weekOffset != -1) {
                         // Already showing range
                    } else {
                        Text(
                            text = "$rangeStart - $rangeEnd",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = { if (weekOffset < 0) weekOffset++ },
                    enabled = weekOffset < 0,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (weekOffset < 0) CardBackground else CardBackground.copy(alpha = 0.5f), 
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(">", color = if (weekOffset < 0) Color.White else MutedText, fontWeight = FontWeight.Bold)
                }
            }
        }

        ReportSection(
            title = "WORKOUT SESSIONS",
            avgValue = "$totalWorkouts",
            avgLabel = "Total this week",
            metrics = fullWeekMetrics,
            valueSelector = { weekWorkoutCounts[it.date]?.toFloat() ?: 0f },
            barColor = ChartCyan
        )

        ReportSection(
            title = "CARDIO EXERCISES",
            avgValue = "$totalCardio",
            avgLabel = "Total this week",
            metrics = fullWeekMetrics,
            valueSelector = { weekCardioCounts[it.date]?.toFloat() ?: 0f },
            barColor = AccentBlue
        )

        ReportSection(
            title = "PROTEIN INTAKE",
            avgValue = String.format(Locale.US, "%.0f g", avgProtein),
            avgLabel = "Daily Average",
            metrics = fullWeekMetrics,
            valueSelector = { it.protein },
            barColor = ChartPurple
        )

        ReportSection(
            title = "WATER INTAKE",
            avgValue = String.format(Locale.US, "%.1f L", avgWater),
            avgLabel = "Daily Average",
            metrics = fullWeekMetrics,
            valueSelector = { it.water },
            barColor = AccentPurple
        )

        ReportSection(
            title = "BODY WEIGHT",
            avgValue = String.format(Locale.US, "%.1f kg", avgWeight),
            avgLabel = "Current Average",
            metrics = fullWeekMetrics,
            valueSelector = { it.bodyWeight },
            barColor = AccentPurple
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ReportSection(
    title: String,
    avgValue: String,
    avgLabel: String,
    metrics: List<HealthMetric>,
    valueSelector: (HealthMetric) -> Float,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = avgValue,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = avgLabel,
                color = MutedText,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .padding(16.dp)
        ) {
            if (metrics.isEmpty()) {
                Text(
                    "No data for this week",
                    color = MutedText,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val maxValue = (metrics.maxOfOrNull(valueSelector) ?: 1f).coerceAtLeast(1f)
                
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Show last 7 days (including empty ones if necessary)
                    // For simplicity, we show what we have in the sorted metrics list
                    metrics.takeLast(7).forEach { metric ->
                        val value = valueSelector(metric)
                        val barHeightFactor = if (maxValue > 0) value / maxValue else 0f
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(barHeightFactor.coerceIn(0.05f, 1f))
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(barColor)
                                )
                            }
                            
                            val dateText = try {
                                LocalDate.parse(metric.date).format(DateTimeFormatter.ofPattern("E"))
                            } catch (_: Exception) {
                                ""
                            }
                            Text(
                                text = dateText,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
