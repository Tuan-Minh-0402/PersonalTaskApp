package com.example.personaltaskapp.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // All tasks
    val allTasks by viewModel.tasks.collectAsState(initial = emptyList())

    // Filters
    var filterActive by rememberSaveable { mutableStateOf(false) }
    var filterDone by rememberSaveable { mutableStateOf(false) }
    var filterDueSoon by rememberSaveable { mutableStateOf(false) }

    val isAllSelected = !filterActive && !filterDone && !filterDueSoon
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    // Pomodoro dialog flags
    var showPomodoroSelector by rememberSaveable { mutableStateOf(false) }
    var showPomodoroRunning by rememberSaveable { mutableStateOf(false) }

    // Apply filtering
    val filteredTasks = remember(allTasks, filterActive, filterDone, filterDueSoon) {
        if (isAllSelected) return@remember allTasks

        allTasks.filter { task ->
            var match = false

            if (filterActive && !task.isCompleted) match = true
            if (filterDone && task.isCompleted) match = true

            if (filterDueSoon && !task.isCompleted && task.dueDateIso != null) {
                try {
                    val due = LocalDate.parse(task.dueDateIso)
                    if (due >= LocalDate.now() && due <= LocalDate.now().plusDays(7)) {
                        match = true
                    }
                } catch (_: Exception) {}
            }

            match
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Your Tasks") }) },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = { showPomodoroSelector = true },
                    containerColor = Color(0xFFD46AFF)
                ) {
                    Text("🍅", fontSize = 20.sp)
                }
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "Add")
                }
            }
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {

            // Filters
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterButton("All", isAllSelected) {
                    filterActive = false
                    filterDone = false
                    filterDueSoon = false
                }
                FilterButton("Active", filterActive) { filterActive = !filterActive }
                FilterButton("Done", filterDone) { filterDone = !filterDone }
                FilterButton("Due soon", filterDueSoon) { filterDueSoon = !filterDueSoon }

                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredTasks.forEach { task ->
                        TaskRow(task) {
                            viewModel.updateTask(task.copy(isCompleted = !task.isCompleted))
                        }
                    }
                }
            }
        }

        // Add task dialog
        if (showAddDialog) {
            AddTaskDialog(
                context = context,
                onDismiss = { showAddDialog = false },
                onSave = {
                    viewModel.addTask(it)
                    showAddDialog = false
                }
            )
        }

        // Pomodoro task select dialog
        if (showPomodoroSelector) {
            PomodoroTaskSelectorDialog(
                tasks = allTasks.filter { !it.isCompleted },
                onDismiss = { showPomodoroSelector = false },
                onStart = { task ->
                    viewModel.pomodoro.start(task)
                    showPomodoroSelector = false
                    showPomodoroRunning = true
                }
            )
        }

        // Pomodoro running dialog
        if (showPomodoroRunning && viewModel.pomodoro.isRunning) {
            PomodoroRunningDialog(
                secondsLeft = viewModel.pomodoro.secondsLeft,
                state = viewModel.pomodoro.state,
                onCancel = {
                    viewModel.pomodoro.cancel()
                    showPomodoroRunning = false
                }
            )
        }

    }
}

// Filter button composable
@Composable
fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text, color = if (selected) Color.White else Color.Black)
    }
}

// Task row
@Composable
fun TaskRow(task: Task, onToggleComplete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)

                if (!task.description.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(task.description!!, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Priority: " + when (task.priority) {
                        3 -> "High"
                        2 -> "Medium"
                        else -> "Low"
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                task.dueDateIso?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Due: $it", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(4.dp))
                Text("🍅 ${task.completedPomodoros}/${task.pomodoroCount}")
            }

            Checkbox(task.isCompleted, { onToggleComplete() })
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTaskDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf(30) }
    var priority by rememberSaveable { mutableStateOf(2) }
    var dueDate by rememberSaveable { mutableStateOf<String?>(null) }
    var isFlexible by rememberSaveable { mutableStateOf(true) }

    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(showPicker) {
        if (showPicker) {
            val today = LocalDate.now()
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    dueDate = LocalDate.of(y, m + 1, d)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
                },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            ).apply {
                setOnDismissListener { showPicker = false }
                show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") })
                OutlinedTextField(description, { description = it }, label = { Text("Description") })
                OutlinedTextField(
                    duration.toString(),
                    { it.toIntOrNull()?.let { duration = it } },
                    label = { Text("Duration (minutes)") }
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showPicker = true }
                        .padding(8.dp)
                ) {
                    Text(dueDate ?: "Due date", Modifier.weight(1f))
                    Icon(Icons.Default.Add, contentDescription = null)
                }

                Text("Priority:")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("High" to 3, "Medium" to 2, "Low" to 1).forEach { (label, value) ->
                        RadioButtonWithLabel(
                            selected = (priority == value),
                            label = label,
                            onSelect = { priority = value }
                        )
                    }
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(isFlexible, { isFlexible = it })
                    Text("Flexible scheduling")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Task(
                        id = 0,
                        title = title,
                        description = description.ifBlank { null },
                        durationMinutes = duration,
                        dueDateIso = dueDate,
                        priority = priority,
                        isFlexible = isFlexible
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RadioButtonWithLabel(
    selected: Boolean,
    label: String,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onSelect() }
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(label)
    }
}

