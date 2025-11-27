package com.example.personaltaskapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.viewmodel.HabitViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(habitViewModel: HabitViewModel = viewModel()) {
    val habits by habitViewModel.habits.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("💪 Habit Tracker") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (habits.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No habits yet. Tap + to add one.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItemCard(habit, onDelete = { habitViewModel.deleteHabit(habit.id) })
                    }
                }
            }
        }

        if (showDialog) {
            AddHabitDialog(onDismiss = { showDialog = false }) { newHabit ->
                habitViewModel.addHabit(newHabit)
                showDialog = false
            }
        }
    }
}

@Composable
fun HabitItemCard(habit: Habit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(habit.title, style = MaterialTheme.typography.titleMedium)
                habit.description?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Text("Days: ${habit.daysOfWeek}", style = MaterialTheme.typography.labelSmall)
                Text("Start: ${habit.startTimeIso.takeLast(8)}", style = MaterialTheme.typography.labelSmall)
                Text("Duration: ${habit.durationMinutes} min", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete habit")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (Habit) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTimeText by remember { mutableStateOf("07:00") }
    var durationText by remember { mutableStateOf("30") }
    var daysOfWeek by remember { mutableStateOf("MON,WED,FRI") }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val startTimeIso = "2025-11-03T" + LocalTime.parse(startTimeText, timeFormatter).toString()
                    onAdd(
                        Habit(
                            title = title,
                            description = description.ifBlank { null },
                            startTimeIso = startTimeIso,
                            durationMinutes = durationText.toIntOrNull() ?: 30,
                            daysOfWeek = daysOfWeek
                        )
                    )
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") })
                OutlinedTextField(value = startTimeText, onValueChange = { startTimeText = it }, label = { Text("Start Time (HH:mm)") })
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter(Char::isDigit) },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = daysOfWeek, onValueChange = { daysOfWeek = it }, label = { Text("Days (e.g. MON,WED,FRI)") })
            }
        }
    )
}
