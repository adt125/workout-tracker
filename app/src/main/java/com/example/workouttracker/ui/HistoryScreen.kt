package com.example.workouttracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.DaySummary
import com.example.workouttracker.data.SetRecord
import com.example.workouttracker.data.WorkoutEntry
import com.example.workouttracker.ui.components.DeltaChip
import com.example.workouttracker.ui.components.DeltaVariant
import com.example.workouttracker.ui.components.FitCard
import com.example.workouttracker.ui.components.LabelText
import com.example.workouttracker.ui.theme.Ground
import com.example.workouttracker.ui.theme.MintAccent
import com.example.workouttracker.ui.theme.Raised
import com.example.workouttracker.ui.theme.ScreenTitleStyle
import com.example.workouttracker.ui.theme.SecondaryTextStyle
import com.example.workouttracker.ui.theme.TextHi
import com.example.workouttracker.ui.theme.TextLo
import com.example.workouttracker.util.ExerciseDelta
import com.example.workouttracker.util.formatWeight
import com.example.workouttracker.util.progressDelta
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: AppViewModel,
    onNavigateDayLog: (String) -> Unit,
    onBack: () -> Unit
) {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val workoutDates by viewModel.workoutDatesForMonth

    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var daySummary by remember { mutableStateOf<DaySummary?>(null) }
    var showAllExercises by remember { mutableStateOf(false) }

    val yearMonthStr = visibleMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    LaunchedEffect(yearMonthStr) {
        viewModel.loadWorkoutDatesForMonth(yearMonthStr)
    }

    val selectedDateStr = selectedDate.format(formatter)
    LaunchedEffect(selectedDateStr) {
        daySummary = viewModel.getDaySummary(selectedDateStr)
        showAllExercises = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header & Month Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextHi
                    )
                }
                Text(
                    text = "History",
                    style = ScreenTitleStyle
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { visibleMonth = visibleMonth.minusMonths(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month", tint = TextHi)
                }
                Text(
                    text = visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)),
                    color = TextHi,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { visibleMonth = visibleMonth.plusMonths(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month", tint = TextHi)
                }
            }
        }

        // Calendar Card
        FitCard(padding = PaddingValues(16.dp)) {
            // Day of week headers (M T W T F S S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        color = TextLo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Month Grid (Monday first)
            val firstDayOfMonth = visibleMonth.atDay(1)
            val daysInMonth = visibleMonth.lengthOfMonth()
            val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday
            val leadEmptyDays = dayOfWeekValue - 1

            val totalCells = leadEmptyDays + daysInMonth
            val rows = (totalCells + 6) / 7

            val today = LocalDate.now()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - leadEmptyDays + 1

                            if (dayNumber in 1..daysInMonth) {
                                val date = visibleMonth.atDay(dayNumber)
                                val dateStr = date.format(formatter)
                                val hasWorkout = workoutDates.contains(dateStr)
                                val isSelected = date == selectedDate
                                val isToday = date == today
                                val isFuture = date.isAfter(today)

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    hasWorkout = hasWorkout,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    isFuture = isFuture,
                                    onClick = { selectedDate = date }
                                )
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Month summary footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MintAccent)
                )
                Text(
                    text = "Workout logged — ${workoutDates.size} this month",
                    color = TextLo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Day Summary & Exercises
        val summary = daySummary
        if (summary != null) {
            val formattedDateHeader = try {
                LocalDate.parse(summary.date).format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.US))
            } catch (_: Exception) {
                summary.date
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDateHeader,
                    color = TextHi,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${summary.workouts.size} exercises",
                    color = TextLo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Three small metric tiles: Weight, Water, Protein
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val weightVal = summary.metrics?.bodyWeight?.let { if (it > 0) "${formatWeight(it.toDouble())} kg" else "--" } ?: "--"
                val waterVal = summary.metrics?.water?.let { String.format(Locale.US, "%.1f L", it) } ?: "0.0 L"
                val proteinVal = summary.metrics?.protein?.let { String.format(Locale.US, "%.0f g", it) } ?: "0 g"

                MiniStatTile(label = "WEIGHT", value = weightVal, modifier = Modifier.weight(1f))
                MiniStatTile(label = "WATER", value = waterVal, modifier = Modifier.weight(1f))
                MiniStatTile(label = "PROTEIN", value = proteinVal, modifier = Modifier.weight(1f))
            }

            // Exercise list
            if (summary.workouts.isEmpty()) {
                FitCard(padding = PaddingValues(20.dp)) {
                    Text(
                        text = "No workouts logged on this day",
                        color = TextLo,
                        fontSize = 14.sp
                    )
                }
            } else {
                val displayedWorkouts = if (showAllExercises) summary.workouts else summary.workouts.take(3)
                displayedWorkouts.forEach { workoutPair ->
                    HistoryExerciseRow(
                        workout = workoutPair.first,
                        exName = workoutPair.second,
                        viewModel = viewModel,
                        onEdit = { onNavigateDayLog(summary.date) }
                    )
                }

                if (summary.workouts.size > 3 && !showAllExercises) {
                    TextButton(
                        onClick = { showAllExercises = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Show ${summary.workouts.size - 3} more",
                            color = MintAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    hasWorkout: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val cellShape = CircleShape
    val backgroundColor = when {
        isSelected -> MintAccent
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> Ground
        isFuture -> TextLo.copy(alpha = 0.6f)
        else -> TextHi
    }
    val borderColor = if (isToday && !isSelected) MintAccent else Color.Transparent

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(cellShape)
            .background(backgroundColor)
            .border(width = if (isToday && !isSelected) 1.5.dp else 0.dp, color = borderColor, shape = cellShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (hasWorkout && !isSelected) MintAccent else Color.Transparent)
            )
        }
    }
}

