package com.example.alarmapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.alarmapp.data.AlarmDatabase
import com.example.alarmapp.data.AlarmRepository
import com.example.alarmapp.service.AlarmSchedulerService
import com.example.alarmapp.ui.screens.AlarmDetailScreen
import com.example.alarmapp.ui.screens.AlarmListScreen
import com.example.alarmapp.ui.theme.AlarmAppTheme
import com.example.alarmapp.viewmodel.AlarmViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var alarmSchedulerService: AlarmSchedulerService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize services
        alarmSchedulerService = AlarmSchedulerService(applicationContext)

        // Initialize database and repository
        val database = AlarmDatabase.getDatabase(applicationContext)
        val repository = AlarmRepository.getInstance(database.alarmDao())

        setContent {
            AlarmAppTheme {
                AlarmApp(
                    repository = repository,
                    alarmSchedulerService = alarmSchedulerService
                )
            }
        }
    }
}

@Composable
fun AlarmApp(
    repository: AlarmRepository,
    alarmSchedulerService: AlarmSchedulerService
) {
    val navController = rememberNavController()
    val viewModel: AlarmViewModel = viewModel(
        factory = AlarmViewModel.Factory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Handle one-time events
    LaunchedEffect(true) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AlarmEvent.ShowSnackbar -> {
                    // Handle snackbar (already handled in screens)
                }
                is AlarmEvent.NavigateToEdit -> {
                    navController.navigate("alarm/edit/${event.alarmId ?: -1}")
                }
                is AlarmEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "alarm/list"
    ) {
        composable("alarm/list") {
            AlarmListScreen(
                uiState = uiState,
                onAddAlarm = {
                    navController.navigate("alarm/add")
                },
                onEditAlarm = { alarmId ->
                    navController.navigate("alarm/edit/$alarmId")
                },
                onDeleteAlarm = { alarm ->
                    scope.launch {
                        viewModel.deleteAlarm(alarm)
                        alarmSchedulerService.cancelAlarm(alarm.id)
                    }
                },
                onToggleAlarm = { alarmId, isEnabled ->
                    scope.launch {
                        viewModel.toggleAlarmEnabled(alarmId, isEnabled)
                        if (isEnabled) {
                            viewModel.getAlarmById(alarmId)?.let { alarm ->
                                alarmSchedulerService.scheduleAlarm(alarm)
                            }
                        } else {
                            alarmSchedulerService.cancelAlarm(alarmId)
                        }
                    }
                }
            )
        }

        composable(
            route = "alarm/edit/{alarmId}",
            arguments = listOf(
                navArgument("alarmId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
            val alarm = if (alarmId != -1L) {
                remember(alarmId) {
                    runBlocking {
                        viewModel.getAlarmById(alarmId)
                    }
                }
            } else null

            AlarmDetailScreen(
                alarm = alarm,
                onSave = { newAlarm ->
                    scope.launch {
                        if (alarm == null) {
                            viewModel.createAlarm(
                                newAlarm.hour,
                                newAlarm.minute,
                                newAlarm.label
                            )
                        } else {
                            viewModel.updateAlarm(newAlarm)
                        }
                        alarmSchedulerService.scheduleAlarm(newAlarm)
                        navController.popBackStack()
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("alarm/add") {
            AlarmDetailScreen(
                alarm = null,
                onSave = { newAlarm ->
                    scope.launch {
                        viewModel.createAlarm(
                            newAlarm.hour,
                            newAlarm.minute,
                            newAlarm.label
                        )
                        alarmSchedulerService.scheduleAlarm(newAlarm)
                        navController.popBackStack()
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}