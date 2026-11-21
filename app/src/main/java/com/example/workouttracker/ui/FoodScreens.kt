package com.example.workouttracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.FoodEntry
import com.example.workouttracker.data.FoodAiClient
import com.example.workouttracker.data.ParsedItem
import com.example.workouttracker.ui.components.FitCard
import com.example.workouttracker.ui.components.LabelText
import com.example.workouttracker.ui.components.PrimaryButton
import com.example.workouttracker.ui.theme.*
import com.example.workouttracker.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class EditableFood(var name: String, var qty: String, var calories: String, var protein: String, var carbs: String, var fat: String, val rough: Boolean)
private val FoodDate = DateTimeFormatter.ISO_LOCAL_DATE
private val MealTypes = listOf("Breakfast", "Brunch", "Lunch", "Dinner", "Snack")

@Composable
fun AddMealScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<EditableFood>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mealType by remember { mutableStateOf("Breakfast") }
    var mealMenuExpanded by remember { mutableStateOf(false) }
    var showConsent by remember { mutableStateOf(false) }
    var consentAccepted by remember { mutableStateOf(context.getSharedPreferences("ai_preferences", 0).getBoolean("meal_consent", false)) }
    fun online(): Boolean = (context.getSystemService(ConnectivityManager::class.java)?.activeNetwork?.let { context.getSystemService(ConnectivityManager::class.java)?.getNetworkCapabilities(it) }?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
    fun estimateMeal() {
        error = null
        if (text.isBlank()) { error = "Describe your meal first."; return }
        if (!consentAccepted) { showConsent = true; return }
        if (!online()) { error = "No internet. You can add food manually."; return }
        loading = true
        scope.launch {
            try {
                items = FoodAiClient().parse(text.trim(), null).map { it.toEditable() }
                if (items.isEmpty()) error = "I couldn't find food in that. Try describing it differently."
            } catch (e: Exception) {
                error = when {
                    e.message?.contains("user_cap") == true -> "You have used today's AI estimates. Try again tomorrow."
                    e.message?.contains("global_cap") == true -> "AI is resting today. Try again tomorrow."
                    e.message?.contains("no_food_found") == true -> "I couldn't find food in that. Try describing it differently."
                    else -> "Couldn't estimate right now. Try again, or add manually."
                }
            } finally { loading = false }
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) { LabelText("ADD MEAL"); Text("Estimate nutrition", style = ScreenTitleStyle) }
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Raised).padding(horizontal = 12.dp, vertical = 7.dp)) {
                Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM")), color = TextLo, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
        FitCard(Modifier.fillMaxWidth(), padding = PaddingValues(10.dp)) {
            LabelText("DESCRIBE YOUR MEAL")
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                enabled = !loading,
                textStyle = LocalTextStyle.current.copy(color = TextHi, fontSize = 14.sp),
                cursorBrush = SolidColor(Accent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Raised)
                    .border(1.dp, MintAccent, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                "e.g. 2 rotis, dal and curd",
                                color = TextLo.copy(alpha = 0.65f),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Raised)
                        .border(
                            1.dp,
                            if (mealMenuExpanded) MintAccent else CardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { mealMenuExpanded = !mealMenuExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Meal type: $mealType",
                            modifier = Modifier.weight(1f),
                            color = TextLo,
                            fontSize = 15.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Text("⌄", color = TextLo, fontSize = 14.sp)
                    }
                }

                if (mealMenuExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FitCard(padding = PaddingValues(4.dp)) {
                        Column {
                            MealTypes.forEach { option ->
                                DropdownItem(option) {
                                    mealType = option
                                    mealMenuExpanded = false
                                }
                            }
                        }
                    }
                }
            }
            if (items.isNotEmpty()) {
                TextButton(onClick = { estimateMeal() }, enabled = !loading, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Re-estimate")
                }
            }
        }
        if (items.isEmpty()) {
            PrimaryButton(text = if (loading) "Estimating…" else "Estimate nutrition", onClick = { estimateMeal() }, enabled = !loading, leadingIcon = if (loading) null else Icons.Rounded.AutoAwesome)
            Text("AI estimates can be off. Check the numbers before saving.", color = TextLo, fontSize = 11.sp)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LabelText("ESTIMATED MEAL")
                Spacer(Modifier.weight(1f))
                Text("Tap a number to edit", color = TextLo, fontSize = 10.sp)
            }
            items.forEachIndexed { index, item ->
                FitCard(Modifier.fillMaxWidth(), padding = PaddingValues(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = TextHi)
                            Text(item.qty, color = TextLo, fontSize = 11.sp)
                        }
                        if (item.rough) Text("rough estimate", color = TextLo, fontSize = 10.sp)
                        IconButton(onClick = { items = items.toMutableList().also { it.removeAt(index) } }) { Icon(Icons.Rounded.Delete, "Delete", tint = TextLo) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                        FoodField("Cal", item.calories, true, Modifier.weight(1f)) { items = items.updated(index) { copy(calories = it) } }
                        FoodField("Protein", item.protein, true, Modifier.weight(1f)) { items = items.updated(index) { copy(protein = it) } }
                        FoodField("Carbs", item.carbs, true, Modifier.weight(1f)) { items = items.updated(index) { copy(carbs = it) } }
                        FoodField("Fat", item.fat, true, Modifier.weight(1f)) { items = items.updated(index) { copy(fat = it) } }
                    }
                }
            }
            FitCard(Modifier.fillMaxWidth(), backgroundColor = Raised, padding = PaddingValues(14.dp)) {
                LabelText("TOTAL ESTIMATE")
                Spacer(Modifier.height(6.dp))
                Text("${items.sumOf { it.calories.toIntOrNull() ?: 0 }} kcal", style = CardNumberStyle)
                Text("P ${items.sumOf { it.protein.toIntOrNull() ?: 0 }}g   C ${items.sumOf { it.carbs.toIntOrNull() ?: 0 }}g   F ${items.sumOf { it.fat.toIntOrNull() ?: 0 }}g", color = TextLo, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Text("AI estimates can be off. Check the numbers before saving.", color = TextLo, fontSize = 11.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = { viewModel.saveFoodEntries(items.map { FoodEntry(date = LocalDate.now().format(FoodDate), name = it.name, qty = it.qty, calories = it.calories.toIntOrNull() ?: 0, proteinG = it.protein.toIntOrNull() ?: 0, carbsG = it.carbs.toIntOrNull() ?: 0, fatG = it.fat.toIntOrNull() ?: 0, source = "ai", createdAt = System.currentTimeMillis(), mealType = mealType) }); onBack() }, modifier = Modifier.weight(1.6f)) { Text("Save to log") }
            }
        }
    }
    if (showConsent) AlertDialog(onDismissRequest = { showConsent = false }, title = { Text("AI nutrition estimate") }, text = { Text("Your meal description is sent to an AI service to estimate nutrition. Nothing else leaves your phone.") }, confirmButton = { TextButton(onClick = { consentAccepted = true; showConsent = false; context.getSharedPreferences("ai_preferences", 0).edit().putBoolean("meal_consent", true).apply() }) { Text("Continue") } }, dismissButton = { TextButton(onClick = { showConsent = false }) { Text("Cancel") } })
}

