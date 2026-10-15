package com.example.workouttracker.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.workouttracker.data.WeightEntry
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.ui.components.FitCard
import com.example.workouttracker.ui.components.FitProgressBar
import com.example.workouttracker.ui.components.LabelText
import com.example.workouttracker.ui.components.PrimaryButton
import com.example.workouttracker.ui.components.StatTile
import com.example.workouttracker.ui.components.UndoSnackbarHost
import com.example.workouttracker.ui.theme.BigNumberStyle
import com.example.workouttracker.ui.theme.Ground
import com.example.workouttracker.ui.theme.MintAccent
import com.example.workouttracker.ui.theme.PrimaryButtonBrush
import com.example.workouttracker.ui.theme.ProgressBrush
import com.example.workouttracker.ui.theme.Raised
import com.example.workouttracker.ui.theme.ScreenDateStyle
import com.example.workouttracker.ui.theme.SecondaryTextStyle
import com.example.workouttracker.ui.theme.TextHi
import com.example.workouttracker.ui.theme.TextLo
import com.example.workouttracker.util.formatWeight
import com.example.workouttracker.util.goalProgress
import com.example.workouttracker.util.relativeDay
import com.example.workouttracker.util.weightChange7d
import com.example.workouttracker.viewmodel.AppViewModel
import kotlinx.coroutines.launch
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
    val weightHistory by viewModel.weightHistory
    val water by viewModel.todayWater
    val protein by viewModel.todayProtein
    val todayNotes by viewModel.todayNotes
    val recentWorkouts by viewModel.recentWorkouts
    val todayWorkouts by viewModel.todayWorkouts
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showWeightDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showProteinDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var isRecentExpanded by remember { mutableStateOf(false) }

    val proteinTarget = userGoal?.dailyProteinTarget ?: 120f
    val waterTarget = userGoal?.dailyWaterTarget ?: 3.0f
    val todayDate = LocalDate.now()

    LaunchedEffect(Unit) {
        viewModel.loadRecentWorkouts()
        viewModel.loadWeightHistory(30)
    }

    fun triggerUndoSnackbar(message: String, logId: Long) {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoActivityLog(logId)
            }
        }
    }

    if (showWeightDialog) {
        LogWeightDialog(
            initialValue = todayWeight?.let { formatWeight(it.toDouble()) } ?: "",
            onDismiss = { showWeightDialog = false },
            onConfirm = { valueStr: String ->
                valueStr.toFloatOrNull()?.let { weight ->
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
                viewModel.addWaterIncrement(amount) { logId ->
                    val label = if (amount >= 1.0f) String.format(Locale.US, "+%.1f L water logged", amount)
                    else String.format(Locale.US, "+%.0f ml water logged", amount * 1000)
                    triggerUndoSnackbar(label, logId)
                }
                showWaterDialog = false
            },
            onAddCustom = { amount ->
                viewModel.addWaterIncrement(amount) { logId ->
                    val label = if (amount >= 1.0f) String.format(Locale.US, "+%.1f L water logged", amount)
                    else String.format(Locale.US, "+%.0f ml water logged", amount * 1000)
                    triggerUndoSnackbar(label, logId)
                }
                showWaterDialog = false
            }
        )
    }

    if (showProteinDialog) {
        AddProteinDialog(
            onDismiss = { showProteinDialog = false },
            onAddPreset = { amount ->
                viewModel.addProteinIncrement(amount) { logId ->
                    val label = String.format(Locale.US, "+%.0f g protein logged", amount)
                    triggerUndoSnackbar(label, logId)
                }
                showProteinDialog = false
            },
            onAddCustom = { amount ->
                viewModel.addProteinIncrement(amount) { logId ->
                    val label = String.format(Locale.US, "+%.0f g protein logged", amount)
                    triggerUndoSnackbar(label, logId)
                }
                showProteinDialog = false
            }
        )
    }

    if (showNotesDialog) {
        DashboardNotesDialog(
            title = "Workout Notes",
            initialValue = todayNotes ?: "",
            onDismiss = { showNotesDialog = false },
            onConfirm = { value: String ->
                viewModel.updateSessionNotes(
                    todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    if (value.isBlank()) null else value
                )
                showNotesDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fixed Top Header & Share
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        LabelText(text = "TODAY")
                        val dateFormatted = todayDate.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
                        Text(
                            text = dateFormatted,
                            style = ScreenDateStyle
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
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = TextLo
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

            // Goal Strip
            val goal = userGoal
            val startW = goal?.currentWeight?.toDouble()
            val targetW = goal?.targetWeight?.toDouble()
            val currentW = todayWeight?.toDouble() ?: lastKnownWeight?.toDouble()

            FitCard(
                onClick = onNavigateGoalSetup,
                padding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal?.fitnessGoal?.displayName ?: "General fitness",
                                color = TextHi,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentW != null && targetW != null) {
                                Text(
                                    text = "${formatWeight(currentW)} → ${formatWeight(targetW)} kg",
                                    color = TextLo,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (startW != null && currentW != null && targetW != null && startW != targetW) {
                            val ratio = goalProgress(startW, currentW, targetW)
                            if (ratio != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                FitProgressBar(
                                    progress = ratio,
                                    height = 6.dp,
                                    progressBrush = ProgressBrush
                                )

                                val toGo = Math.abs(targetW - currentW)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (toGo < 0.05) "Goal reached" else "${formatWeight(toGo)} kg to go",
                                    color = TextLo,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onNavigateGoalSetup,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit goal",
                            tint = TextLo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Weight Card
            val latestWeightValue = todayWeight ?: lastKnownWeight
            FitCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelText(text = "WEIGHT")
                    IconButton(
                        onClick = { showWeightDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Log weight",
                            tint = TextLo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = latestWeightValue?.let { formatWeight(it.toDouble()) } ?: "--",
                                style = BigNumberStyle
                            )
                            Text(
                                text = " kg",
                                style = SecondaryTextStyle,
                                color = TextLo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Relative day & 7-day change
                        val latestEntryDate = if (todayWeight != null) todayDate else weightHistory.lastOrNull()?.let {
                            try { LocalDate.parse(it.date) } catch (_: Exception) { null }
                        } ?: todayDate

                        val relativeText = relativeDay(latestEntryDate, todayDate)
                        val change7d = weightChange7d(weightHistory, todayDate)

                        val annotatedWeightText = buildAnnotatedString {
                            append("Logged $relativeText")
                            if (change7d != null && change7d != 0.0) {
                                val sign = if (change7d >= 0.0) "+" else ""
                                val changeColor = if (change7d < 0.0) Color(0xFFEF5350) else Color(0xFF66BB6A)
                                append(" · ")
                                withStyle(SpanStyle(color = changeColor)) {
                                    append("$sign${formatWeight(change7d)} kg this week")
                                }
                            }
                        }

                        Text(
                            text = annotatedWeightText,
                            style = SecondaryTextStyle,
                            color = TextLo,
                            fontSize = 12.sp
                        )
                    }

                    // Sparkline
                    if (weightHistory.size >= 2) {
                        WeightSparkline(
                            entries = weightHistory,
                            modifier = Modifier
                                .width(96.dp)
                                .height(44.dp)
                        )
                    }
                }
            }

            // Today's progress section title
            Text(
                text = "Today's progress",
                color = TextHi,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Workout Card inside Today's progress
            FitCard(
                onClick = onNavigateAddWorkout,
                padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Raised),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FitnessCenter,
                                contentDescription = "Workout",
                                tint = MintAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            LabelText(text = "WORKOUT")
                            Text(
                                text = if (todayWorkouts > 0) "$todayWorkouts exercise${if (todayWorkouts > 1) "s" else ""} logged today" else "No workout yet",
                                color = TextHi,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val lastWorkout = recentWorkouts.firstOrNull()
                            if (lastWorkout != null) {
                                Text(
                                    text = "Last: ${lastWorkout.first.date}",
                                    color = TextLo,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Compact Log button
                    Button(
                        onClick = onNavigateAddWorkout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryButtonBrush)
                    ) {
                        Text(
                            text = "Log",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Today's Notes Row (full width)
            FitCard(
                onClick = { showNotesDialog = true },
                padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = "Notes",
                        tint = TextLo,
                        modifier = Modifier.size(20.dp)
                    )
                    val noteText = todayNotes?.lines()?.firstOrNull { it.isNotBlank() } ?: "Add a note for today..."
                    Text(
                        text = noteText,
                        color = if (todayNotes.isNull_or_blank()) TextLo else TextHi,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Protein & Water Nutrition Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Protein Tile
                val proteinRem = (proteinTarget - protein).coerceAtLeast(0f)
                val proteinProgress = if (proteinTarget > 0) (protein / proteinTarget).coerceIn(0f, 1f) else 0f
                StatTile(
                    label = "PROTEIN",
                    icon = Icons.Rounded.Restaurant,
                    currentValueText = String.format(Locale.US, "%.0f", protein),
                    goalValueText = String.format(Locale.US, "%.0f g", proteinTarget),
                    progress = proteinProgress,
                    remainingText = if (proteinRem > 0) String.format(Locale.US, "%.0f g left", proteinRem) else "Goal reached",
                    chip1Label = "+20g",
                    onChip1Click = {
                        viewModel.addProteinIncrement(20f) { logId ->
                            triggerUndoSnackbar("+20 g protein logged", logId)
                        }
                    },
                    chip2Label = "+25g",
                    onChip2Click = {
                        viewModel.addProteinIncrement(25f) { logId ->
                            triggerUndoSnackbar("+25 g protein logged", logId)
                        }
                    },
                    onTileClick = { showProteinDialog = true },
                    modifier = Modifier.weight(1f)
                )

                // Water Tile
                val waterRem = (waterTarget - water).coerceAtLeast(0f)
                val waterProgress = if (waterTarget > 0) (water / waterTarget).coerceIn(0f, 1f) else 0f
                StatTile(
                    label = "WATER",
                    icon = Icons.Rounded.WaterDrop,
                    currentValueText = String.format(Locale.US, "%.1f", water),
                    goalValueText = String.format(Locale.US, "%.1f L", waterTarget),
                    progress = waterProgress,
                    remainingText = if (waterRem > 0) String.format(Locale.US, "%.1f L left", waterRem) else "Goal reached",
                    chip1Label = "+250ml",
                    onChip1Click = {
                        viewModel.addWaterIncrement(0.25f) { logId ->
                            triggerUndoSnackbar("+250 ml water logged", logId)
                        }
                    },
                    chip2Label = "+500ml",
                    onChip2Click = {
                        viewModel.addWaterIncrement(0.5f) { logId ->
                            triggerUndoSnackbar("+500 ml water logged", logId)
                        }
                    },
                    onTileClick = { showWaterDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            // Recent Workout Logs Section
            FitCard(
                onClick = { isRecentExpanded = !isRecentExpanded },
                padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                val rotationState by animateFloatAsState(
                    targetValue = if (isRecentExpanded) 180f else 0f,
                    label = "rotation"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent workout logs",
                        color = TextHi,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "▼",
                        color = TextLo,
                        fontSize = 12.sp,
                        modifier = Modifier.rotate(rotationState)
                    )
                }

                AnimatedVisibility(
                    visible = isRecentExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (recentWorkouts.isEmpty()) {
                            Text(
                                text = "No recent workout logs",
                                color = TextLo,
                                fontSize = 13.sp
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
                                    text = "View more",
                                    color = MintAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 100 dp bottom padding for floating bottom nav bar
            Spacer(modifier = Modifier.height(100.dp))
        }
        }

        // Snackbar Host floating above nav
        UndoSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
        )
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.isBlank()
}

@Composable
fun RecentLogItem(name: String, date: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = TextHi,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Text(date, color = TextLo, fontSize = 12.sp, maxLines = 1)
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
        FitCard(
            cornerRadius = 24.dp,
            padding = PaddingValues(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = title,
                    color = TextHi,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelText(text = label)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Raised)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        BasicTextField(
                            value = value,
                            onValueChange = { value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = TextHi,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            cursorBrush = SolidColor(TextHi)
                        )
                    }
                }

                val isValid = value.toFloatOrNull()?.let { it > 0f } == true
                PrimaryButton(
                    text = "Add",
                    onClick = { onConfirm(value) },
                    enabled = isValid
                )
            }
        }
    }
}

@Composable
private fun DashboardNotesDialog(
    title: String,
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
        FitCard(
            cornerRadius = 24.dp,
            padding = PaddingValues(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = title,
                    color = TextHi,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelText(text = "NOTES")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Raised)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        BasicTextField(
                            value = value,
                            onValueChange = { value = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = TextHi,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(TextHi)
                        )
                    }
                }

                PrimaryButton(
                    text = "Save",
                    onClick = { onConfirm(value) }
                )
            }
        }
    }
}

@Composable
fun WeightSparkline(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier,
    lineColor: Color = MintAccent
) {
    if (entries.size < 2) return

    val recentEntries = entries.takeLast(7)
    val minWeight = recentEntries.minOf { it.weightKg }
    val maxWeight = recentEntries.maxOf { it.weightKg }
    val range = (maxWeight - minWeight).let { if (it == 0f) 1f else it }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val points = recentEntries.mapIndexed { index, entry ->
            val x = (index.toFloat() / (recentEntries.size - 1)) * width
            val normalizedY = (entry.weightKg - minWeight) / range
            val y = height - (normalizedY * (height - 12.dp.toPx()) + 6.dp.toPx())
            Offset(x, y)
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val lastPoint = points.last()
        drawCircle(
            color = lineColor,
            radius = 3.5.dp.toPx(),
            center = lastPoint
        )
    }
}

@Composable
fun LogWeightDialog(
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
        FitCard(
            cornerRadius = 24.dp,
            padding = PaddingValues(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Log weight",
                    color = TextHi,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelText(text = "KG")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Raised)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = value,
                                onValueChange = { value = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                textStyle = TextStyle(
                                    color = TextHi,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                cursorBrush = SolidColor(TextHi)
                            )
                            Text(
                                text = "kg",
                                color = TextLo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                val isValid = value.toFloatOrNull()?.let { it > 0f } == true
                PrimaryButton(
                    text = "Add",
                    onClick = { onConfirm(value) },
                    enabled = isValid
                )
            }
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        FitCard(
            cornerRadius = 24.dp,
            padding = PaddingValues(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Add water",
                    color = TextHi,
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
                        contentPadding = PaddingValues(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Raised, contentColor = TextHi)
                    ) {
                        Text("+250ml", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onAddPreset(0.5f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Raised, contentColor = TextHi)
                    ) {
                        Text("+500ml", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onAddPreset(1.0f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Raised, contentColor = TextHi)
                    ) {
                        Text("+1.0L", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelText(text = "CUSTOM LITRES")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Raised)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = customValue,
                                onValueChange = { customValue = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                textStyle = TextStyle(
                                    color = TextHi,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                cursorBrush = SolidColor(TextHi)
                            )
                            Text(
                                text = "L",
                                color = TextLo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                val isValid = customValue.toFloatOrNull()?.let { it > 0f } == true
                PrimaryButton(
                    text = "Add",
                    onClick = { customValue.toFloatOrNull()?.let { onAddCustom(it) } },
                    enabled = isValid
                )
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        FitCard(
            cornerRadius = 24.dp,
            padding = PaddingValues(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    text = "Add protein",
                    color = TextHi,
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
                        contentPadding = PaddingValues(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Raised, contentColor = TextHi)
                    ) {
                        Text("+20g", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onAddPreset(25f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Raised, contentColor = TextHi)
                    ) {
                        Text("+25g", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onAddPreset(30f) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Raised, contentColor = TextHi)
                    ) {
                        Text("+30g", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelText(text = "CUSTOM GRAMS")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Raised)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = customValue,
                                onValueChange = { customValue = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                textStyle = TextStyle(
                                    color = TextHi,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                cursorBrush = SolidColor(TextHi)
                            )
                            Text(
                                text = "g",
                                color = TextLo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                val isValid = customValue.toFloatOrNull()?.let { it > 0f } == true
                PrimaryButton(
                    text = "Add",
                    onClick = { customValue.toFloatOrNull()?.let { onAddCustom(it) } },
                    enabled = isValid
                )
            }
        }
    }
}
