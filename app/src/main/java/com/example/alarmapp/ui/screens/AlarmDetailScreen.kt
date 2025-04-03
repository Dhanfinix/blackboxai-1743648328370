package com.example.alarmapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alarmapp.data.AlarmEntity
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    alarm: AlarmEntity?,
    onSave: (AlarmEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var hour by remember { mutableStateOf(alarm?.hour ?: 8) }
    var minute by remember { mutableStateOf(alarm?.minute ?: 0) }
    var label by remember { mutableStateOf(alarm?.label ?: "") }
    var repeatDays by remember {
        mutableStateOf(
            mutableStateMapOf(
                DayOfWeek.MONDAY to (alarm?.repeatOnMonday ?: false),
                DayOfWeek.TUESDAY to (alarm?.repeatOnTuesday ?: false),
                DayOfWeek.WEDNESDAY to (alarm?.repeatOnWednesday ?: false),
                DayOfWeek.THURSDAY to (alarm?.repeatOnThursday ?: false),
                DayOfWeek.FRIDAY to (alarm?.repeatOnFriday ?: false),
                DayOfWeek.SATURDAY to (alarm?.repeatOnSaturday ?: false),
                DayOfWeek.SUNDAY to (alarm?.repeatOnSunday ?: false)
            )
        )
    }
    var vibrate by remember { mutableStateOf(alarm?.vibrate ?: true) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarm == null) "Add Alarm" else "Edit Alarm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%02d:%02d", hour, minute),
                        style = MaterialTheme.typography.displayLarge
                    )
                    TextButton(
                        onClick = { showTimePicker = true }
                    ) {
                        Text("Change Time")
                    }
                }
            }

            // Label Input
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                maxLines = 1
            )

            // Repeat Days
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Repeat",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DayOfWeek.values().forEach { day ->
                            DayToggleButton(
                                day = day,
                                selected = repeatDays[day] ?: false,
                                onSelectedChange = { selected ->
                                    repeatDays[day] = selected
                                }
                            )
                        }
                    }
                }
            }

            // Vibration Setting
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vibrate",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it }
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    val newAlarm = (alarm ?: AlarmEntity(hour = hour, minute = minute)).copy(
                        hour = hour,
                        minute = minute,
                        label = label,
                        repeatOnMonday = repeatDays[DayOfWeek.MONDAY] ?: false,
                        repeatOnTuesday = repeatDays[DayOfWeek.TUESDAY] ?: false,
                        repeatOnWednesday = repeatDays[DayOfWeek.WEDNESDAY] ?: false,
                        repeatOnThursday = repeatDays[DayOfWeek.THURSDAY] ?: false,
                        repeatOnFriday = repeatDays[DayOfWeek.FRIDAY] ?: false,
                        repeatOnSaturday = repeatDays[DayOfWeek.SATURDAY] ?: false,
                        repeatOnSunday = repeatDays[DayOfWeek.SUNDAY] ?: false,
                        vibrate = vibrate
                    )
                    onSave(newAlarm)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Alarm")
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                showTimePicker = false
            },
            initialHour = hour,
            initialMinute = minute
        )
    }
}

@Composable
private fun DayToggleButton(
    day: DayOfWeek,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    val dayLabel = day.name.take(1)
    
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(dayLabel) },
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Simple time picker using number pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours
                    NumberPicker(
                        value = selectedHour,
                        onValueChange = { selectedHour = it },
                        range = 0..23
                    )
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    // Minutes
                    NumberPicker(
                        value = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        range = 0..59
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedHour, selectedMinute) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column {
        IconButton(
            onClick = {
                val newValue = if (value + 1 > range.last) range.first else value + 1
                onValueChange(newValue)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
        }
        
        Text(
            text = String.format("%02d", value),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        IconButton(
            onClick = {
                val newValue = if (value - 1 < range.first) range.last else value - 1
                onValueChange(newValue)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
        }
    }
}