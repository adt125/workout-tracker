package com.example.workouttracker.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.AppTheme
import com.example.workouttracker.ui.theme.TextLo

@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextLo
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.08.em
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
fun LabelTextPreview() {
    AppTheme {
        LabelText(text = "Protein")
    }
}
