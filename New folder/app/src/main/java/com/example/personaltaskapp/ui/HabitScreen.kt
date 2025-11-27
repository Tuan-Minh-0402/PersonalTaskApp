package com.example.personaltaskapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.viewmodel.HabitViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitScreen(viewModel: HabitViewModel) {

    val habits by viewModel.habits.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Tracker") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            if (habits.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No habits yet. Tap + to add one.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItemCard(
                            habit = habit,
                            onDelete = { viewModel.deleteHabit(habit) }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddHabitDialog(
                onDismiss = { showDialog = false },
                onAdd = { newHabit ->
                    viewModel.addHabit(newHabit)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun HabitItemCard(habit: Habit, onDelete: () -> Unit) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(Modifier.weight(1f)) {

                Text(habit.title, style = MaterialTheme.typography.titleMedium)

                habit.description?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }

                Text("Days: ${habit.daysOfWeek}", style = MaterialTheme.typography.labelSmall)

                Text(
                    "Start: ${habit.startTimeIso.takeLast(8)}",
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    "Duration: ${habit.durationMinutes} minutes",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (Habit) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTimeText by remember { mutableStateOf("07:00") }
    var durationText by remember { mutableStateOf("30") }
    var daysOfWeek by remember { mutableStateOf("MON,WED,FRI") }

    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Habit") },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {

                    val startIso =
                        "2025-01-01T" + LocalTime.parse(startTimeText, formatter).toString()

                    onAdd(
                        Habit(
                            id = 0,
                            title = title,
                            description = description.ifBlank { null },
                            startTimeIso = startIso,
                            durationMinutes = durationText.toIntOrNull() ?: 30,
                            daysOfWeek = daysOfWeek
                        )
                    )
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") }
                )

                OutlinedTextField(
                    value = startTimeText,
                    onValueChange = { startTimeText = it },
                    label = { Text("Start Time (HH:mm)") }
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter(Char::isDigit) },
                    label = { Text("Duration (minutes)") }
                )

                OutlinedTextField(
                    value = daysOfWeek,
                    onValueChange = { daysOfWeek = it },
                    label = { Text("Days (e.g. MON,WED,FRI)") }
                )
            }
        }
    )
}
