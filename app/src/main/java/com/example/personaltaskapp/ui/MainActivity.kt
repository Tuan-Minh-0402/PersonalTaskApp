package com.example.personaltaskapp.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaltaskapp.data.DatabaseModule
import com.example.personaltaskapp.repository.CalendarRepository
import com.example.personaltaskapp.repository.HabitRepository
import com.example.personaltaskapp.repository.TaskRepository
import com.example.personaltaskapp.viewmodel.CalendarViewModel
import com.example.personaltaskapp.viewmodel.CalendarViewModelFactory
import com.example.personaltaskapp.viewmodel.HabitViewModel
import com.example.personaltaskapp.viewmodel.HabitViewModelFactory
import com.example.personaltaskapp.viewmodel.PomodoroManager
import com.example.personaltaskapp.viewmodel.TaskViewModel
import com.example.personaltaskapp.viewmodel.TaskViewModelFactory

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Database instance (singleton) ---
        val db = DatabaseModule.getDatabase(this)

        // --- Repositories ---
        val taskRepo = TaskRepository(db.taskDao())
        val habitRepo = HabitRepository(db.habitDao())
        val calendarRepo = CalendarRepository(db.calendarEventDao())

        // --- ViewModels that need factories (constructed here) ---
        val taskViewModel: TaskViewModel = ViewModelProvider(
            this,
            TaskViewModelFactory(taskRepo)
        ).get(TaskViewModel::class.java)

        val habitViewModel: HabitViewModel = ViewModelProvider(
            this,
            HabitViewModelFactory(habitRepo)
        ).get(HabitViewModel::class.java)

        val calendarViewModel: CalendarViewModel = ViewModelProvider(
            this,
            CalendarViewModelFactory(calendarRepo, taskRepo, habitRepo)
        ).get(CalendarViewModel::class.java)

        // --- Compose UI ---
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
