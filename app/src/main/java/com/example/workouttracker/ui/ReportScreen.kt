package com.example.workouttracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.HealthMetric
import com.example.workouttracker.ui.components.FitCard
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.util.WeeklyGoalProgress
import com.example.workouttracker.util.formatWeight
import com.example.workouttracker.util.weekLabel
import com.example.workouttracker.util.weekRange
import com.example.workouttracker.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val RangeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.US)
private val DayFormatter = DateTimeFormatter.ofPattern("E", Locale.US)
private val ShortDays = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun ReportScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val metrics by viewModel.weekMetrics
    val workoutCounts by viewModel.weekWorkoutCounts
    val goal by viewModel.userGoal
    var weekOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(weekOffset) { viewModel.loadWeekMetrics(weekOffset) }

    val range = weekRange(LocalDate.now().plusWeeks(weekOffset.toLong()))
    val days = (0..6).map { range.start.plusDays(it.toLong()) }
    val metricsByDate = metrics.associateBy { it.date }
    val weekMetrics = days.map { metricsByDate[it.format(DateFormatter)] ?: HealthMetric(it.format(DateFormatter)) }
    val workoutDates = workoutCounts.filterValues { it > 0 }.keys.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
    val workoutProgress = com.example.workouttracker.util.weeklyGoalProgress(workoutDates, goal?.workoutsPerWeek ?: 4, range)

    Column(modifier = Modifier.fillMaxSize().background(Ground)) {
        TrendsHeader(range, weekLabel(range), weekOffset < 0, { weekOffset-- }, { if (weekOffset < 0) weekOffset++ })
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WorkoutCard(days, workoutCounts, workoutProgress)
            MetricCard("PROTEIN", averageText(weekMetrics.map { it.protein }, "g"), "Daily average", weekMetrics.map { it.protein }, goal?.dailyProteinTarget ?: 120f, "g", MintAccent)
            MetricCard("WATER", averageText(weekMetrics.map { it.water }, "L"), "Daily average", weekMetrics.map { it.water }, goal?.dailyWaterTarget ?: 3f, "L", TealAccent)
            WeightCard(days, weekMetrics.map { it.bodyWeight }, goal?.targetWeight)
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun TrendsHeader(range: ClosedRange<LocalDate>, label: String, canAdvance: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Trends", style = ScreenTitleStyle)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) { Icon(Icons.Rounded.ChevronLeft, "Previous week", tint = TextHi) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${range.start.format(RangeFormatter)} – ${range.endInclusive.format(RangeFormatter)}", color = TextHi, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (label.isNotEmpty()) Text(label, color = TextLo, fontSize = 11.sp)
            }
            IconButton(onClick = onNext, enabled = canAdvance, modifier = Modifier.size(36.dp)) { Icon(Icons.Rounded.ChevronRight, "Next week", tint = if (canAdvance) TextHi else TextLo.copy(alpha = .35f)) }
        }
    }
}

