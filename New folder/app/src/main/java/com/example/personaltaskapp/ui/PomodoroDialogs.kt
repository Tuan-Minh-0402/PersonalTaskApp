package com.example.personaltaskapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.personaltaskapp.model.PomodoroState
import com.example.personaltaskapp.model.Task

// ─────────────────────────────────────────────
// SELECT TASK FOR POMODORO
// ─────────────────────────────────────────────
@Composable
fun PomodoroTaskSelectorDialog(
    tasks: List<Task>,
    onDismiss: () -> Unit,
    onStart: (Task) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(Modifier.padding(16.dp)) {
                Text("Select Task", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(12.dp))

                LazyColumn {
                    items(tasks) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStart(task) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(task.title, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.weight(1f))
                            Text("${task.completedPomodoros}/${task.pomodoroCount}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// RUNNING POMODORO DIALOG
// ─────────────────────────────────────────────
@Composable
fun PomodoroRunningDialog(
    secondsLeft: Int,
    onCancel: () -> Unit,
    state: PomodoroState
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pomodoro Running", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(20.dp))

                val minutes = secondsLeft / 60
                val seconds = secondsLeft % 60
                Text(
                    String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(Modifier.height(20.dp))

                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}
