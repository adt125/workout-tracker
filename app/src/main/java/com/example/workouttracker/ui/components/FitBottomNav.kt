package com.example.workouttracker.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.AppTheme
import com.example.workouttracker.ui.theme.Card
import com.example.workouttracker.ui.theme.CardBorder
import com.example.workouttracker.ui.theme.MintAccent
import com.example.workouttracker.ui.theme.Raised
import com.example.workouttracker.ui.theme.TextLo

data class NavItemSpec(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val DefaultNavItems = listOf(
    NavItemSpec("dashboard", "Home", Icons.Rounded.Home),
    NavItemSpec("this_week", "Week", Icons.Rounded.CalendarMonth),
    NavItemSpec("history", "History", Icons.Rounded.History),
    NavItemSpec("report", "Trends", Icons.AutoMirrored.Rounded.TrendingUp)
)

@Composable
fun FitBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItemSpec> = DefaultNavItems
) {
    val navShape = RoundedCornerShape(34.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(68.dp)
            .clip(navShape)
            .background(Card)
            .border(width = 1.dp, color = CardBorder, shape = navShape)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                FitNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun FitNavItem(
    item: NavItemSpec,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Raised else Color.Transparent
    val contentColor = if (isSelected) MintAccent else TextLo
    val shape = RoundedCornerShape(26.dp)

    Box(
        modifier = Modifier
            .height(52.dp)
            .widthIn(min = 72.dp)
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = item.label,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080B18)
@Composable
fun FitBottomNavPreview() {
    AppTheme {
        FitBottomNav(
            currentRoute = "dashboard",
            onNavigate = {}
        )
    }
}
