package com.example.personaltaskapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.personaltaskapp.data.DatabaseModule
import com.example.personaltaskapp.repository.AuthManager
import com.example.personaltaskapp.repository.CalendarRepository
import com.example.personaltaskapp.repository.HabitRepository
import com.example.personaltaskapp.repository.TaskRepository
import com.example.personaltaskapp.viewmodel.CalendarViewModel
import com.example.personaltaskapp.viewmodel.CalendarViewModelFactory
import com.example.personaltaskapp.viewmodel.HabitViewModel
import com.example.personaltaskapp.viewmodel.HabitViewModelFactory
import com.example.personaltaskapp.viewmodel.TaskViewModel
import com.example.personaltaskapp.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DatabaseModule.getDatabase(this)
        val authManager = AuthManager(this)

        val taskRepo = TaskRepository(db.taskDao())
        val habitRepo = HabitRepository(db.habitDao())
        val calendarRepo = CalendarRepository(db.calendarEventDao())

        val taskViewModel: TaskViewModel = ViewModelProvider(
            this,
            TaskViewModelFactory(taskRepo)
        )[TaskViewModel::class.java]

        val habitViewModel: HabitViewModel = ViewModelProvider(
            this,
            HabitViewModelFactory(habitRepo)
        )[HabitViewModel::class.java]

        val calendarViewModel: CalendarViewModel = ViewModelProvider(
            this,
            CalendarViewModelFactory(calendarRepo, taskRepo, habitRepo)
        )[CalendarViewModel::class.java]

        setContent {
            MainApp(
                authManager = authManager,
                taskViewModel = taskViewModel,
                habitViewModel = habitViewModel,
                calendarViewModel = calendarViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    authManager: AuthManager,
    taskViewModel: TaskViewModel,
    habitViewModel: HabitViewModel,
    calendarViewModel: CalendarViewModel
) {
    var isLoggedIn by remember { mutableStateOf(authManager.isLoggedIn()) }
    var showRegister by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        if (showRegister) {
            RegisterScreen(
                authManager = authManager,
                onRegisterSuccess = {
                    isLoggedIn = true
                    showRegister = false
                },
                onGoToLogin = { showRegister = false }
            )
        } else {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = { isLoggedIn = true },
                onGoToRegister = { showRegister = true }
            )
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PersonalTaskApp") },
                actions = {
                    TextButton(onClick = {
                        authManager.logout()
                        isLoggedIn = false
                    }) {
                        Text("Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
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