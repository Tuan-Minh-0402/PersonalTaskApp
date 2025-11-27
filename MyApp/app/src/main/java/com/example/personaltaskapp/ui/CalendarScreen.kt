package com.example.personaltaskapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personaltaskapp.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val events by viewModel.events.collectAsState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- Month Navigation Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Text("← Prev")
            }

            Text(
                text = currentMonth.month.toString() + " " + currentMonth.year,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Text("Next →")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Days Grid ---
        CalendarGrid(
            month = currentMonth,
            events = events,
            selectedDate = selectedDate,
            onDateClick = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Events for selected day ---
        val selectedDayEvents = events.filter {
            val eventDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(it.startTime))
            eventDate == selectedDate.format(formatter)
        }

        Text(
            text = "Events on ${selectedDate.format(formatter)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (selectedDayEvents.isEmpty()) {
            Text("No events today", color = Color.Gray)
        } else {
            LazyColumn {
                items(selectedDayEvents) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF4FF))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(event.title, fontWeight = FontWeight.Bold)
                            event.description?.let { Text(it) }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarGrid(
    month: YearMonth,
    events: List<com.example.personaltaskapp.model.CalendarEvent>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        val days = (1..daysInMonth).toList()
        var week = mutableListOf<@Composable () -> Unit>()

        for (i in 0 until firstDayOfWeek) {
            week.add { Box(modifier = Modifier.weight(1f).height(40.dp)) }
        }

        days.forEach { day ->
            val date = month.atDay(day)
            val hasEvent = events.any {
                val eventDate = formatter.format(Date(it.startTime))
                eventDate == date.toString()
            }

            week.add {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable { onDateClick(date) }
                        .background(
                            when {
                                date == selectedDate -> Color(0xFFBBDEFB)
                                hasEvent -> Color(0xFFE3F2FD)
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day.toString())
                }
            }

            if (week.size == 7) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { it() }
                }
                week.clear()
            }
        }

        if (week.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { it() }
            }
        }
    }
}