@Composable
private fun WorkoutCard(days: List<LocalDate>, counts: Map<String, Int>, progress: WeeklyGoalProgress) {
    FitCard(modifier = Modifier.fillMaxWidth()) {
        CardHeading("WORKOUTS", "Goal: ${progress.goal} / week")
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${progress.done} / ${progress.goal} days", style = CardNumberStyle)
            Text(if (progress.met) "Goal met" else "${progress.short} short of goal", color = if (progress.met) Positive else TextLo, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            days.forEach { day ->
                val completed = (counts[day.format(DateFormatter)] ?: 0) > 0
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(if (completed) MintAccent else Raised).border(1.dp, if (completed) MintAccent else CardBorder, CircleShape), Alignment.Center) {
                        if (completed) Icon(Icons.Rounded.Check, "Workout logged", tint = Ground, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(day.format(DayFormatter).take(1), color = TextLo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, summary: String, summaryLabel: String, values: List<Float>, goal: Float, unit: String, accent: Color) {
    FitCard(modifier = Modifier.fillMaxWidth()) {
        CardHeading(title, summaryLabel)
        Text(summary, style = CardNumberStyle)
        Spacer(Modifier.height(10.dp))
        val description = "$title this week: " + values.mapIndexed { i, value -> "${i + 1} ${metricValue(value, unit)}" }.joinToString(", ")
        GoalBarChart(values, goal, unit, accent, Modifier.fillMaxWidth().height(142.dp).semantics { contentDescription = description })
    }
}

@Composable
private fun GoalBarChart(values: List<Float>, goal: Float, unit: String, accent: Color, modifier: Modifier) {
    val maxValue = maxOf(goal, values.maxOrNull() ?: 0f, 1f) * 1.18f
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Column(Modifier.width(54.dp).fillMaxHeight(), Arrangement.SpaceBetween) { Text("Goal ${metricValue(goal, unit)}", color = TextLo, fontSize = 9.sp); Text("0", color = TextLo, fontSize = 9.sp) }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).fillMaxHeight()) {
            Canvas(Modifier.fillMaxSize()) {
                val goalY = size.height * (1f - goal / maxValue)
                drawLine(TextLo.copy(alpha = .65f), androidx.compose.ui.geometry.Offset(0f, goalY), androidx.compose.ui.geometry.Offset(size.width, goalY), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())))
            }
            Row(Modifier.fillMaxSize(), Arrangement.SpaceEvenly, Alignment.Bottom) {
                values.forEachIndexed { index, value ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (value > 0) metricValue(value, unit) else "–", color = if (value > 0) TextHi else TextLo, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.width(18.dp).height(100.dp * (if (value > 0) (value / maxValue).coerceIn(.04f, 1f) else .04f)).clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)).background(if (value >= goal && value > 0) accent else if (value > 0) accent.copy(alpha = .6f) else Raised))
                        Spacer(Modifier.height(4.dp))
                        Text(ShortDays[index], color = TextLo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightCard(days: List<LocalDate>, values: List<Float>, target: Float?) {
    val logged = values.mapIndexedNotNull { index, value -> if (value > 0) index to value else null }
    val latest = logged.lastOrNull()?.second
    val change = if (logged.size >= 2) logged.last().second - logged.first().second else null
    val isMovingTowardGoal = target != null && logged.size >= 2 &&
        kotlin.math.abs(latest!! - target) < kotlin.math.abs(logged.first().second - target)
    FitCard(modifier = Modifier.fillMaxWidth()) {
        CardHeading(
            "BODY WEIGHT",
            change?.let { signedWeight(it) + " this week" } ?: "Weekly change",
            if (isMovingTowardGoal) Positive else TextLo
        )
        Text(latest?.let { "${formatWeight(it.toDouble())} kg" } ?: "—", style = CardNumberStyle)
        Spacer(Modifier.height(8.dp))
        val description = "Body weight this week: " + values.map { if (it > 0) formatWeight(it.toDouble()) + " kg" else "not logged" }.joinToString(", ")
        WeightChart(days.size, values, target, Modifier.fillMaxWidth().height(150.dp).semantics { contentDescription = description })
    }
}

@Composable
private fun WeightChart(count: Int, values: List<Float>, target: Float?, modifier: Modifier) {
    val logged = values.filter { it > 0 }
    if (logged.isEmpty()) { Text("No weight logged this week", color = TextLo, fontSize = 13.sp, modifier = modifier.padding(top = 36.dp)); return }
    val min = minOf(logged.minOrNull() ?: 0f, target ?: Float.MAX_VALUE) - .4f
    val max = maxOf(logged.maxOrNull() ?: 0f, target ?: Float.MIN_VALUE) + .4f
    Canvas(modifier) {
        val range = (max - min).coerceAtLeast(.1f)
        val xStep = size.width / (count - 1).coerceAtLeast(1)
        fun y(value: Float) = size.height * (1f - (value - min) / range)
        target?.let { drawLine(TextLo.copy(alpha = .7f), androidx.compose.ui.geometry.Offset(0f, y(it)), androidx.compose.ui.geometry.Offset(size.width, y(it)), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))) }
        val points = values.mapIndexedNotNull { index, value -> if (value > 0) androidx.compose.ui.geometry.Offset(index * xStep, y(value)) else null }
        if (points.size > 1) { val path = androidx.compose.ui.graphics.Path().apply { moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) } }; drawPath(path, MintAccent, style = Stroke(2.dp.toPx())) }
        points.forEachIndexed { index, point -> drawCircle(if (index == points.lastIndex) MintAccent else TextHi, 5.dp.toPx(), point) }
    }
}

@Composable
private fun CardHeading(title: String, detail: String, detailColor: Color = TextLo) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text(title, style = LabelStyle); Text(detail, color = detailColor, fontSize = 11.sp) }
}

private fun averageText(values: List<Float>, unit: String): String {
    val logged = values.filter { it > 0 }
    if (logged.isEmpty()) return "—"
    return if (unit == "g") String.format(Locale.US, "%.0f g", logged.average()) else String.format(Locale.US, "%.1f L", logged.average())
}

private fun metricValue(value: Float, unit: String): String = if (unit == "g") "${value.toInt()} g" else String.format(Locale.US, "%.1f L", value)
private fun signedWeight(change: Float): String = String.format(Locale.US, "%+.1f kg", change)
