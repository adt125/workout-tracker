package com.example.workouttracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel

@Composable
fun CompareScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    // Mock data based on Figma
    val avgWeight = "72.6 kg"
    val weightDelta = "-0.4 kg"
    val waterTotal = "12.4 L"
    val waterDelta = "+1.2 L"
    val workouts = "5 sessions"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(FigmaSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Text("←", fontSize = 20.sp, color = FigmaTextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This week vs last",
                    color = FigmaTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ComparisonRow(label = "Avg weight", value = avgWeight, delta = weightDelta)
            Divider(color = Color(0xFFE5E5E8), thickness = 1.dp)
            ComparisonRow(label = "Water total", value = waterTotal, delta = waterDelta)
            Divider(color = Color(0xFFE5E5E8), thickness = 1.dp)
            ComparisonRow(label = "Workouts", value = workouts, delta = "same as last week", isMutedDelta = true)

            Spacer(modifier = Modifier.height(14.dp))

            // Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                ChartBar(height = 38.dp, color = FigmaChartLight, modifier = Modifier.weight(1f))
                ChartBar(height = 52.dp, color = FigmaChartDark, modifier = Modifier.weight(1f))
                ChartBar(height = 28.dp, color = FigmaChartLight, modifier = Modifier.weight(1f))
                ChartBar(height = 63.dp, color = FigmaChartDark, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ComparisonRow(label: String, value: String, delta: String, isMutedDelta: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = FigmaTextPrimary, fontSize = 13.sp)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, color = FigmaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                text = delta,
                color = if (isMutedDelta) FigmaMutedText else Color(0xFF299959),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ChartBar(height: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(color)
    )
}
