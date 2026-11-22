package com.example.workouttracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.AppTheme
import com.example.workouttracker.ui.theme.MintAccent
import com.example.workouttracker.ui.theme.Raised
import com.example.workouttracker.ui.theme.TextLo

enum class DeltaVariant { POSITIVE, NEUTRAL }

@Composable
fun DeltaChip(
    text: String,
    variant: DeltaVariant,
    modifier: Modifier = Modifier
) {
    val containerColor = when (variant) {
        DeltaVariant.POSITIVE -> MintAccent.copy(alpha = 0.15f)
        DeltaVariant.NEUTRAL -> Raised
    }
    val contentColor = when (variant) {
        DeltaVariant.POSITIVE -> MintAccent
        DeltaVariant.NEUTRAL -> TextLo
    }
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .height(28.dp)
            .clip(shape)
            .background(containerColor)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080B18)
@Composable
fun DeltaChipPreview() {
    AppTheme {
        DeltaChip(text = "+2 reps", variant = DeltaVariant.POSITIVE)
    }
}
