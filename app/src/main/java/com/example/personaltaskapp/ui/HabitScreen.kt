package com.example.personaltaskapp.ui

import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitScreen(viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Habit Tracker") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (habits.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No habits yet")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    habits.forEach { habit ->
                        HabitItem(
                            habit = habit,
                            onEdit = { editingHabit = habit },
                            onDelete = { viewModel.deleteHabit(habit) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || editingHabit != null) {
        AddHabitDialog(
            initialHabit = editingHabit,
            onDismiss = {
                showAddDialog = false
                editingHabit = null
            },
            onSave = { savedHabit ->
                if (editingHabit == null) viewModel.addHabit(savedHabit) else viewModel.updateHabit(savedHabit)
                showAddDialog = false
                editingHabit = null
            }
        )
    }
}

@Composable
fun HabitItem(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                habit.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text("Days: ${habit.frequency}")

            Text("Start: ${formatMinutes(habit.startMinutes)}")
            Text("Duration: ${habit.durationMinutes} min")
            Text("Streak: ${habit.streak}")

            Spacer(Modifier.height(6.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Edit",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onEdit() }
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Delete",
                    color = Color.Red,
                    modifier = Modifier.clickable { onDelete() }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddHabitDialog(
    initialHabit: Habit? = null,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit
) {
    var title by remember(initialHabit?.id) { mutableStateOf(initialHabit?.title ?: "") }

    // Frequency selection
    val freqOptions = listOf("Daily", "Weekdays", "Weekends", "Custom")
    var selectedFreq by remember(initialHabit?.id) {
        mutableStateOf(
            when (initialHabit?.frequency) {
                "DAILY" -> "Daily"
                "MON,TUE,WED,THU,FRI" -> "Weekdays"
                "SAT,SUN" -> "Weekends"
                null -> freqOptions.first()
                else -> "Custom"
            }
        )
    }

    // Custom days
    val customDays = remember {
        mutableStateListOf(false, false, false, false, false, false, false)
    }
    val weekdays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    // Time Picker
    val context = LocalContext.current
    var startMinutes by remember(initialHabit?.id) { mutableStateOf(initialHabit?.startMinutes ?: 8 * 60) }
    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute -> startMinutes = hour * 60 + minute },
        startMinutes / 60,
        startMinutes % 60,
        true
    )

    var duration by remember(initialHabit?.id) { mutableStateOf(initialHabit?.durationMinutes ?: 30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialHabit == null) "Add Habit" else "Edit Habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit name") }
                )

                // Frequency dropdown
                FrequencyDropdown(
                    selected = selectedFreq,
                    onSelected = { selectedFreq = it },
                    options = freqOptions
                )

                // Custom expandable section
                if (selectedFreq == "Custom") {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F1F1), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("Select days:", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))

                        weekdays.forEachIndexed { i, day ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        customDays[i] = !customDays[i]
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = customDays[i],
                                    onCheckedChange = { customDays[i] = it }
                                )
                                Text(day)
                            }
                        }
                    }
                }

                // Time selector
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { timePicker.show() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Start Time")
                    Text(formatMinutes(startMinutes), fontWeight = FontWeight.Bold)
                }

                // Duration
                OutlinedTextField(
                    value = duration.toString(),
                    onValueChange = { it.toIntOrNull()?.let { d -> duration = d } },
                    label = { Text("Duration (min)") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val freq = when (selectedFreq) {
                    "Daily" -> "DAILY"
                    "Weekdays" -> "MON,TUE,WED,THU,FRI"
                    "Weekends" -> "SAT,SUN"
                    else -> weekdays
                        .filterIndexed { i, _ -> customDays[i] }
                        .joinToString(",")
                }

                onSave(
                    Habit(
                        id = initialHabit?.id ?: 0,
                        title = title,
                        description = initialHabit?.description,
                        frequency = freq,
                        startMinutes = startMinutes,
                        durationMinutes = duration,
                        streak = initialHabit?.streak ?: 0,
                        lastCompletedIso = initialHabit?.lastCompletedIso
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun FrequencyDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(selected)
            Icon(Icons.Default.Add, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun formatMinutes(min: Int): String {
    val h = min / 60
    val m = min % 60
    return "%02d:%02d".format(h, m)
}