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
import androidx.compose.material.icons.filled.Check
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
import com.example.workouttracker.data.FitnessGoal
import com.example.workouttracker.data.UserGoal
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel

@Composable
fun GoalSetupScreen(
    viewModel: AppViewModel,
    onDone: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val currentGoal by viewModel.userGoal

    var selectedFitnessGoal by remember(currentGoal) {
        mutableStateOf(currentGoal?.fitnessGoal ?: FitnessGoal.GENERAL_FITNESS)
    }
    var currentWeightInput by remember(currentGoal) {
        mutableStateOf(currentGoal?.currentWeight?.toString() ?: "")
    }
    var targetWeightInput by remember(currentGoal) {
        mutableStateOf(currentGoal?.targetWeight?.toString() ?: "")
    }
    var proteinInput by remember(currentGoal) {
        mutableStateOf(currentGoal?.dailyProteinTarget?.toInt()?.toString() ?: "120")
    }
    var waterInput by remember(currentGoal) {
        mutableStateOf(currentGoal?.dailyWaterTarget?.toString() ?: "3.0")
    }
    var workoutsPerWeekInput by remember(currentGoal) {
        mutableStateOf(currentGoal?.workoutsPerWeek?.toString() ?: "4")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column {
                    Text(
                        text = "GOALS & TARGETS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Set your fitness target",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Fitness Goal Selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "WHAT'S YOUR GOAL?",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessGoal.entries.forEach { goalOption ->
                        val isSelected = selectedFitnessGoal == goalOption
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CardBackground else InputBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentPurple else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedFitnessGoal = goalOption }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goalOption.displayName,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = AccentPurple
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Weight Goals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalInputField(
                    title = "CURRENT WEIGHT",
                    unit = "KG",
                    value = currentWeightInput,
                    onValueChange = { currentWeightInput = it },
                    modifier = Modifier.weight(1f)
                )
                GoalInputField(
                    title = "TARGET WEIGHT",
                    unit = "KG",
                    value = targetWeightInput,
                    onValueChange = { targetWeightInput = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Nutrition Targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalInputField(
                    title = "DAILY PROTEIN",
                    unit = "GRAMS",
                    value = proteinInput,
                    onValueChange = { proteinInput = it },
                    modifier = Modifier.weight(1f)
                )
                GoalInputField(
                    title = "DAILY WATER",
                    unit = "LITRES",
                    value = waterInput,
                    onValueChange = { waterInput = it },
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. Workout Target
            GoalInputField(
                title = "WORKOUTS PER WEEK",
                unit = "DAYS / WEEK",
                value = workoutsPerWeekInput,
                onValueChange = { workoutsPerWeekInput = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            Button(
                onClick = {
                    val goal = UserGoal(
                        currentWeight = currentWeightInput.toFloatOrNull(),
                        targetWeight = targetWeightInput.toFloatOrNull(),
                        fitnessGoal = selectedFitnessGoal,
                        dailyProteinTarget = proteinInput.toFloatOrNull() ?: 120f,
                        dailyWaterTarget = waterInput.toFloatOrNull() ?: 3.0f,
                        workoutsPerWeek = workoutsPerWeekInput.toIntOrNull() ?: 4
                    )
                    viewModel.saveUserGoal(goal)
                    if (currentWeightInput.isNotBlank()) {
                        currentWeightInput.toFloatOrNull()?.let { weight ->
                            viewModel.logWeight(weight)
                        }
                    }
                    onDone()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save Goals",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GoalInputField(
    title: String,
    unit: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = unit,
                    color = MutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(InputBackground)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    cursorBrush = SolidColor(Color.White)
                )
            }
        }
    }
}