@Composable
fun MiniStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    FitCard(
        modifier = modifier,
        padding = PaddingValues(10.dp)
    ) {
        LabelText(text = label)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextHi,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HistoryExerciseRow(
    workout: WorkoutEntry,
    exName: String,
    viewModel: AppViewModel,
    onEdit: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var previousSets by remember { mutableStateOf<List<SetRecord>>(emptyList()) }
    val sets = workout.sets

    LaunchedEffect(workout.exerciseId, workout.date) {
        workout.exerciseId?.let {
            previousSets = viewModel.getPreviousRecord(it, workout.date)
        }
    }

    val delta = progressDelta(sets, previousSets)
    val deltaChipText = when (delta) {
        is ExerciseDelta.TopWeightUp -> "+${formatWeight(delta.diffKg)} kg"
        is ExerciseDelta.RepsUp -> "+${delta.diffReps} ${if (delta.diffReps == 1) "rep" else "reps"}"
        is ExerciseDelta.VolumeUp -> "+${delta.percent}% vol"
        is ExerciseDelta.Same -> "Same"
        is ExerciseDelta.Lower -> delta.desc
        ExerciseDelta.None -> null
    }
    val deltaVariant = when (delta) {
        is ExerciseDelta.Same, is ExerciseDelta.Lower -> DeltaVariant.NEUTRAL
        else -> DeltaVariant.POSITIVE
    }

    val topSetSummary = sets.filter { it.weight != null && it.reps != null }
        .maxWithOrNull(compareBy<SetRecord> { it.weight!! }.thenBy { it.reps!! })

    val summarySubtitle = if (topSetSummary != null && topSetSummary.weight != null) {
        "${sets.size} sets · top ${formatWeight(topSetSummary.weight.toDouble())} kg × ${topSetSummary.reps}"
    } else if (sets.firstOrNull()?.duration != null) {
        "${sets.first().duration} min cardio"
    } else {
        "${sets.size} sets"
    }

    FitCard(
        onClick = { isExpanded = !isExpanded },
        padding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = exName,
                    color = TextHi,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summarySubtitle,
                    style = SecondaryTextStyle,
                    color = TextLo,
                    fontSize = 13.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (deltaChipText != null) {
                    DeltaChip(text = deltaChipText, variant = deltaVariant)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = "Expand",
                    tint = TextLo,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expanded view showing current vs previous sets and edit pencil
        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Raised)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SETS DETAIL",
                            color = TextLo,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.08.em
                        )
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit workout",
                                tint = TextHi,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    sets.forEachIndexed { idx, record ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Set ${idx + 1}",
                                color = TextLo,
                                fontSize = 13.sp
                            )
                            val recordText = if (record.weight != null && record.reps != null) {
                                "${formatWeight(record.weight.toDouble())} kg × ${record.reps}"
                            } else if (record.duration != null) {
                                "${record.duration} min"
                            } else {
                                "-"
                            }
                            Text(
                                text = recordText,
                                color = TextHi,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