private fun ParsedItem.toEditable() = EditableFood(name, qty, calories.toString(), proteinG.toString(), carbsG.toString(), fatG.toString(), confidence == "low")
private fun List<EditableFood>.updated(index: Int, update: EditableFood.() -> EditableFood) = toMutableList().also { it[index] = it[index].update() }
@Composable private fun FoodField(label: String, value: String, numeric: Boolean = false, modifier: Modifier = Modifier, onChange: (String) -> Unit) { OutlinedTextField(value, onChange, modifier, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text), textStyle = LocalTextStyle.current.copy(fontSize = 12.sp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextHi, unfocusedTextColor = TextHi, focusedBorderColor = Accent, unfocusedBorderColor = CardBorder, focusedContainerColor = Raised, unfocusedContainerColor = Raised)) }

@Composable
fun FoodLogScreen(viewModel: AppViewModel, onBack: () -> Unit, onAddMeal: () -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now()) }; val entries by viewModel.foodEntries
    LaunchedEffect(date) { viewModel.loadFoodEntries(date.format(FoodDate)) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") }
            Column { LabelText("NUTRITION"); Text("Food log", style = ScreenTitleStyle) }
            Spacer(Modifier.weight(1f))
        }
        FitCard(Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { date = date.minusDays(1) }) { Icon(Icons.Rounded.ChevronLeft, "Previous day", tint = TextHi) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LabelText("SELECTED DAY")
                    Text(date.format(DateTimeFormatter.ofPattern("EEE, d MMM")), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                IconButton(onClick = { date = date.plusDays(1) }) { Icon(Icons.Rounded.ChevronRight, "Next day", tint = TextHi) }
            }
        }
        FoodTotalsCard(entries)
        PrimaryButton(text = "Add meal", onClick = onAddMeal, modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.AutoAwesome)
        if (entries.isEmpty()) {
            FitCard(Modifier.fillMaxWidth(), padding = PaddingValues(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("No meals logged", style = CardNumberStyle)
                    Text("Add a meal to start tracking today's nutrition.", color = TextLo, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = onAddMeal) { Text("Add meal") }
                }
            }
        } else {
            LabelText("MEALS")
            entries.groupBy { it.mealType.ifBlank { "Meal" } }.toList().forEachIndexed { index, (mealName, mealEntries) ->
                MealCard(index + 1, mealName, mealEntries, viewModel)
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun MealCard(index: Int, mealName: String, entries: List<FoodEntry>, viewModel: AppViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(if (expanded) 90f else 0f, tween(220), label = "meal-chevron")
    val calories = entries.sumOf { it.calories }
    val protein = entries.sumOf { it.proteinG }
    val carbs = entries.sumOf { it.carbsG }
    val fat = entries.sumOf { it.fatG }
    FitCard(Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Meal $index: $mealName", color = TextHi, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("${entries.size} item${if (entries.size == 1) "" else "s"}  •  $calories kcal", color = MintAccent, fontSize = 12.sp)
            }
            Icon(Icons.Rounded.ChevronRight, "Expand meal", tint = TextLo, modifier = Modifier.rotate(chevronRotation))
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(250)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(220)) + fadeOut(tween(120))
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(Modifier.height(10.dp))
                entries.forEach { entry -> FoodEntryCard(entry) { viewModel.deleteFoodEntry(entry) } }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Text("P ${protein}g", color = TextLo, fontSize = 11.sp)
                    Text("C ${carbs}g", color = TextLo, fontSize = 11.sp)
                    Text("F ${fat}g", color = TextLo, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun FoodTotalsCard(entries: List<FoodEntry>) {
    val calories = entries.sumOf { it.calories }
    val protein = entries.sumOf { it.proteinG }
    val carbs = entries.sumOf { it.carbsG }
    val fat = entries.sumOf { it.fatG }
    val mealCount = entries.map { it.mealType.ifBlank { "Meal" } }.distinct().size
    FitCard(Modifier.fillMaxWidth(), backgroundColor = Raised, padding = PaddingValues(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                LabelText("TODAY'S NUTRITION")
                Text("$calories kcal", style = CardNumberStyle)
            }
            Text("$mealCount meal${if (mealCount == 1) "" else "s"}", color = TextLo, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NutritionTotal("PROTEIN", "${protein}g", MintAccent, Modifier.weight(1f))
            NutritionTotal("CARBS", "${carbs}g", TealAccent, Modifier.weight(1f))
            NutritionTotal("FAT", "${fat}g", CoralAccent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NutritionTotal(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(Card).padding(8.dp)) {
        Text(label, color = TextLo, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(value, color = color, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

@Composable
private fun FoodEntryCard(entry: FoodEntry, onDelete: () -> Unit) {
    FitCard(Modifier.fillMaxWidth(), padding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = TextHi)
                Text(entry.qty, color = TextLo, fontSize = 12.sp)
            }
            Text("${entry.calories} kcal", color = MintAccent, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, "Delete ${entry.name}", tint = TextLo) }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("P ${entry.proteinG}g", color = TextLo, fontSize = 11.sp)
            Text("C ${entry.carbsG}g", color = TextLo, fontSize = 11.sp)
            Text("F ${entry.fatG}g", color = TextLo, fontSize = 11.sp)
        }
    }
}

@Composable private fun ManualFoodDialog(date: String, onCancel: () -> Unit, onSave: (String, String, Int, Int, Int, Int) -> Unit) { var n by remember { mutableStateOf("") }; var q by remember { mutableStateOf("") }; var c by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var carbs by remember { mutableStateOf("") }; var f by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onCancel, title = { Text("Add food") }, text = { Column { listOf("Name" to n, "Quantity" to q, "Calories" to c, "Protein" to p, "Carbs" to carbs, "Fat" to f).forEach { (label, value) -> OutlinedTextField(value, { v -> when (label) { "Name" -> n = v; "Quantity" -> q = v; "Calories" -> c = v; "Protein" -> p = v; "Carbs" -> carbs = v; else -> f = v } }, label = { Text(label) }, singleLine = true) } } }, confirmButton = { TextButton(onClick = { onSave(n, q, c.toIntOrNull() ?: 0, p.toIntOrNull() ?: 0, carbs.toIntOrNull() ?: 0, f.toIntOrNull() ?: 0) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }) }
