package com.example.workouttracker.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.workouttracker.notifications.AppNotificationManager
import com.example.workouttracker.notifications.NotificationSettings
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val manager = remember { AppNotificationManager(context) }
    var settings by remember { mutableStateOf(manager.getSettings()) }
    var timePickerTarget by remember { mutableStateOf<String?>(null) }
    var draftTime by remember { mutableStateOf(LocalTime.of(settings.waterStartHour, settings.waterStartMinute)) }
    var showIntervalPicker by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun update(newSettings: NotificationSettings) {
        settings = newSettings
        manager.saveSettings(newSettings)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Stay consistent", style = MaterialTheme.typography.titleMedium)
            NotificationRow(
                title = "Daily check-in",
                subtitle = "Every day at 10:00 PM",
                checked = settings.checkInEnabled,
                onCheckedChange = { update(settings.copy(checkInEnabled = it)) }
            )
            NotificationRow(
                title = "Water reminder",
                subtitle = "Every ${formatInterval(settings.waterIntervalMinutes)}, from ${formatTime(settings.waterStartHour, settings.waterStartMinute)} to ${formatTime(settings.waterEndHour, settings.waterEndMinute)}",
                checked = settings.waterEnabled,
                onCheckedChange = { update(settings.copy(waterEnabled = it)) },
                onClick = {
                    showIntervalPicker = true
                }
            )
            Text("You can change notification permission anytime in Android Settings.", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (timePickerTarget != null) {
        val state = androidx.compose.material3.rememberTimePickerState(draftTime.hour, draftTime.minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { timePickerTarget = null },
            title = { Text(if (timePickerTarget == "start") "Water reminder start" else "Water reminder end") },
            text = { TimeInput(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    val newSettings = if (timePickerTarget == "start") {
                        settings.copy(waterStartHour = state.hour, waterStartMinute = state.minute)
                    } else {
                        settings.copy(waterEndHour = state.hour, waterEndMinute = state.minute)
                    }
                    update(newSettings)
                    timePickerTarget = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { timePickerTarget = null }) { Text("Cancel") } }
        )
    }

    if (showIntervalPicker) {
        AlertDialog(
            onDismissRequest = { showIntervalPicker = false },
            title = { Text("Water reminder schedule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Remind me every", style = MaterialTheme.typography.labelLarge)
                    listOf(15, 30, 60, 120).forEach { interval ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settings.waterIntervalMinutes == interval,
                                onClick = { update(settings.copy(waterIntervalMinutes = interval)) }
                            )
                            Text(formatInterval(interval))
                        }
                    }
                    Text("Start and end", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            draftTime = LocalTime.of(settings.waterStartHour, settings.waterStartMinute)
                            timePickerTarget = "start"
                            showIntervalPicker = false
                        }) { Text("Start: ${formatTime(settings.waterStartHour, settings.waterStartMinute)}") }
                        TextButton(onClick = {
                            draftTime = LocalTime.of(settings.waterEndHour, settings.waterEndMinute)
                            timePickerTarget = "end"
                            showIntervalPicker = false
                        }) { Text("End: ${formatTime(settings.waterEndHour, settings.waterEndMinute)}") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showIntervalPicker = false }) { Text("Done") } }
        )
    }
}

@Composable
private fun NotificationRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.padding(vertical = 4.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(Icons.Rounded.Notifications, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        if (onClick != null) TextButton(onClick = onClick) { Text("Change") }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun formatTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))

private fun formatInterval(minutes: Int): String = when {
    minutes % 60 == 0 -> "${minutes / 60} hr"
    else -> "$minutes min"
}
