package com.example.workouttracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.AppTheme
import com.example.workouttracker.ui.theme.CardNumberStyle
import com.example.workouttracker.ui.theme.MintAccent
import com.example.workouttracker.ui.theme.ProgressBrush
import com.example.workouttracker.ui.theme.SecondaryTextStyle
import com.example.workouttracker.ui.theme.TextHi
import com.example.workouttracker.ui.theme.TextLo

@Composable
fun StatTile(
    label: String,
    icon: ImageVector,
    currentValueText: String,
    goalValueText: String,
    progress: Float,
    remainingText: String,
    chip1Label: String,
    onChip1Click: () -> Unit,
    chip2Label: String,
    onChip2Click: () -> Unit,
    onTileClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MintAccent,
    progressBrush: Brush = ProgressBrush
) {
    FitCard(
        modifier = modifier,
        onClick = onTileClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabelText(text = label)
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentValueText,
                    style = CardNumberStyle,
                    color = TextHi
                )
                Text(
                    text = " / $goalValueText",
                    style = SecondaryTextStyle,
                    color = TextLo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FitProgressBar(
                progress = progress,
                height = 10.dp,
                progressBrush = progressBrush
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = remainingText,
                style = SecondaryTextStyle,
                color = TextLo
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddChip(
                    label = chip1Label,
                    onClick = onChip1Click,
                    modifier = Modifier.weight(1f)
                )
                QuickAddChip(
                    label = chip2Label,
                    onClick = onChip2Click,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080B18)
@Composable
fun StatTilePreview() {
    AppTheme {
        StatTile(
            label = "Protein",
            icon = Icons.Default.Restaurant,
            currentValueText = "45",
            goalValueText = "100 g",
            progress = 0.45f,
            remainingText = "55 g left",
            chip1Label = "+20",
            onChip1Click = {},
            chip2Label = "+25",
            onChip2Click = {},
            onTileClick = {}
        )
    }
}
