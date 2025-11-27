package com.example.personaltaskapp.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaltaskapp.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(calendarViewModel: CalendarViewModel = viewModel()) {
    // Track visible month and selected date
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Calendar") }) }) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            MonthHeader(visibleMonth, onPrev = { visibleMonth = visibleMonth.minusMonths(1) }, onNext = { visibleMonth = visibleMonth.plusMonths(1) })
            Spacer(Modifier.height(8.dp))
            MonthGrid(visibleMonth, onDayClick = { date ->
                selectedDate = date
                coroutineScope.launch { sheetState.show() }
            })
        }

        // Bottom sheet with day details
        ModalBottomSheet(
            onDismissRequest = { LaunchedEffect(Unit) { /* handled by sheetState */ } },
            sheetState = sheetState,
        ) {
            BottomSheetContent(
                date = selectedDate,
                calendarViewModel = calendarViewModel,
                onScheduleTask = { task, date -> calendarViewModel.scheduleTaskOn(task, date) },
                onMarkHabitDone = { habit, date -> calendarViewModel.markHabitDone(habit, date) },
                onAddEvent = { title, date -> calendarViewModel.addCalendarEvent(title, date) }
            )
        }
    }
}

@Composable
fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("<", modifier = Modifier.clickable { onPrev() }.padding(8.dp))
        Text("${month.month.name} ${month.year}", style = MaterialTheme.typography.titleLarge)
        Text(">", modifier = Modifier.clickable { onNext() }.padding(8.dp))
    }
}

@Composable
fun MonthGrid(month: YearMonth, onDayClick: (LocalDate) -> Unit) {
    val firstDay = month.atDay(1)
    val startOffset = (firstDay.dayOfWeek.value % 7) // Sunday = 0, Monday = 1 -> adjust so grid shows Sun..Sat or Mon..Sun per your preference
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }

    // build a padded list to align days
    val cells = List(startOffset) { LocalDate.MIN } + days

    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth().height(360.dp)) {
        items(cells) { day ->
            if (day == LocalDate.MIN) {
                Box(Modifier.size(48.dp)) {}
            } else {
                Box(modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
                    .clickable { onDayClick(day) }
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day.dayOfMonth.toString())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContent(
    date: LocalDate,
    calendarViewModel: CalendarViewModel,
    onScheduleTask: (com.example.personaltaskapp.model.Task, LocalDate) -> Unit,
    onMarkHabitDone: (com.example.personaltaskapp.model.Habit, LocalDate) -> Unit,
    onAddEvent: (String, LocalDate) -> Unit
) {
    val events = remember(date, calendarViewModel) { calendarViewModel.eventsFor(date) }
    val tasks = remember(date, calendarViewModel) { calendarViewModel.tasksFor(date) }
    val habits = remember(date, calendarViewModel) { calendarViewModel.habitsFor(date) }
    val suggestions = remember(date, calendarViewModel) { calendarViewModel.suggestionsFor(date) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Details for ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}", style = MaterialTheme.typography.titleLarge)

        if (events.isNotEmpty()) {
            Text("Events", style = MaterialTheme.typography.titleMedium)
            events.forEach { ev -> Text("- ${ev.title}") }
        }

        if (tasks.isNotEmpty()) {
            Text("Tasks", style = MaterialTheme.typography.titleMedium)
            tasks.forEach { t ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(t.title)
                    Button(onClick = { onScheduleTask(t, date) }) { Text("Schedule") }
                }
            }
        }

        if (habits.isNotEmpty()) {
            Text("Habits", style = MaterialTheme.typography.titleMedium)
            habits.forEach { h ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(h.title)
                    Button(onClick = { onMarkHabitDone(h, date) }) { Text("Done") }
                }
            }
        }

        if (suggestions.isNotEmpty()) {
            Text("Suggestions", style = MaterialTheme.typography.titleMedium)
            suggestions.forEach { s ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(s.title)
                        Button(onClick = {
                            // scheduling callback: load task from VM store by id and schedule it
                            val maybe = calendarViewModel.allTasks.value.find { it.id == s.taskId }
                            if (maybe != null) onScheduleTask(maybe, LocalDate.parse(s.suggestedDateIso))
                        }) {
                            Text("Apply")
                        }
                    }
                }
            }
        }

        // quick add event
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            var title by remember { mutableStateOf("") }
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event title") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (title.isNotBlank()) {
                    onAddEvent(title, date)
                    title = ""
                }
            }) { Text("Add") }
        }
    }
}
