package com.example.workouttracker.ui

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.border
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.BasicTextField
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
    val dayNotes by viewModel.dayNotes
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
    val displayDate = try { LocalDate.parse(date).format(formatter) } catch(e: Exception) { date }

    var showWeightDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showProteinDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(date) {
        viewModel.loadWorkoutsForDate(date)
    }

    if (showWeightDialog) {
        LogMetricDialog(
            title = "Edit Weight",
            label = "KG",
            initialValue = dayMetrics?.bodyWeight?.let { if (it > 0) it.toString() else "" } ?: "",
            onDismiss = { showWeightDialog = false },
            onConfirm = { value ->
                viewModel.updateDailyMetrics(weight = value.toFloatOrNull(), date = date)
                showWeightDialog = false
            }
        )
    }

    if (showWaterDialog) {
        LogMetricDialog(
            title = "Edit Water",
            label = "LITRES",
            initialValue = dayMetrics?.water?.let { if (it > 0) it.toString() else "" } ?: "",
            onDismiss = { showWaterDialog = false },
            onConfirm = { value ->
                viewModel.updateDailyMetrics(water = value.toFloatOrNull() ?: 0f, date = date)
                showWaterDialog = false
            }
        )
    }

    if (showProteinDialog) {
        LogMetricDialog(
            title = "Edit Protein",
            label = "GRAMS",
            initialValue = dayMetrics?.protein?.let { if (it > 0) it.toInt().toString() else "" } ?: "",
            onDismiss = { showProteinDialog = false },
            onConfirm = { value ->
                viewModel.updateDailyMetrics(protein = value.toFloatOrNull() ?: 0f, date = date)
                showProteinDialog = false
            }
        )
    }

    if (showNotesDialog) {
        LogNotesDialog(
            title = "Workout Notes",
            initialValue = dayNotes ?: "",
            onDismiss = { showNotesDialog = false },
            onConfirm = { value ->
                viewModel.updateSessionNotes(date, if (value.isBlank()) null else value)
                showNotesDialog = false
            }
        )
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
                    MetricSummaryItem("Weight", dayMetrics?.bodyWeight?.let { if (it > 0) "$it kg" else "--" } ?: "--", "⚖", Modifier.weight(1f)) { showWeightDialog = true }
                    MetricSummaryItem("Water", dayMetrics?.water?.let { "$it L" } ?: "0 L", "💧", Modifier.weight(1f)) { showWaterDialog = true }
                    MetricSummaryItem("Protein", dayMetrics?.protein?.let { "${it.toInt()} g" } ?: "0 g", "💊", Modifier.weight(1f)) { showProteinDialog = true }
                }
            }

            item {
                NotesCard(notes = dayNotes, modifier = Modifier.fillMaxWidth()) {
                    showNotesDialog = true
                }
            }

            if (workouts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
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
fun MetricSummaryItem(label: String, value: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                        
                        Text(
                            text = "• $comparisonText than last time",
                            color = comparisonColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit workout",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Side-by-side Layout Content
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Column: Current Sets
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "CURRENT SESSION",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                sets.forEach { record ->
                    if (record.weight != null) {
                        Text("${record.weight} kg  •  ${record.reps} reps", fontSize = 13.sp, color = Color.White)
                    } else if (record.duration != null) {
                        Text("${record.duration} min", fontSize = 13.sp, color = Color.White)
                    }
                }
            }

            // Right Column: Previous Sets (if any)
            if (previousSets.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "LAST TIME",
                        color = MutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    previousSets.forEach { record ->
                        if (record.weight != null) {
                            Text("${record.weight} kg  •  ${record.reps} reps", fontSize = 13.sp, color = TextSecondary)
                        } else if (record.duration != null) {
                            Text("${record.duration} min", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesCard(notes: String?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = CardBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("WORKOUT NOTES", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Text("📝", fontSize = 14.sp)
            }
            if (!notes.isNullOrBlank()) {
                Text(
                    text = notes,
                    color = Color.White,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Tap to add notes for the day...",
                    color = MutedText,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun LogNotesDialog(
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
                        text = "NOTES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(InputBackground)
                            .border(1.dp, MutedText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable { focusRequester.requestFocus() }
                            .padding(16.dp)
                    ) {
                        BasicTextField(
                            value = value,
                            onValueChange = { value = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(Color.White),
                            decorationBox = { innerTextField ->
                                if (value.isEmpty()) {
                                    Text("How was your workout today? Any PRs, energy levels, or notes...", color = MutedText, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (initialValue.isNotEmpty()) {
                        Button(
                            onClick = { onConfirm("") },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F).copy(alpha = 0.2f),
                                contentColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text("Clear", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { onConfirm(value) },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
