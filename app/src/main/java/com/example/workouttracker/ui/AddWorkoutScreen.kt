package com.example.workouttracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.workouttracker.data.SetRecord
import com.example.workouttracker.ui.components.FitCard
import com.example.workouttracker.ui.components.LabelText
import com.example.workouttracker.ui.components.PrimaryButton
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddWorkoutScreen(viewModel: AppViewModel, entryId: Long? = null, onDone: () -> Unit) {
    var selectedTab by remember { mutableStateOf("Weights") }
    var exerciseName by remember { mutableStateOf("") }
    var weightUnit by remember { mutableStateOf("kg") }
    var isDropdownVisible by remember { mutableStateOf(false) }
    var workoutDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }

    // Cardio specific states
    var duration by remember { mutableStateOf("") }

    val suggestions by viewModel.exerciseSuggestions
    val filteredSuggestions = suggestions.filter { it.contains(exerciseName, ignoreCase = true) }
    var showAllExercisesDialog by remember { mutableStateOf(false) }

    if (showAllExercisesDialog) {
        Dialog(onDismissRequest = { showAllExercisesDialog = false }) {
            FitCard(
                cornerRadius = 24.dp,
                padding = PaddingValues(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "All Exercises",
                        color = TextHi,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suggestions) { exercise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Raised)
                                    .clickable {
                                        exerciseName = exercise
                                        showAllExercisesDialog = false
                                        isDropdownVisible = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = exercise,
                                    color = TextHi,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Select",
                                    color = MintAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { showAllExercisesDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close", color = MintAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val sets = remember {
        mutableStateListOf<WorkoutSetState>()
    }

    // Initialize with default sets if not editing
    LaunchedEffect(entryId) {
        if (entryId != null) {
            val workoutPair = viewModel.getWorkoutEntry(entryId)
            if (workoutPair != null) {
                val workout = workoutPair.first
                exerciseName = workoutPair.second
                workoutDate = workout.date
                
                val firstSet = workout.sets.firstOrNull()
                if (firstSet?.duration != null) {
                    selectedTab = "Cardio"
                    duration = firstSet.duration.toString()
                } else {
                    selectedTab = "Weights"
                    sets.clear()
                    workout.sets.forEachIndexed { index, set ->
                        sets.add(WorkoutSetState(index + 1, mutableStateOf(set.weight?.toString() ?: ""), mutableStateOf(set.reps?.toString() ?: "")))
                    }
                }
            }
        } else if (sets.isEmpty()) {
            sets.addAll(listOf(
                WorkoutSetState(1, mutableStateOf(""), mutableStateOf("")),
                WorkoutSetState(2, mutableStateOf(""), mutableStateOf("")),
                WorkoutSetState(3, mutableStateOf(""), mutableStateOf(""))
            ))
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Back Button and Delete Button if editing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDone, modifier = Modifier.offset(x = (-8).dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextHi
                    )
                }
                
                if (entryId != null) {
                    TextButton(onClick = {
                        viewModel.deleteWorkout(entryId, workoutDate)
                        onDone()
                    }) {
                        Text("Delete", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tab Bar (Weights / Cardio)
            if (entryId == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Card)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    TabItem(
                        text = "Weights",
                        isSelected = selectedTab == "Weights",
                        modifier = Modifier.weight(1f)
                    ) { selectedTab = "Weights" }
                    TabItem(
                        text = "Cardio",
                        isSelected = selectedTab == "Cardio",
                        modifier = Modifier.weight(1f)
                    ) { selectedTab = "Cardio" }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelText(text = "EXERCISE")

                // Exercise Input with Dropdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Raised)
                            .border(1.dp, if (isDropdownVisible) MintAccent else CardBorder, RoundedCornerShape(12.dp))
                    ) {
                        BasicTextField(
                            value = exerciseName,
                            onValueChange = {
                                exerciseName = it
                                isDropdownVisible = it.isNotEmpty()
                            },
                            textStyle = TextStyle(color = TextHi, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                            cursorBrush = SolidColor(TextHi),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            decorationBox = { innerTextField ->
                                if (exerciseName.isEmpty()) Text("Bench press", color = TextLo, fontSize = 15.sp)
                                innerTextField()
                            }
                        )
                    }

                    if (isDropdownVisible) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FitCard(padding = PaddingValues(4.dp)) {
                            Column {
                                filteredSuggestions.forEach { suggestion ->
                                    DropdownItem(suggestion) {
                                        exerciseName = suggestion
                                        isDropdownVisible = false
                                    }
                                }
                                if (!suggestions.any { it.equals(exerciseName, ignoreCase = true) }) {
                                    DropdownItem("+ Create \"$exerciseName\"", color = MintAccent) {
                                        isDropdownVisible = false
                                    }
                                    DropdownItem("View all exercises", color = MintAccent) {
                                        isDropdownVisible = false
                                        showAllExercisesDialog = true
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedTab == "Weights") {
                // Weights Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelText(text = "WEIGHT UNIT")
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Card)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        UnitButton("kg", weightUnit == "kg") { weightUnit = "kg" }
                        UnitButton("lbs", weightUnit == "lbs") { weightUnit = "lbs" }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabelText(text = "SET", modifier = Modifier.width(28.dp), color = TextLo)
                    LabelText(text = "WEIGHT (${weightUnit.uppercase()})", modifier = Modifier.weight(1f))
                    LabelText(text = "REPS", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(28.dp)) // space for status indicator
                }

                sets.forEach { set ->
                    SetRow(set) {
                        sets.remove(set)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FitCard(
                        onClick = { sets.add(WorkoutSetState(sets.size + 1, mutableStateOf(""), mutableStateOf(""))) },
                        padding = PaddingValues(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("+ Add set", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextHi)
                        }
                    }

                    FitCard(
                        onClick = {
                            val lastSet = sets.lastOrNull()
                            val w = lastSet?.weight?.value ?: ""
                            val r = lastSet?.reps?.value ?: ""
                            sets.add(WorkoutSetState(sets.size + 1, mutableStateOf(w), mutableStateOf(r)))
                        },
                        padding = PaddingValues(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Copy last set", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextHi)
                        }
                    }
                }
            } else {
                // Cardio Section
                CardioField("DURATION (MIN)", duration) { duration = it }
            }

            PrimaryButton(
                text = if (entryId != null) "Update exercise" else "Add exercise",
                onClick = {
                    if (selectedTab == "Weights") {
                        val setRecords = sets.mapNotNull { set ->
                            val weightVal = set.weight.value.toFloatOrNull()
                            val repsVal = set.reps.value.toIntOrNull()
                            if (weightVal == null && repsVal == null) null
                            else {
                                val w = weightVal ?: 0f
                                val weightKg = if (weightUnit == "lbs") w * 0.453592f else w
                                SetRecord(
                                    weight = weightKg,
                                    reps = repsVal ?: 0
                                )
                            }
                        }
                        if (setRecords.isNotEmpty()) {
                            viewModel.addWorkout(
                                exerciseName = exerciseName,
                                setRecords = setRecords,
                                isCardio = false,
                                date = workoutDate,
                                replaceEntryId = entryId
                            )
                        }
                    } else {
                        val cardioRecord = SetRecord(
                            duration = duration.toIntOrNull() ?: 0
                        )
                        viewModel.addWorkout(
                            exerciseName = exerciseName,
                            setRecords = listOf(cardioRecord),
                            isCardio = true,
                            date = workoutDate,
                            replaceEntryId = entryId
                        )
                    }
                    onDone()
                },
                enabled = exerciseName.isNotBlank() && (if (selectedTab == "Weights") sets.any { it.weight.value.isNotBlank() && it.reps.value.isNotBlank() } else duration.isNotBlank())
            )

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

data class WorkoutSetState(val id: Int, val weight: MutableState<String>, val reps: MutableState<String>)

@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    val bg = if (isSelected) MintAccent else Color.Transparent
    val txtColor = if (isSelected) Ground else TextLo

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = txtColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DropdownItem(text: String, color: Color = TextHi, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text, color = color, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun UnitButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    val bg = if (isSelected) MintAccent else Color.Transparent
    val txtColor = if (isSelected) Ground else TextLo

    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = txtColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SetRow(set: WorkoutSetState, onDelete: () -> Unit) {
    val isComplete = set.weight.value.isNotBlank() && set.reps.value.isNotBlank()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${set.id}",
            color = TextLo,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )
        EditableInputBox(value = set.weight.value, onValueChange = { set.weight.value = it }, modifier = Modifier.weight(1f))
        EditableInputBox(value = set.reps.value, onValueChange = { set.reps.value = it }, modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = MintAccent,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove set",
                        tint = TextLo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditableInputBox(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Raised)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = TextHi, fontSize = 15.sp, fontWeight = FontWeight.Medium),
            cursorBrush = SolidColor(TextHi),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}

@Composable
fun CardioField(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelText(text = label)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Raised)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = TextHi, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    cursorBrush = SolidColor(TextHi),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}
