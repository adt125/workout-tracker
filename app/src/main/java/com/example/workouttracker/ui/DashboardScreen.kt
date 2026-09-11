package com.example.workouttracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: AppViewModel, onNavigateAddWorkout: () -> Unit, onNavigateCompare: () -> Unit, onNavigateThisWeek: () -> Unit, onNavigateHistory: () -> Unit) {
    val todayWeight by viewModel.todayWeight
    val water by viewModel.todayWater
    val recentWorkouts by viewModel.recentWorkouts

    var showWeightDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var isRecentExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRecentWorkouts()
    }

    if (showWeightDialog) {
        LogMetricDialog(
            title = "Add Weight",
            label = "KG",
            initialValue = todayWeight?.toString() ?: "",
            onDismiss = { showWeightDialog = false },
            onConfirm = { value ->
                if (value.isNotEmpty()) {
                    viewModel.updateDailyMetrics(weight = value.toFloatOrNull())
                }
                showWeightDialog = false
            }
        )
    }

    if (showWaterDialog) {
        LogMetricDialog(
            title = "Add Water",
            label = "LITRES",
            initialValue = "",
            onDismiss = { showWaterDialog = false },
            onConfirm = { value ->
                if (value.isNotEmpty()) {
                    viewModel.updateDailyMetrics(water = (water + (value.toFloatOrNull() ?: 0f)))
                }
                showWaterDialog = false
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TODAY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM"))
                Text(
                    text = today,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "WEIGHT",
                    value = todayWeight?.let { String.format(Locale.US, "%.1f kg", it) } ?: "72.4 kg",
                    subValue = "-0.4 kg vs last week",
                    icon = "⚖",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "WATER",
                    value = String.format(Locale.US, "%.1f L", water),
                    subValue = "Goal: 3.0 L",
                    icon = "💧",
                    modifier = Modifier.weight(1f)
                )
            }

            // Recent Logs Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .clickable { isRecentExpanded = !isRecentExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📜", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Recent logs",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = if (isRecentExpanded) "▲" else "▼",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isRecentExpanded) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (recentWorkouts.isEmpty()) {
                            Text(
                                "No logs yet",
                                color = MutedText,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            recentWorkouts.take(6).forEach { workoutPair ->
                                RecentLogItem(workoutPair.second, workoutPair.first.date)
                            }
                            
                            TextButton(
                                onClick = onNavigateHistory,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    "View more",
                                    color = AccentPurple,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "QUICK ADD",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                QuickAddButton(text = "Weight", icon = "⚖") { showWeightDialog = true }
                QuickAddButton(text = "Water", icon = "💧") { showWaterDialog = true }
                QuickAddButton(text = "Workout", icon = "🏋️") { onNavigateAddWorkout() }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun RecentLogItem(name: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(date, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun LogMetricDialog(
    title: String,
    label: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardBackground)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = label,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(InputBackground)
                            .border(1.dp, MutedText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        BasicTextField(
                            value = value,
                            onValueChange = { value = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            cursorBrush = SolidColor(Color.White)
                        )
                    }
                }

                Button(
                    onClick = { onConfirm(value) },
                    enabled = value.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White.copy(alpha = 0.3f),
                        disabledContentColor = Color.Black.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Add",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subValue: String, icon: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(title, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                Text(icon, fontSize = 14.sp)
            }
            Column {
                Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(subValue, color = MutedText, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun QuickAddButton(text: String, icon: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
