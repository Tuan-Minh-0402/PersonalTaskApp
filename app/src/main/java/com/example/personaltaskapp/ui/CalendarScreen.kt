package com.example.personaltaskapp.ui

import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.personaltaskapp.model.CalendarEvent
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.scheduler.SmartSuggestion
import com.example.personaltaskapp.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    // LIVE COLLECTION OF FLOWS — required for correct recomposition
    val allTasks by viewModel.allTasks.collectAsState()
    val allHabits by viewModel.allHabits.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()

    // Always re-evaluate based on selected date
    val tasksForDate = remember(selectedDate, allTasks) {
        allTasks.filter { task ->
            val key = selectedDate.toString()
            val fixed = task.fixedStartIso?.take(10) == key
            val earliest = task.earliestStartIso?.take(10) == key
            fixed || earliest
        }
    }

    val tasksByDate = remember(allTasks) {
        allTasks.groupBy { task -> taskDate(task) }.filterKeys { it != null }.mapKeys { it.key!! }
    }
    val habitsByDate = remember(allHabits) {
        { date: LocalDate ->
            val dow3 = date.dayOfWeek.name.take(3).uppercase()
            allHabits.filter { habit ->
                val freq = habit.frequency.uppercase()
                freq == "DAILY" ||
                        (freq == "WEEKDAYS" && dow3 in listOf("MON", "TUE", "WED", "THU", "FRI")) ||
                        (freq == "WEEKENDS" && dow3 in listOf("SAT", "SUN")) ||
                        (freq.isNotBlank() && freq.split(",").map { it.trim() }.contains(dow3))
            }
        }
    }
    val habitsForDate = remember(selectedDate, allHabits) { habitsByDate(selectedDate) }
    val eventsByDate = remember(allEvents) {
        { date: LocalDate -> allEvents.filter { it.dateIso.take(10) == date.toString() } }
    }
    val eventsForDate = remember(selectedDate, allEvents) { eventsByDate(selectedDate) }
    val rawSuggestionsForDate = viewModel.smartSuggestions(selectedDate)
    val suggestionItemsForDate = rawSuggestionsForDate.map { suggestion ->
        suggestion.toUiSuggestion(allTasks)
    }

    Box(Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            // --------------------------------
            // MONTH HEADER (ARROWS)
            // --------------------------------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
                }

                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                            " " + currentMonth.year,
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                }
            }

            Spacer(Modifier.height(8.dp))

            // --------------------------------
            // WEEKDAY HEADER (Mon-Sun)
            // --------------------------------
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // --------------------------------
            // CALENDAR GRID
            // --------------------------------
            CalendarMonthGrid(
                month = currentMonth,
                selectedDate = selectedDate,
                onSelect = {
                    selectedDate = it
                    showSheet = true
                },
                tasksByDate = tasksByDate,
                eventsByDate = eventsByDate,
                habitsByDate = habitsByDate
            )
        }

    }

    // --------------------------------
    // BOTTOM SHEET
    // --------------------------------
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = bottomSheetState
        ) {
            CalendarBottomSheetContent(
                date = selectedDate,
                events = eventsForDate,
                tasks = tasksForDate,
                habits = habitsForDate,
                suggestions = suggestionItemsForDate,
                onAddEvent = { showAddEventDialog = true },
                onEditEvent = { event ->
                    editingEvent = event
                    showAddEventDialog = true
                },
                onDeleteEvent = { event -> viewModel.deleteCalendarEvent(event) },
                onApplySuggestion = { suggestionItem ->
                    val originalSuggestion = rawSuggestionsForDate.firstOrNull {
                        it.taskId == suggestionItem.taskId &&
                                it.suggestedStartIso == suggestionItem.suggestedStartIso &&
                                it.suggestedEndIso == suggestionItem.suggestedEndIso
                    }
                    if (originalSuggestion != null) {
                        viewModel.applySmartSuggestion(originalSuggestion, selectedDate)
                    }
                },
                onTaskCheckedChange = { taskId, checked ->
                    viewModel.updateTaskCompletion(taskId = taskId, isCompleted = checked)
                },
                isHabitCompleted = { habitId, day ->
                    val habit = allHabits.firstOrNull { it.id == habitId }
                    habit?.let { viewModel.isHabitCompletedOnDate(it, day) } ?: false
                },
                onHabitCheckedChange = { habitId, day, checked ->
                    viewModel.updateHabitCompletionForDate(habitId, day, checked)
                }
            )
        }
    }

    // --------------------------------
    // ADD-EVENT DIALOG
    // --------------------------------
    if (showAddEventDialog) {
        AddEventDialog(
            date = selectedDate,
            initialEvent = editingEvent,
            onDismiss = {
                showAddEventDialog = false
                editingEvent = null
            },
            onSave = { title, desc, dateIso, timeIso, durationMinutes ->
                val editing = editingEvent
                if (editing == null) {
                    viewModel.addCalendarEvent(title, desc, dateIso, timeIso, durationMinutes, "GENERAL")
                } else {
                    viewModel.updateCalendarEvent(
                        editing.copy(
                            title = title,
                            description = desc,
                            dateIso = dateIso,
                            startTimeIso = timeIso,
                            durationMinutes = durationMinutes
                        )
                    )
                }
                showAddEventDialog = false
                editingEvent = null
            }
        )
    }
}

