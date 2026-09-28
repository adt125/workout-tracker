package com.example.workouttracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.HealthMetric
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class ChartType {
    BAR,
    LINE,
    CHECKMARK
}

@Composable
fun ReportScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val weekMetrics by viewModel.weekMetrics
    val weekWorkoutCounts by viewModel.weekWorkoutCounts
    val weekCardioCounts by viewModel.weekCardioCounts

    var weekOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(weekOffset) {
        viewModel.loadWeekMetrics(weekOffset)
    }

    val targetDate = LocalDate.now().plusWeeks(weekOffset.toLong())

    // Metrics use rolling 7 days (baki k liye thik hai)
    val last7Days = (0..6).map { targetDate.minusDays(it.toLong()) }.reversed()
    val fullWeekMetrics = last7Days.map { date ->
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        weekMetrics.find { it.date == dateString } ?: HealthMetric(date = dateString)
    }

    // Cardio and exercise use Monday to Sunday week
    val mondayOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sundayOfWeek = mondayOfWeek.plusDays(6)
    val mondayToSundayDays = (0..6).map { mondayOfWeek.plusDays(it.toLong()) }
    val mondayToSundayMetrics = mondayToSundayDays.map { date ->
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        HealthMetric(date = dateString)
    }

    val rangeStart = mondayOfWeek.format(DateTimeFormatter.ofPattern("d MMM"))
    val rangeEnd = sundayOfWeek.format(DateTimeFormatter.ofPattern("d MMM"))

    val avgProtein = if (weekMetrics.isNotEmpty()) weekMetrics.map { it.protein }.average().toFloat() else 0f
    val avgWater = if (weekMetrics.isNotEmpty()) weekMetrics.map { it.water }.average().toFloat() else 0f
    val validWeights = weekMetrics.filter { it.bodyWeight > 0 }.map { it.bodyWeight }
    val avgWeight = if (validWeights.isNotEmpty()) validWeights.average().toFloat() else 0f

    val workoutDaysCompleted = mondayToSundayDays.count { (weekWorkoutCounts[it.format(DateTimeFormatter.ISO_LOCAL_DATE)] ?: 0) > 0 }
    val cardioDaysCompleted = mondayToSundayDays.count { (weekCardioCounts[it.format(DateTimeFormatter.ISO_LOCAL_DATE)] ?: 0) > 0 }

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
            avgValue = "$workoutDaysCompleted / 7 days",
            avgLabel = "Completed this week",
            metrics = mondayToSundayMetrics,
            valueSelector = { weekWorkoutCounts[it.date]?.toFloat() ?: 0f },
            barColor = ChartCyan,
            chartType = ChartType.CHECKMARK
        )

        ReportSection(
            title = "CARDIO EXERCISES",
            avgValue = "$cardioDaysCompleted / 7 days",
            avgLabel = "Completed this week",
            metrics = mondayToSundayMetrics,
            valueSelector = { weekCardioCounts[it.date]?.toFloat() ?: 0f },
            barColor = AccentBlue,
            chartType = ChartType.CHECKMARK
        )

        ReportSection(
            title = "PROTEIN INTAKE",
            avgValue = String.format(Locale.US, "%.0f g", avgProtein),
            avgLabel = "Daily Average",
            metrics = fullWeekMetrics,
            valueSelector = { it.protein },
            barColor = ChartPurple,
            chartType = ChartType.BAR
        )

        ReportSection(
            title = "WATER INTAKE",
            avgValue = String.format(Locale.US, "%.1f L", avgWater),
            avgLabel = "Daily Average",
            metrics = fullWeekMetrics,
            valueSelector = { it.water },
            barColor = AccentPurple,
            chartType = ChartType.BAR
        )

        ReportSection(
            title = "BODY WEIGHT",
            avgValue = if (avgWeight > 0) String.format(Locale.US, "%.1f kg", avgWeight) else "-- kg",
            avgLabel = "Current Average",
            metrics = fullWeekMetrics,
            valueSelector = { it.bodyWeight },
            barColor = AccentPurple,
            chartType = ChartType.LINE
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

fun formatMetricValue(value: Float, title: String): String {
    if (value <= 0f) return ""
    return when {
        title.contains("PROTEIN", ignoreCase = true) -> "${value.toInt()}g"
        title.contains("WATER", ignoreCase = true) -> String.format(Locale.US, "%.1fL", value)
        title.contains("WEIGHT", ignoreCase = true) -> String.format(Locale.US, "%.1f", value)
        else -> if (value % 1f == 0f) "${value.toInt()}" else String.format(Locale.US, "%.1f", value)
    }
}

@Composable
fun ReportSection(
    title: String,
    avgValue: String,
    avgLabel: String,
    metrics: List<HealthMetric>,
    valueSelector: (HealthMetric) -> Float,
    barColor: Color,
    chartType: ChartType = ChartType.BAR
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
                .height(if (chartType == ChartType.CHECKMARK) 130.dp else 170.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .padding(14.dp)
        ) {
            val weekItems = metrics.takeLast(7)
            val nonZeroValues = weekItems.map(valueSelector).filter { it > 0 }

            if (weekItems.isEmpty()) {
                Text(
                    "No data for this week",
                    color = MutedText,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (chartType == ChartType.CHECKMARK) {
                // Checkmark Consistency Row for Workouts and Cardio
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekItems.forEach { metric ->
                        val value = valueSelector(metric)
                        val isCompleted = value > 0

                        val dateText = try {
                            LocalDate.parse(metric.date).format(DateTimeFormatter.ofPattern("E"))
                        } catch (_: Exception) {
                            ""
                        }
                        val dayNum = try {
                            LocalDate.parse(metric.date).format(DateTimeFormatter.ofPattern("d"))
                        } catch (_: Exception) {
                            ""
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompleted) barColor else InputBackground)
                                    .border(
                                        1.dp,
                                        if (isCompleted) Color.Transparent else MutedText.copy(alpha = 0.3f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MutedText.copy(alpha = 0.4f))
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dateText,
                                    color = if (isCompleted) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = dayNum,
                                    color = MutedText,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            } else if (chartType == ChartType.LINE) {
                if (nonZeroValues.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⚖", fontSize = 20.sp)
                        Text(
                            "No weight logged this week",
                            color = MutedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Line chart layout with Y-axis scale on left and weight labels above points
                    val minW = (nonZeroValues.minOrNull() ?: 0f) - 0.5f
                    val maxW = (nonZeroValues.maxOrNull() ?: 100f) + 0.5f

                    Row(modifier = Modifier.fillMaxSize()) {
                        // Y-Axis scale column
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(bottom = 18.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", maxW),
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f", minW),
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height

                                    val valRange = if (maxW > minW) maxW - minW else 1f
                                    val colWidth = width / 7f

                                    val points = mutableListOf<Pair<Int, Offset>>()

                                    weekItems.forEachIndexed { index, metric ->
                                        val valFloat = valueSelector(metric)
                                        if (valFloat > 0) {
                                            val x = (index * colWidth) + (colWidth / 2f)
                                            val y = height - (((valFloat - minW) / valRange) * (height - 24.dp.toPx())) - 12.dp.toPx()
                                            points.add(index to Offset(x, y))
                                        }
                                    }

                                    if (points.size > 1) {
                                        val linePath = Path().apply {
                                            moveTo(points.first().second.x, points.first().second.y)
                                            for (i in 1 until points.size) {
                                                lineTo(points[i].second.x, points[i].second.y)
                                            }
                                        }
                                        drawPath(
                                            path = linePath,
                                            color = barColor,
                                            style = Stroke(width = 2.5.dp.toPx())
                                        )
                                    }

                                    points.forEach { (_, point) ->
                                        drawCircle(
                                            color = Color.White,
                                            radius = 5.dp.toPx(),
                                            center = point
                                        )
                                        drawCircle(
                                            color = barColor,
                                            radius = 3.dp.toPx(),
                                            center = point
                                        )
                                    }
                                }
                            }

                            // Day labels and daily values below line
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weekItems.forEach { metric ->
                                    val valFloat = valueSelector(metric)
                                    val dateText = try {
                                        LocalDate.parse(metric.date).format(DateTimeFormatter.ofPattern("E"))
                                    } catch (_: Exception) {
                                        ""
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (valFloat > 0) String.format(Locale.US, "%.1f", valFloat) else "-",
                                            color = if (valFloat > 0) Color.White else MutedText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
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
            } else {
                // Bar Chart layout with Y-axis scale on left and daily value labels above bars
                val maxVal = (weekItems.maxOfOrNull(valueSelector) ?: 1f).coerceAtLeast(1f)

                Row(modifier = Modifier.fillMaxSize()) {
                    // Y-Axis scale column
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 18.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = formatMetricValue(maxVal, title),
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "0",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weekItems.forEach { metric ->
                            val value = valueSelector(metric)
                            val barHeightFactor = if (maxVal > 0) value / maxVal else 0f
                            val valueText = formatMetricValue(value, title)

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = valueText,
                                    color = if (value > 0) Color.White else Color.Transparent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(barHeightFactor.coerceIn(if (value > 0) 0.08f else 0.02f, 1f))
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (value > 0) barColor else InputBackground)
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
}
