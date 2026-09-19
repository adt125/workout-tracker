package com.example.workouttracker.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.SetRecord
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
    var distance by remember { mutableStateOf("") }
    var speed by remember { mutableStateOf("") }
    var incline by remember { mutableStateOf("") }

    val suggestions by viewModel.exerciseSuggestions
    val filteredSuggestions = suggestions.filter { it.contains(exerciseName, ignoreCase = true) }

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
                        tint = Color.White
                    )
                }
                
                if (entryId != null) {
                    TextButton(onClick = {
                        viewModel.deleteWorkout(entryId, workoutDate)
                        onDone()
                    }) {
                        Text("Delete", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tab Bar
            if (entryId == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
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
                Text("EXERCISE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)

                // Exercise Input with Dropdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(InputBackground)
                            .border(1.dp, if (isDropdownVisible) AccentPurple else MutedText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        BasicTextField(
                            value = exerciseName,
                            onValueChange = {
                                exerciseName = it
                                isDropdownVisible = it.isNotEmpty()
                            },
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 15.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            decorationBox = { innerTextField ->
                                if (exerciseName.isEmpty()) Text("Bench", color = MutedText, fontSize = 15.sp)
                                innerTextField()
                            }
                        )
                    }

                    if (isDropdownVisible) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                filteredSuggestions.forEach { suggestion ->
                                    DropdownItem(suggestion) {
                                        exerciseName = suggestion
                                        isDropdownVisible = false
                                    }
                                }
                                if (!suggestions.any { it.equals(exerciseName, ignoreCase = true) }) {
                                    DropdownItem("+ Create \"$exerciseName\"", color = AccentPurple) {
                                        isDropdownVisible = false
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
                    Text("WEIGHT UNIT", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardBackground)
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
                    Text("SET", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                    Text("WEIGHT (${weightUnit.uppercase()})", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("REPS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }

                sets.forEach { set ->
                    SetRow(set)
                }

                Button(
                    onClick = { sets.add(WorkoutSetState(sets.size + 1, mutableStateOf(""), mutableStateOf(""))) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
                ) {
                    Text("+ Add set", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                // Cardio Section
                CardioField("DURATION (MIN)", duration) { duration = it }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardioField("DISTANCE (KM)", distance, modifier = Modifier.weight(1f)) { distance = it }
                    CardioField("AVG SPEED", speed, modifier = Modifier.weight(1f)) { speed = it }
                }
                CardioField("INCLINE", incline) { incline = it }
            }

            Button(
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
                enabled = exerciseName.isNotBlank() && (if (selectedTab == "Weights") sets.any { it.weight.value.isNotBlank() && it.reps.value.isNotBlank() } else duration.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, 
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.3f),
                    disabledContentColor = Color.Black.copy(alpha = 0.5f)
                )
            ) {
                Text(if (entryId != null) "Update exercise" else "Add exercise", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

data class WorkoutSetState(val id: Int, val weight: MutableState<String>, val reps: MutableState<String>)

@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DropdownItem(text: String, color: Color = Color.White, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text, color = color, fontSize = 15.sp)
    }
}

@Composable
fun UnitButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SetRow(set: WorkoutSetState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${set.id}",
            color = TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )
        EditableInputBox(value = set.weight.value, onValueChange = { set.weight.value = it }, modifier = Modifier.weight(1f))
        EditableInputBox(value = set.reps.value, onValueChange = { set.reps.value = it }, modifier = Modifier.weight(1f))
    }
}

@Composable
fun EditableInputBox(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(InputBackground)
            .border(1.dp, MutedText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 15.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun CardioField(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(InputBackground)
                .border(1.dp, MutedText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 15.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}