@Composable
fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    tasksByDate: Map<LocalDate, List<Task>>,
    eventsByDate: (LocalDate) -> List<CalendarEvent>,
    habitsByDate: (LocalDate) -> List<Habit>
) {
    val firstDay = month.atDay(1)
    val lastDay = month.atEndOfMonth()

    // Correct Monday-based offset
    val firstDayOfWeek = (firstDay.dayOfWeek.value - 1) // Mon=0 .. Sun=6
    val totalCells = firstDayOfWeek + lastDay.dayOfMonth

    val cells = (0 until totalCells).map { i ->
        if (i < firstDayOfWeek) null else month.atDay(i - firstDayOfWeek + 1)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(cells.size) { index ->
            val date = cells[index]

            if (date == null) {
                Box(Modifier.size(36.dp)) {}
                return@items
            }

            val events = eventsByDate(date)
            val tasks = tasksByDate[date].orEmpty()
            val habits = habitsByDate(date)

            val isSelected = date == selectedDate
            val isFutureOrToday = !date.isBefore(LocalDate.now())

            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(3.dp)
                    .clickable { onSelect(date) },
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (tasks.isNotEmpty()) {
                            Dot(MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(3.dp))
                        }
                        if (habits.isNotEmpty() && isFutureOrToday) {
                            Dot(MaterialTheme.colorScheme.tertiary)
                            Spacer(Modifier.width(3.dp))
                        }
                        if (events.isNotEmpty()) {
                            Dot(MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(color, CircleShape)
    )
}

@Composable
fun CalendarBottomSheetContent(
    date: LocalDate,
    events: List<CalendarEvent>,
    tasks: List<Task>,
    habits: List<Habit>,
    suggestions: List<CalendarSuggestionUi>,
    onAddEvent: () -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onApplySuggestion: (CalendarSuggestionUi) -> Unit,
    onTaskCheckedChange: (taskId: Int, isCompleted: Boolean) -> Unit,
    isHabitCompleted: (habitId: Int, date: LocalDate) -> Boolean,
    onHabitCheckedChange: (habitId: Int, date: LocalDate, isCompleted: Boolean) -> Unit
) {
    val visibleSuggestions = if (date >= LocalDate.now()) suggestions else emptyList()
    val dateText = "${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.dayOfMonth}"
    val summaryText = "${tasks.size} Tasks • ${habits.size} Habits • ${visibleSuggestions.size} Suggestions"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(text = dateText, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAddEvent) {
            Text("Add Event")
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text(text = "Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (tasks.isEmpty()) {
            Text("No tasks for this day", style = MaterialTheme.typography.bodyMedium)
        } else {
            tasks.forEach { task ->
                val taskTime = extractTaskTime(task)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { checked ->
                            onTaskCheckedChange(task.id, checked)
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$taskTime ${task.title}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(text = "Habits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (habits.isEmpty()) {
            Text("No habits for this day", style = MaterialTheme.typography.bodyMedium)
        } else {
            habits.forEach { habit ->
                val checked = isHabitCompleted(habit.id, date)
                val monthStreak = habit.completedDatesIsoCsv
                    .split(",")
                    .map { it.trim() }
                    .filter { it.startsWith("${date.year}-%02d".format(date.monthValue)) }
                    .distinct()
                    .count()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    onHabitCheckedChange(habit.id, date, it)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            val durationSuffix = if (habit.durationMinutes > 0) {
                                " (${habit.durationMinutes}m)"
                            } else {
                                ""
                            }
                            Text(
                                text = "${formatMinutesAsTime(habit.startMinutes)} ${habit.title}$durationSuffix",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "🔥 Streak: $monthStreak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Events",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        if (events.isEmpty()) {
            Text("No events for this day", style = MaterialTheme.typography.bodyMedium)
        } else {
            events.forEach { event ->
                val eventTime = extractTimeFromText(event.startTimeIso) ?: "--:--"
                val eventDuration = event.durationMinutes.coerceAtLeast(1)
                Text(
                    text = "• $eventTime ${event.title} (${eventDuration}m)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onEditEvent(event) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { onDeleteEvent(event) }
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "AI Suggestions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        if (visibleSuggestions.isEmpty()) {
            Text("No suggestions for this day", style = MaterialTheme.typography.bodyMedium)
        } else {
            visibleSuggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "✨ ${suggestion.title}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        val scheduleText = formatSuggestionTimeRange(
                            suggestion.suggestedStartIso,
                            suggestion.suggestedEndIso
                        )
                        if (scheduleText != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = scheduleText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (suggestion.displayReason.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Reason: ${suggestion.displayReason}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { onApplySuggestion(suggestion) }) {
                            Text("Apply")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

data class CalendarSuggestionUi(
    val taskId: Int,
    val title: String,
    val displayReason: String,
    val suggestedStartIso: String?,
    val suggestedEndIso: String?
)

private fun SmartSuggestion.toUiSuggestion(tasks: List<Task>): CalendarSuggestionUi {
    val fallbackTitle = tasks.firstOrNull { it.id == taskId }?.title ?: "Task #$taskId"
    val uiTitle = if (title.isBlank()) fallbackTitle else title

    return CalendarSuggestionUi(
        taskId = taskId,
        title = uiTitle,
        displayReason = reason,
        suggestedStartIso = suggestedStartIso,
        suggestedEndIso = suggestedEndIso
    )
}

private fun extractTaskTime(task: Task): String {
    return extractTimeFromText(task.fixedStartIso)
        ?: extractTimeFromText(task.earliestStartIso)
        ?: "--:--"
}

@RequiresApi(Build.VERSION_CODES.O)
private fun taskDate(task: Task): LocalDate? {
    return parseIsoDate(task.fixedStartIso)
        ?: parseIsoDate(task.earliestStartIso)
        ?: parseIsoDate(task.dueDateIso)
}

private fun formatMinutesAsTime(totalMinutes: Int): String {
    if (totalMinutes < 0) return "--:--"
    val hour = (totalMinutes / 60).coerceAtLeast(0)
    val minute = (totalMinutes % 60).coerceAtLeast(0)
    return "%02d:%02d".format(hour, minute)
}

private fun extractTimeFromText(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val match = Regex("(\\d{2}:\\d{2})").find(raw)
    return match?.groupValues?.get(1)
}

private fun formatSuggestionTimeRange(startIso: String?, endIso: String?): String? {
    if (startIso.isNullOrBlank() || endIso.isNullOrBlank()) return null
    val start = runCatching { java.time.LocalDateTime.parse(startIso) }.getOrNull() ?: return null
    val end = runCatching { java.time.LocalDateTime.parse(endIso) }.getOrNull() ?: return null
    return "%02d:%02d - %02d:%02d".format(
        start.hour, start.minute, end.hour, end.minute
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseIsoDate(raw: String?): LocalDate? {
    if (raw.isNullOrBlank()) return null
    val datePart = raw.trim().take(10)
    return try {
        LocalDate.parse(datePart)
    } catch (_: DateTimeParseException) {
        null
    }
}

@Composable
fun AddEventDialog(
    date: LocalDate,
    initialEvent: CalendarEvent? = null,
    onDismiss: () -> Unit,
    onSave: (String, String?, String, String, Int) -> Unit
) {
    var title by remember(initialEvent?.id) { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember(initialEvent?.id) { mutableStateOf(initialEvent?.description ?: "") }

    var timeIso by remember(initialEvent?.id) { mutableStateOf(initialEvent?.startTimeIso ?: "08:00") }
    var durationText by remember(initialEvent?.id) { mutableStateOf((initialEvent?.durationMinutes ?: 60).toString()) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEvent == null) "Add Event" else "Edit Event") },
        text = {

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(title, { title = it }, label = { Text("Title") })
                OutlinedTextField(description, { description = it }, label = { Text("Description") })

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val hour = timeIso.substring(0, 2).toInt()
                            val minute = timeIso.substring(3, 5).toInt()

                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    timeIso = "%02d:%02d".format(h, m)
                                },
                                hour,
                                minute,
                                true
                            ).show()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Start time")
                    Text(timeIso, style = MaterialTheme.typography.bodyMedium)
                }

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { input ->
                        durationText = input.filter { it.isDigit() }.take(4)
                    },
                    label = { Text("Duration (minutes)") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val durationMinutes = durationText.toIntOrNull()?.coerceAtLeast(1) ?: 60
                onSave(title, description, (initialEvent?.dateIso ?: date.toString()), timeIso, durationMinutes)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}