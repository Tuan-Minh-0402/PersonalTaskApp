package com.example.personaltaskapp.ui

import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.personaltaskapp.model.CalendarEvent
import com.example.personaltaskapp.model.Habit
import com.example.personaltaskapp.model.Task
import com.example.personaltaskapp.scheduler.SmartSuggestion
import com.example.personaltaskapp.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // LIVE COLLECTION OF FLOWS — required for correct recomposition
    val allTasks by viewModel.allTasks.collectAsState()
    val allHabits by viewModel.allHabits.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()

    // Always re-evaluate based on selected date
    val tasksForDate = viewModel.tasksFor(selectedDate)
    val habitsForDate = viewModel.habitsFor(selectedDate)
    val eventsForDate = viewModel.eventsFor(selectedDate)
    val suggestionsForDate = viewModel.smartSuggestions(selectedDate)

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
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                            + " " + currentMonth.year,
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
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach {
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
                viewModel = viewModel
            )
        }

        // ADD EVENT BUTTON
        FloatingActionButton(
            onClick = { showAddEventDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add event")
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
                suggestions = suggestionsForDate,
                onApplySuggestion = { suggestion ->
                    viewModel.applySmartSuggestion(suggestion, selectedDate)
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
            onDismiss = { showAddEventDialog = false },
            onSave = { title, desc, dateIso, timeIso ->
                viewModel.addCalendarEvent(title, desc, dateIso, timeIso, "GENERAL")
                showAddEventDialog = false
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    viewModel: CalendarViewModel
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

            val events = viewModel.eventsFor(date)
            val tasks = viewModel.tasksFor(date)
            val habits = viewModel.habitsFor(date)

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarBottomSheetContent(
    date: LocalDate,
    events: List<CalendarEvent>,
    tasks: List<Task>,
    habits: List<Habit>,
    suggestions: List<SmartSuggestion>,
    onApplySuggestion: (SmartSuggestion) -> Unit
) {
    Column(Modifier.padding(16.dp)) {

        Text(
            "Details for ${date.dayOfMonth}-${date.monthValue}-${date.year}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(10.dp))

        if (events.isNotEmpty()) {
            Text("Events:", style = MaterialTheme.typography.titleSmall)
            events.forEach { Text("• ${it.title}") }
            Spacer(Modifier.height(8.dp))
        }

        if (tasks.isNotEmpty()) {
            Text("Tasks:", style = MaterialTheme.typography.titleSmall)
            tasks.forEach { Text("• ${it.title}") }
            Spacer(Modifier.height(8.dp))
        }

        if (habits.isNotEmpty()) {
            Text("Habits:", style = MaterialTheme.typography.titleSmall)
            habits.forEach { Text("• ${it.title}") }
            Spacer(Modifier.height(8.dp))
        }

        if (date >= LocalDate.now() && suggestions.isNotEmpty()) {
            Text("Suggestions:", style = MaterialTheme.typography.titleSmall, color = Color.Blue)

            suggestions.forEach { s ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5)
                    )
                    Spacer(Modifier.width(8.dp))

                    Column(Modifier.weight(1f)) {
                        Text(s.title, style = MaterialTheme.typography.bodyMedium)
                        Text("(recommended)", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = { onApplySuggestion(s) },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AddEventDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, String?, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var timeIso by remember { mutableStateOf("08:00") }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Event") },
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(title, description, date.toString(), timeIso)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
