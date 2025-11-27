package com.example.personaltaskapp.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.personaltaskapp.data.DatabaseModule
import com.example.personaltaskapp.repository.*
import com.example.personaltaskapp.viewmodel.*

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) get DB
        val db = DatabaseModule.getDatabase(this)

        // 2) create repos
        val taskRepo = TaskRepository(db.taskDao())
        val habitRepo = HabitRepository(db.habitDao())
        val calendarRepo = CalendarRepository(db.calendarEventDao())

        // 3) viewmodels via factories
        val taskViewModel = ViewModelProvider(
            this,
            TaskViewModelFactory(taskRepo)
        ).get(TaskViewModel::class.java)

        val habitViewModel = ViewModelProvider(
            this,
            HabitViewModelFactory(habitRepo)
        ).get(HabitViewModel::class.java)

        val calendarViewModel = ViewModelProvider(
            this,
            CalendarViewModelFactory(calendarRepo)
        ).get(CalendarViewModel::class.java)

        // 4) set compose content and pass them in
        setContent {
            // optionally wrap in your theme if you have one
            MaterialTheme {
                MainApp(
                    taskViewModel = taskViewModel,
                    habitViewModel = habitViewModel,
                    calendarViewModel = calendarViewModel
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(
    taskViewModel: TaskViewModel,
    habitViewModel: HabitViewModel,
    calendarViewModel: CalendarViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, "Tasks") },
                    label = { Text("Tasks") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.FitnessCenter, "Habits") },
                    label = { Text("Habits") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.DateRange, "Calendar") },
                    label = { Text("Calendar") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> TaskScreen(taskViewModel)
                1 -> HabitScreen(habitViewModel)
                2 -> CalendarScreen(calendarViewModel)
            }
        }
    }
}
