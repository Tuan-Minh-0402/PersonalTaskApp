package com.example.personaltaskapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import androidx.compose.foundation.layout.width
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel
) {
    val context = LocalContext.current
    val allTasks by viewModel.tasks.collectAsState(initial = emptyList())

    // Filters
    var filterActive by rememberSaveable { mutableStateOf(false) }
    var filterDone by rememberSaveable { mutableStateOf(false) }
    var filterDueSoon by rememberSaveable { mutableStateOf(false) }

    val isAllSelected = !filterActive && !filterDone && !filterDueSoon
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    // Pomodoro dialog flags
    var showPomodoroSelector by rememberSaveable { mutableStateOf(false) }
    var showPomodoroRunning by rememberSaveable { mutableStateOf(false) }

    // --- Pomodoro state collectors ---
    val secondsLeft by viewModel.pomodoro.secondsLeft.collectAsState()
    val isPomodoroRunning by viewModel.pomodoro.isRunning.collectAsState()


    // Filtering
    val filteredTasks = remember(allTasks, filterActive, filterDone, filterDueSoon) {
        if (isAllSelected) allTasks else {
            allTasks.filter { t ->
                var include = false

                if (filterActive && !t.isCompleted) include = true
                if (filterDone && t.isCompleted) include = true

                if (filterDueSoon && !t.isCompleted && t.dueDateIso != null) {
                    val due = runCatching { LocalDate.parse(t.dueDateIso) }.getOrNull()
                    if (due != null && due >= LocalDate.now() && due <= LocalDate.now().plusDays(7)) {
                        include = true
                    }
                }

                include
            }
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
                FloatingActionButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
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

            // ---- Filter buttons ----
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterButton("All", isAllSelected) {
                    filterActive = false; filterDone = false; filterDueSoon = false
                }
                FilterButton("Active", filterActive) { filterActive = !filterActive }
                FilterButton("Done", filterDone) { filterDone = !filterDone }
                FilterButton("Due soon", filterDueSoon) { filterDueSoon = !filterDueSoon }

                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))

            // ---- Task list ----
            if (filteredTasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredTasks.forEach { task ->
                        TaskRow(
                            task = task,
                            onToggleComplete = {
                                viewModel.updateTask(task.copy(isCompleted = !task.isCompleted))
                            },
                            onEdit = { editingTask = task },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }

        // ---- Add Task Dialog ----
        if (showAddDialog || editingTask != null) {
            AddTaskDialog(
                context = context,
                initialTask = editingTask,
                onDismiss = {
                    showAddDialog = false
                    editingTask = null
                },
                onSave = { savedTask ->
                    if (editingTask == null) viewModel.addTask(savedTask) else viewModel.updateTask(savedTask)
                    showAddDialog = false
                    editingTask = null
                }
            )
        }

        // ---- Pomodoro Selector ----
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

// ---- Pomodoro Running ----
        if (showPomodoroRunning && isPomodoroRunning) {
            PomodoroRunningDialog(
                secondsLeft = secondsLeft,
                onCancel = {
                    viewModel.pomodoro.cancel()
                    showPomodoroRunning = false
                }
            )
        }

    }
}

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

@Composable
fun TaskRow(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {

                Text(task.title, style = MaterialTheme.typography.titleMedium)

                if (!task.description.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(task.description, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Priority: " + when (task.priority) {
                        3 -> "High"
                        2 -> "Medium"
                        else -> "Low"
                    }
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
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
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

@Composable
fun AddTaskDialog(
    context: Context,
    initialTask: Task? = null,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.title ?: "") }
    var description by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.description ?: "") }
    var durationInput by rememberSaveable(initialTask?.id) { mutableStateOf((initialTask?.estimatedMinutes ?: 30).toString()) }
    var priority by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.priority ?: 2) }
    var dueDate by rememberSaveable(initialTask?.id) { mutableStateOf<String?>(initialTask?.dueDateIso) }
    var isFlexible by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.isFlexible ?: true) }
    var selectedTime by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.fixedStartIso?.takeLast(5)) }

    var showPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // ---- Date Picker ----
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

    // ---- Time Picker ----
    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            val initialHour = selectedTime?.substring(0, 2)?.toIntOrNull() ?: 8
            val initialMinute = selectedTime?.substring(3, 5)?.toIntOrNull() ?: 0
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    selectedTime = "%02d:%02d".format(hour, minute)
                },
                initialHour,
                initialMinute,
                true
            ).apply {
                setOnDismissListener { showTimePicker = false }
                show()
            }
        }
    }

    // If flexible scheduling is ON, time must not be saved.
    LaunchedEffect(isFlexible) {
        if (isFlexible) selectedTime = null
    }

    val durationMinutes = durationInput.toIntOrNull() ?: 0

    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.95f),
        onDismissRequest = onDismiss,
        title = { Text(if (initialTask == null) "Add Task" else "Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationInput,
                    onValueChange = { durationInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duration (minutes)") },
                    isError = durationInput.isNotBlank() && durationMinutes <= 0,
                    modifier = Modifier.fillMaxWidth()
                )

                // Due date row
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("High" to 3, "Medium" to 2, "Low" to 1).forEach { (label, value) ->
                        RadioButtonWithLabel(
                            selected = priority == value,
                            label = label,
                            onSelect = { priority = value },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(isFlexible, { isFlexible = it })
                    Text("Flexible scheduling")
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (isFlexible) Modifier
                            else Modifier.clickable { showTimePicker = true }
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isFlexible) "Time (disabled when flexible)"
                        else (selectedTime ?: "Time"),
                        modifier = Modifier.weight(1f),
                        color = if (isFlexible) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank() || durationMinutes <= 0) return@TextButton

                val fixedStartIso = if (!isFlexible && !selectedTime.isNullOrBlank() && !dueDate.isNullOrBlank()) {
                    "${dueDate}T$selectedTime"
                } else {
                    null
                }
                onSave(
                    Task(
                        id = initialTask?.id ?: 0,
                        title = title,
                        description = description.ifBlank { null },
                        isCompleted = initialTask?.isCompleted ?: false,
                        estimatedMinutes = durationMinutes,
                        earliestStartIso = initialTask?.earliestStartIso,
                        latestEndIso = initialTask?.latestEndIso,
                        mustFinishToday = initialTask?.mustFinishToday ?: false,
                        minSessionMinutes = initialTask?.minSessionMinutes ?: 25,
                        maxSessionMinutes = initialTask?.maxSessionMinutes ?: 60,
                        dueDateIso = dueDate,
                        priority = priority,
                        isFlexible = isFlexible,
                        fixedStartIso = fixedStartIso,
                        completedMinutes = initialTask?.completedMinutes ?: 0,
                        pomodoroCount = initialTask?.pomodoroCount ?: 4,
                        completedPomodoros = initialTask?.completedPomodoros ?: 0
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
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onSelect() }
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, maxLines = 1)
    }
}