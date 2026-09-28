package com.example.workouttracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.WeightEntry
import com.example.workouttracker.ui.theme.*
import java.util.Locale
import kotlin.math.abs

@Composable
fun WeightTrendChart(
    weightEntries: List<WeightEntry>,
    selectedRangeDays: Int,
    onRangeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ranges = listOf(7 to "7D", 30 to "30D", 90 to "90D", 365 to "1Y")

    val latestWeight = weightEntries.lastOrNull()?.weightKg
    val firstWeight = weightEntries.firstOrNull()?.weightKg
    val weightDiff = if (latestWeight != null && firstWeight != null && weightEntries.size > 1) {
        latestWeight - firstWeight
    } else null

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header & Time Range Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WEIGHT TREND",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    if (weightDiff != null) {
                        val sign = if (weightDiff > 0) "↑ " else if (weightDiff < 0) "↓ " else ""
                        val diffText = String.format(Locale.US, "%s%.1f kg in %d days", sign,
                            abs(weightDiff), selectedRangeDays)
                        val diffColor = if (weightDiff < 0) AccentGreen else if (weightDiff > 0) AccentPurple else TextSecondary
                        Text(
                            text = diffText,
                            color = diffColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Range pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ranges.forEach { (days, label) ->
                        val isSelected = selectedRangeDays == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentPurple else InputBackground)
                                .clickable { onRangeSelected(days) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Chart area
            if (weightEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⚖", fontSize = 20.sp)
                        Text(
                            text = "No weight records for this range",
                            color = MutedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (weightEntries.size == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "1 log recorded: %.1f kg", weightEntries.first().weightKg),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                val weights = weightEntries.map { it.weightKg }
                val minWeight = (weights.minOrNull() ?: 0f) - 1f
                val maxWeight = (weights.maxOrNull() ?: 100f) + 1f
                val range = if (maxWeight - minWeight > 0) maxWeight - minWeight else 1f

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (weights.size - 1)

                    val points = weights.mapIndexed { index, weight ->
                        val x = index * spacing
                        val y = height - ((weight - minWeight) / range * height)
                        Offset(x, y)
                    }

                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AccentPurple.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = AccentPurple,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    points.forEach { point ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = AccentPurple,
                            radius = 2.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }
    }
}
