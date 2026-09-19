package com.example.workouttracker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: AppViewModel, onNavigateDayLog: (String) -> Unit, onBack: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

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
            Text(
                "History", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth(),
                showModeToggle = false,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors(
                    containerColor = AppBackground,
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    navigationContentColor = Color.White,
                    yearContentColor = Color.White,
                    disabledYearContentColor = MutedText,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = AccentPurple,
                    dayContentColor = Color.White,
                    disabledDayContentColor = MutedText,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = AccentPurple,
                    todayContentColor = AccentPurple,
                    todayDateBorderColor = AccentPurple
                )
            )
            
            val selectedDate = datePickerState.selectedDateMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            
            if (selectedDate != null) {
                Button(
                    onClick = { onNavigateDayLog(selectedDate.format(formatter)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppBackground)
                ) {
                    Text("View Log for ${selectedDate.format(DateTimeFormatter.ofPattern("d MMM"))}", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
