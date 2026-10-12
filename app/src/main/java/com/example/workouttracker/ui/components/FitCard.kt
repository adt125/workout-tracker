package com.example.workouttracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.workouttracker.ui.theme.AppTheme
import com.example.workouttracker.ui.theme.Card
import com.example.workouttracker.ui.theme.CardBorder

@Composable
fun FitCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color = Card,
    borderColor: Color = CardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = modifier
        .clip(shape)
        .background(backgroundColor)
        .border(width = 1.dp, color = borderColor, shape = shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(padding)

    Column(
        modifier = cardModifier,
        content = content
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF080B18)
@Composable
fun FitCardPreview() {
    AppTheme {
        FitCard {
            Text(text = "Card Content", color = Color.White)
        }
    }
}
