package com.example.workouttracker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel

@Composable
fun AddWorkoutScreen(viewModel: AppViewModel, onDone: () -> Unit) {
    var selectedTab by remember { mutableStateOf("Weights") }
    var exerciseName by remember { mutableStateOf("") }
    var weightUnit by remember { mutableStateOf("kg") }
    var isDropdownVisible by remember { mutableStateOf(false) }

    // Cardio specific states
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var speed by remember { mutableStateOf("") }
    var incline by remember { mutableStateOf("") }

    val suggestions by viewModel.exerciseSuggestions
    val filteredSuggestions = suggestions.filter { it.contains(exerciseName, ignoreCase = true) }

    val sets = remember {
        mutableStateListOf(
            WorkoutSetState(1, mutableStateOf("0"), mutableStateOf("0")),
            WorkoutSetState(2, mutableStateOf("0"), mutableStateOf("0")),
            WorkoutSetState(3, mutableStateOf("0"), mutableStateOf("0"))
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(FigmaSurface)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FigmaBackground)
                    .padding(3.dp)
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

            Text("Exercise", color = FigmaMutedText, fontSize = 11.sp)

            // Exercise Input with Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FigmaBackground)
                        .border(1.dp, if (isDropdownVisible) FigmaAccent else Color(0xFFE0E0E3), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    BasicTextField(
                        value = exerciseName,
                        onValueChange = {
                            exerciseName = it
                            isDropdownVisible = it.isNotEmpty()
                        },
                        textStyle = LocalTextStyle.current.copy(color = FigmaTextPrimary, fontSize = 13.sp),
                        decorationBox = { innerTextField ->
                            if (exerciseName.isEmpty()) Text("Search exercise...", color = FigmaMutedText, fontSize = 13.sp)
                            innerTextField()
                        }
                    )
                }

                if (isDropdownVisible) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FigmaSurface),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E3)),
                        shape = RoundedCornerShape(8.dp),
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
                                DropdownItem("+ Create \"$exerciseName\"", color = FigmaAccent) {
                                    isDropdownVisible = false
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
                    Text("Weight unit", color = FigmaMutedText, fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(FigmaBackground)
                            .padding(3.dp)
                    ) {
                        UnitButton("kg", weightUnit == "kg") { weightUnit = "kg" }
                        UnitButton("lbs", weightUnit == "lbs") { weightUnit = "lbs" }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Set", color = FigmaMutedText, fontSize = 11.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                    Text("Weight ($weightUnit)", color = FigmaMutedText, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("Reps", color = FigmaMutedText, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }

                sets.forEach { set ->
                    SetRow(set)
                }

                OutlinedButton(
                    onClick = { sets.add(WorkoutSetState(sets.size + 1, mutableStateOf("0"), mutableStateOf("0"))) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E3)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FigmaTextPrimary)
                ) {
                    Text("+ Add set", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                // Cardio Section
                CardioField("Duration (min)", duration) { duration = it }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CardioField("Distance (km)", distance, modifier = Modifier.weight(1f)) { distance = it }
                    CardioField("Avg speed (km/h)", speed, modifier = Modifier.weight(1f)) { speed = it }
                }
                CardioField("Incline (%)", incline) { incline = it }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (exerciseName.isNotBlank()) {
                        if (selectedTab == "Weights") {
                            sets.forEach { set ->
                                val w = set.weight.value.toFloatOrNull() ?: 0f
                                val r = set.reps.value.toIntOrNull() ?: 0
                                // Convert lbs to kg if needed
                                val weightKg = if (weightUnit == "lbs") w * 0.453592f else w
                                viewModel.addWorkout(
                                    exerciseName = exerciseName,
                                    sets = set.id,
                                    reps = r,
                                    weightKg = weightKg
                                )
                            }
                        } else {
                            viewModel.addWorkout(
                                exerciseName = exerciseName,
                                durationMin = duration.toIntOrNull(),
                                distanceKm = distance.toFloatOrNull(),
                                avgSpeed = speed.toFloatOrNull(),
                                incline = incline.toFloatOrNull()
                            )
                        }
                        onDone()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaAccent)
            ) {
                Text("Add exercise", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

data class WorkoutSetState(val id: Int, val weight: MutableState<String>, val reps: MutableState<String>)

@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) FigmaAccent else FigmaBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else FigmaTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DropdownItem(text: String, color: Color = FigmaTextPrimary, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = color, fontSize = 12.sp)
    }
}

@Composable
fun UnitButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (isSelected) FigmaAccent else FigmaBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else FigmaTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
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
            color = FigmaTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
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
            .clip(RoundedCornerShape(7.dp))
            .background(FigmaBackground)
            .border(1.dp, Color(0xFFE0E0E3), RoundedCornerShape(7.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(color = FigmaTextPrimary, fontSize = 12.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun CardioField(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = FigmaMutedText, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(FigmaBackground)
                .border(1.dp, Color(0xFFE0E0E3), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.copy(color = FigmaTextPrimary, fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}
