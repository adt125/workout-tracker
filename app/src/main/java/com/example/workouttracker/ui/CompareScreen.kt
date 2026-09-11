package com.example.workouttracker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel

@Composable
fun CompareScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    // Mock data based on Figma
    val avgWeight = "72.8 kg"
    val weightDelta = "-0.4 kg"
    val waterTotal = "12.4 L"
    val waterDelta = "+1.2 L"
    val workouts = "5 sessions"

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "This week vs last",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            ComparisonRow(label = "Avg weight", value = avgWeight, delta = weightDelta)
            ComparisonRow(label = "Water total", value = waterTotal, delta = waterDelta)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Workouts", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Column(horizontalAlignment = Alignment.End) {
                    Text(workouts, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "same as last week",
                        color = MutedText,
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVITY COMPARISON",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendItem("Last", Color.White)
                    LegendItem("This", ChartCyan)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    ChartBar(height = 80.dp, color = ChartPurple, modifier = Modifier.weight(1f))
                    ChartBar(height = 120.dp, color = ChartCyan, modifier = Modifier.weight(1f))
                    ChartBar(height = 50.dp, color = ChartPurple, modifier = Modifier.weight(1f))
                    ChartBar(height = 140.dp, color = ChartCyan, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(label, color = MutedText, fontSize = 11.sp)
    }
}

@Composable
fun ComparisonRow(label: String, value: String, delta: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Column(horizontalAlignment = Alignment.End) {
                Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = delta,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Divider(color = MutedText.copy(alpha = 0.2f), thickness = 1.dp)
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
