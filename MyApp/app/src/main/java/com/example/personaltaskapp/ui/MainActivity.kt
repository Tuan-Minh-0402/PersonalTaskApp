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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.personaltaskapp.viewmodel.TaskViewModel
import com.example.personaltaskapp.viewmodel.CalendarViewModel
import com.example.personaltaskapp.viewmodel.HabitViewModel
import com.example.personaltaskapp.data.HabitDatabaseHelper
import com.example.personaltaskapp.repository.HabitRepository
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var habitViewModel: HabitViewModel
    private lateinit var calendarViewModel: CalendarViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -------------------------------
        // TaskViewModel (if yours already uses viewModels() internally, keep that)
        // -------------------------------
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        // -------------------------------
        // HabitViewModel (MANUAL construction)
        // -------------------------------
        val habitDb = HabitDatabaseHelper(this)
        val habitRepo = HabitRepository(habitDb)
        habitViewModel = HabitViewModel(habitRepo)

        // -------------------------------
        // CalendarViewModel
        // -------------------------------
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        // -------------------------------
        // Compose UI
        // -------------------------------
        setContent {
            MainApp(
                taskViewModel = taskViewModel,
                habitViewModel = habitViewModel,
                calendarViewModel = calendarViewModel
            )
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
    var selectedTab by remember { mutableStateOf(0) }

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
                    icon = { Icon(Icons.Default.DateRange, "Calendar") },
                    label = { Text("Calendar") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> TaskScreen(taskViewModel)
                1 -> HabitScreen(habitViewModel)
                2 -> CalendarScreen(calendarViewModel)
            }
        }
    }
}



