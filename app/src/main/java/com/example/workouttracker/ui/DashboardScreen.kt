package com.example.workouttracker.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
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
fun DashboardScreen(
    viewModel: AppViewModel, 
    onNavigateAddWorkout: () -> Unit, 
    onNavigateReport: () -> Unit, 
    onNavigateThisWeek: () -> Unit, 
    onNavigateHistory: () -> Unit,
    onNavigateEditWorkout: (Long) -> Unit
) {
    val todayWeight by viewModel.todayWeight
    val lastKnownWeight by viewModel.lastKnownWeight
    val water by viewModel.todayWater
    val protein by viewModel.todayProtein
    val recentWorkouts by viewModel.recentWorkouts
    val context = LocalContext.current

    var showWeightDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showProteinDialog by remember { mutableStateOf(false) }
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

    if (showProteinDialog) {
        LogMetricDialog(
            title = "Add Protein",
            label = "GRAMS",
            initialValue = "",
            onDismiss = { showProteinDialog = false },
            onConfirm = { value ->
                if (value.isNotEmpty()) {
                    viewModel.updateDailyMetrics(protein = (protein + (value.toFloatOrNull() ?: 0f)))
                }
                showProteinDialog = false
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
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

                IconButton(
                    onClick = {
                        val json = viewModel.getBackupDataJson()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Workout Tracker Backup")
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(intent, "Backup Workout Data"))
                    },
                    modifier = Modifier.background(CardBackground, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Backup Data", tint = Color.White)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "WEIGHT",
                    value = todayWeight?.let { String.format(Locale.US, "%.1f kg", it) } 
                        ?: lastKnownWeight?.let { String.format(Locale.US, "%.1f kg", it) } 
                        ?: "-- kg",
                    subValue = if (todayWeight != null) "Logged today" else "Last recorded",
                    icon = "⚖",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "WATER",
                    value = String.format(Locale.US, "%.1f L", water),
                    subValue = "Daily intake",
                    icon = "💧",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "PROTEIN",
                    value = String.format(Locale.US, "%.0f g", protein),
                    subValue = "Daily intake",
                    icon = "💊",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "AI SUMMARY",
                    value = "✨",
                    subValue = "Coming soon",
                    icon = "🤖",
                    modifier = Modifier.weight(1f)
                )
            }

            // Recent Logs Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val rotationState by animateFloatAsState(
                    targetValue = if (isRecentExpanded) 180f else 0f,
                    label = "rotation"
                )

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
                            text = "▼",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isRecentExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
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
                                RecentLogItem(
                                    name = workoutPair.second, 
                                    date = workoutPair.first.date,
                                    onClick = { onNavigateEditWorkout(workoutPair.first.id) }
                                )
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAddButton(text = "Weight", icon = "⚖", modifier = Modifier.weight(1f)) { showWeightDialog = true }
                    QuickAddButton(text = "Water", icon = "💧", modifier = Modifier.weight(1f)) { showWaterDialog = true }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAddButton(text = "Protein", icon = "💊", modifier = Modifier.weight(1f)) { showProteinDialog = true }
                    QuickAddButton(text = "Workout", icon = "🏋️", modifier = Modifier.weight(1f)) { onNavigateAddWorkout() }
                }
            }
            
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun RecentLogItem(name: String, date: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                            .clickable { focusRequester.requestFocus() }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        BasicTextField(
                            value = value,
                            onValueChange = { value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
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
fun QuickAddButton(text: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
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
