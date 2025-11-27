package com.example.personaltaskapp.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
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

    // Collect tasks (single source of truth)
    val allTasks by viewModel.tasks.collectAsState(initial = emptyList())

    // Filter states
    var filterActive by rememberSaveable { mutableStateOf(false) }     // incomplete
    var filterDone by rememberSaveable { mutableStateOf(false) }       // completed
    var filterDueSoon by rememberSaveable { mutableStateOf(false) }    // due in next 7 days

    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    // “All” is automatically selected when nothing else is selected
    val isAllSelected = !filterActive && !filterDone && !filterDueSoon

    // ---- APPLY FILTERING ----
    val filteredTasks = remember(allTasks, filterActive, filterDone, filterDueSoon) {

        // If no filters ON → show everything
        if (!filterActive && !filterDone && !filterDueSoon) {
            allTasks
        } else {

            allTasks.filter { task ->

                var match = false

                // ACTIVE filter (incomplete tasks)
                if (filterActive && !task.isCompleted) {
                    match = true
                }

                // DONE filter
                if (filterDone && task.isCompleted) {
                    match = true
                }

                // DUE SOON filter (only for incomplete tasks)
                if (filterDueSoon && !task.isCompleted && task.dueDateIso != null) {
                    try {
                        val due = LocalDate.parse(task.dueDateIso)
                        if (due >= LocalDate.now() &&
                            due <= LocalDate.now().plusDays(7)
                        ) {
                            match = true
                        }
                    } catch (_: Exception) {}
                }

                match
            }
        }
    }

    var showPomodoroSelector by rememberSaveable { mutableStateOf(false) }
    var showPomodoroRunning by rememberSaveable { mutableStateOf(false) }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Tasks") }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Pomodoro FAB
                FloatingActionButton(
                    onClick = { showPomodoroSelector = true },
                    containerColor = Color(0xFFD46AFF)
                ) {
                    Text("🍅", fontSize = 20.sp)
                }

                // Add Task FAB
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->

        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {

            // ------------------- FILTER BUTTONS -------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ALL
                FilterButton(
                    text = "All",
                    selected = isAllSelected
                ) {
                    filterActive = false
                    filterDone = false
                    filterDueSoon = false
                }

                // ACTIVE
                FilterButton(
                    text = "Active",
                    selected = filterActive
                ) {
                    filterActive = !filterActive
                }

                // DONE
                FilterButton(
                    text = "Done",
                    selected = filterDone
                ) {
                    filterDone = !filterDone
                }

                // DUE SOON
                FilterButton(
                    text = "Due soon",
                    selected = filterDueSoon
                ) {
                    filterDueSoon = !filterDueSoon
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ------------------- TASK LIST -------------------
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredTasks.forEach { task ->
                        TaskRow(
                            task = task,
                            onToggleComplete = {
                                viewModel.updateTask(task.copy(isCompleted = !task.isCompleted))
                            }
                        )
                    }
                }
            }
        }

        // ------------------- ADD TASK DIALOG -------------------
        if (showAddDialog) {
            AddTaskDialog(
                context = context,
                onDismiss = { showAddDialog = false },
                onSave = { newTask ->
                    viewModel.addTask(newTask)
                    showAddDialog = false
                }
            )
        }

        // ─────────────────────────────────────────────
// Pomodoro Task Selector
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// Pomodoro Running Dialog
// ─────────────────────────────────────────────
        if (showPomodoroRunning && viewModel.pomodoro.isRunning) {
            PomodoroRunningDialog(
                state = viewModel.pomodoro.state,
                secondsLeft = viewModel.pomodoro.secondsLeft,
                onCancel = {
                    viewModel.pomodoro.cancel()
                    showPomodoroRunning = false
                }
            )
        }
    }

}

@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun TaskRow(task: Task, onToggleComplete: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ───────────────────────────────
            // LEFT SIDE — TASK INFO
            // ───────────────────────────────
            Column(modifier = Modifier.weight(1f)) {

                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )

                // Description (optional)
                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description!!,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Priority
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Priority: " + when (task.priority) {
                        3 -> "High"
                        2 -> "Medium"
                        else -> "Low"
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                // Due date
                task.dueDateIso?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Due: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Pomodoro progress
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🍅 ${task.completedPomodoros} / ${task.pomodoroCount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ───────────────────────────────
            // RIGHT SIDE — CHECKBOX
            // ───────────────────────────────
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
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
    // Field states
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var durationMinutes by rememberSaveable { mutableStateOf(30) }
    var dueDateText by rememberSaveable { mutableStateOf<String?>(null) } // displayed text in yyyy-MM-dd
    var priority by rememberSaveable { mutableStateOf(2) } // default medium (1=low,2=medium,3=high)
    var isFlexible by rememberSaveable { mutableStateOf(true) }

    // date picker toggle
    var showDatePicker by remember { mutableStateOf(false) }

    // When showDatePicker flips true, create & show a new DatePickerDialog
    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            try {
                val today = LocalDate.now()
                val dlg = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        // month is 0-based in the DatePickerDialog callback
                        val picked = LocalDate.of(year, month + 1, dayOfMonth)
                        dueDateText = picked.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    },
                    today.year,
                    today.monthValue - 1,
                    today.dayOfMonth
                )
                dlg.setOnDismissListener { showDatePicker = false }
                dlg.show()
            } finally {
                // ensure we reset the flag if something fails (LaunchedEffect will re-run only when flag changes)
                // Do not reset it here immediately; DatePickerDialog's onDismiss will reset. But as a safety:
                // if it's still true here after a short moment, it will be set false by the onDismiss above.
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = durationMinutes.toString(),
                    onValueChange = {
                        val int = it.toIntOrNull()
                        if (int != null) durationMinutes = int
                    },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )


                // Due date field (click to open date picker)
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = dueDateText ?: "Due date", modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Add, // any icon
                            contentDescription = "Pick date"
                        )
                    }
                }

                // Priority radio buttons
                Text("Priority:")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("High" to 3, "Medium" to 2, "Low" to 1).forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { priority = value }
                        ) {
                            RadioButton(selected = priority == value, onClick = { priority = value })
                            Text(label)
                        }
                    }
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFlexible, onCheckedChange = { isFlexible = it })
                    Text("Flexible scheduling")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Build Task object. id=0 (DB should assign), keep iso string for dueDate
                val task = Task(
                    id = 0,
                    title = title.ifBlank { "Untitled" },
                    description = description.ifBlank { null },
                    isCompleted = false,
                    durationMinutes = durationMinutes,
                    earliestStartIso = null,
                    dueDateIso = dueDateText,
                    priority = priority,
                    fixedStartIso = null,
                    isFlexible = isFlexible,
                    pomodoroCount = 4,
                    completedPomodoros = 0,
                    isPomodoroRunning = false
                )
                onSave(task)
            }) {
                Text("Save")
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
private fun RadioButtonWithLabel(selected: Boolean, label: String, onSelect: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onSelect() }) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label)
    }
}
