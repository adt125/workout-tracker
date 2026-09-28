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
import androidx.compose.material.icons.filled.Edit
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
import com.example.workouttracker.data.ActivityType
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
    onNavigateEditWorkout: (Long) -> Unit,
    onNavigateGoalSetup: () -> Unit = {}
) {
    val userGoal by viewModel.userGoal
    val todayWeight by viewModel.todayWeight
    val lastKnownWeight by viewModel.lastKnownWeight
    val water by viewModel.todayWater
    val protein by viewModel.todayProtein
    val todayNotes by viewModel.todayNotes
    val recentWorkouts by viewModel.recentWorkouts
    val context = LocalContext.current

    var showWeightDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showProteinDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var isRecentExpanded by remember { mutableStateOf(false) }

    val proteinTarget = userGoal?.dailyProteinTarget ?: 120f
    val waterTarget = userGoal?.dailyWaterTarget ?: 3.0f

    LaunchedEffect(Unit) {
        viewModel.loadRecentWorkouts()
    }

    if (showWeightDialog) {
        LogMetricDialog(
            title = "Log Weight",
            label = "KG",
            initialValue = todayWeight?.toString() ?: "",
            onDismiss = { showWeightDialog = false },
            onConfirm = { value ->
                value.toFloatOrNull()?.let { weight ->
                    viewModel.logWeight(weight)
                }
                showWeightDialog = false
            }
        )
    }

    if (showWaterDialog) {
        AddWaterDialog(
            onDismiss = { showWaterDialog = false },
            onAddPreset = { amount ->
                viewModel.addWaterIncrement(amount)
                showWaterDialog = false
            },
            onAddCustom = { amount ->
                viewModel.addWaterIncrement(amount)
                showWaterDialog = false
            }
        )
    }

    if (showProteinDialog) {
        AddProteinDialog(
            onDismiss = { showProteinDialog = false },
            onAddPreset = { amount ->
                viewModel.addProteinIncrement(amount)
                showProteinDialog = false
            },
            onAddCustom = { amount ->
                viewModel.addProteinIncrement(amount)
                showProteinDialog = false
            }
        )
    }

    if (showNotesDialog) {
        LogNotesDialog(
            title = "Workout Notes",
            initialValue = todayNotes ?: "",
            onDismiss = { showNotesDialog = false },
            onConfirm = { value ->
                viewModel.updateSessionNotes(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), if (value.isBlank()) null else value)
                showNotesDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Header & Share
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

            // User Goal Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .clickable { onNavigateGoalSetup() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "CURRENT GOAL",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = userGoal?.fitnessGoal?.displayName ?: "Set your target",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (userGoal?.targetWeight != null) {
                            Text(
                                text = String.format(Locale.US, "Target weight: %.1f kg", userGoal?.targetWeight),
                                color = AccentPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateGoalSetup,
                        modifier = Modifier
                            .size(36.dp)
                            .background(AppBackground, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Goals", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Weight Stat Card (Tap to log weight)
            StatCard(
                title = "WEIGHT",
                value = todayWeight?.let { String.format(Locale.US, "%.1f kg", it) }
                    ?: lastKnownWeight?.let { String.format(Locale.US, "%.1f kg", it) }
                    ?: "-- kg",
                subValue = if (todayWeight != null) "Logged today (Tap to update)" else "Last recorded (Tap to log)",
                icon = "⚖",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWeightDialog = true }
            )

            // Progress Indicators (Protein & Water)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalProgressCard(
                    title = "PROTEIN",
                    current = protein,
                    target = proteinTarget,
                    unit = "g",
                    icon = "💊",
                    modifier = Modifier.weight(1f)
                )
                GoalProgressCard(
                    title = "WATER",
                    current = water,
                    target = waterTarget,
                    unit = "L",
                    icon = "💧",
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Add Section
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "QUICK ADD",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAddButton(text = "Add Water", icon = "💧", modifier = Modifier.fillMaxWidth()) { showWaterDialog = true }
                    QuickAddButton(text = "Add Protein", icon = "💊", modifier = Modifier.fillMaxWidth()) { showProteinDialog = true }
                    QuickAddButton(text = "Log Workout", icon = "🏋️", modifier = Modifier.fillMaxWidth()) { onNavigateAddWorkout() }
                }
            }

            NotesCard(notes = todayNotes, modifier = Modifier.fillMaxWidth()) {
                showNotesDialog = true
            }

            // Recent Workout Logs Section
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
                                text = "Recent workout logs",
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
                            EmptyStateView(
                                title = "No Recent Workout Logs",
                                subtitle = "Log a workout to track your performance.",
                                icon = "📜"
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

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun GoalProgressCard(
    title: String,
    current: Float,
    target: Float,
    unit: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (current / target).coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()
    val remaining = (target - current).coerceAtLeast(0f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                Text(icon, fontSize = 14.sp)
            }

            Column {
                Text(
                    text = String.format(Locale.US, "%.1f / %.1f %s", current, target, unit),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (remaining > 0) String.format(Locale.US, "%.1f %s remaining", remaining, unit) else "Goal reached!",
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AccentPurple,
                trackColor = InputBackground
            )

            Text(
                text = "$percentage%",
                color = AccentPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddWaterDialog(
    onDismiss: () -> Unit,
    onAddPreset: (Float) -> Unit,
    onAddCustom: (Float) -> Unit
) {
    var customValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardBackground)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Add Water",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAddPreset(0.25f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InputBackground, contentColor = Color.White)
                    ) {
                        Text("+250ml", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = { onAddPreset(0.5f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InputBackground, contentColor = Color.White)
                    ) {
                        Text("+500ml", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = { onAddPreset(1.0f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InputBackground, contentColor = Color.White)
                    ) {
                        Text("+1L", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CUSTOM LITRES",
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
                            value = customValue,
                            onValueChange = { customValue = it },
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
                    onClick = {
                        customValue.toFloatOrNull()?.let { onAddCustom(it) }
                    },
                    enabled = customValue.isNotBlank(),
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
fun AddProteinDialog(
    onDismiss: () -> Unit,
    onAddPreset: (Float) -> Unit,
    onAddCustom: (Float) -> Unit
) {
    var customValue by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardBackground)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Add Protein",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAddPreset(20f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InputBackground, contentColor = Color.White)
                    ) {
                        Text("+20g", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = { onAddPreset(25f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InputBackground, contentColor = Color.White)
                    ) {
                        Text("+25g", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = { onAddPreset(30f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InputBackground, contentColor = Color.White)
                    ) {
                        Text("+30g", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CUSTOM GRAMS",
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
                            value = customValue,
                            onValueChange = { customValue = it },
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
                    onClick = {
                        customValue.toFloatOrNull()?.let { onAddCustom(it) }
                    },
                    enabled = customValue.isNotBlank(),
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
            .height(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
