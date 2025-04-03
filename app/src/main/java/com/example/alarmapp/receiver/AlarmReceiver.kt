package com.example.alarmapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.alarmapp.data.AlarmDatabase
import com.example.alarmapp.data.AlarmRepository
import com.example.alarmapp.service.AlarmSchedulerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmSchedulerService.ALARM_ID, -1)
        val label = intent.getStringExtra(AlarmSchedulerService.ALARM_LABEL) ?: ""

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule all enabled alarms after device reboot
                handleBootCompleted(context)
            }
            AlarmSchedulerService.ACTION_SNOOZE -> {
                handleSnooze(context, alarmId)
            }
            AlarmSchedulerService.ACTION_DISMISS -> {
                handleDismiss(context, alarmId)
            }
            else -> {
                // Regular alarm trigger
                handleAlarmTrigger(context, alarmId, label)
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val repository = AlarmRepository(AlarmDatabase.getDatabase(context).alarmDao())
                val scheduler = AlarmSchedulerService(context)

                repository.getEnabledAlarms()
                    .onSuccess { alarms ->
                        alarms.forEach { alarm ->
                            scheduler.scheduleAlarm(alarm)
                        }
                        Log.d(TAG, "Rescheduled ${alarms.size} alarms after boot")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Failed to reschedule alarms after boot", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleBootCompleted", e)
            }
        }
    }

    private fun handleSnooze(context: Context, alarmId: Long) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val repository = AlarmRepository(AlarmDatabase.getDatabase(context).alarmDao())
                val scheduler = AlarmSchedulerService(context)

                repository.getAlarmById(alarmId)
                    .onSuccess { alarm ->
                        alarm?.let {
                            // Schedule a new alarm 10 minutes from now
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.MINUTE, 10)
                            
                            val snoozeAlarm = alarm.copy(
                                hour = calendar.get(Calendar.HOUR_OF_DAY),
                                minute = calendar.get(Calendar.MINUTE)
                            )
                            scheduler.scheduleAlarm(snoozeAlarm)
                            Log.d(TAG, "Alarm snoozed: $alarmId")
                        }
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Failed to snooze alarm", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleSnooze", e)
            }
        }
    }

    private fun handleDismiss(context: Context, alarmId: Long) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val repository = AlarmRepository(AlarmDatabase.getDatabase(context).alarmDao())
                
                repository.getAlarmById(alarmId)
                    .onSuccess { alarm ->
                        alarm?.let {
                            if (!alarm.hasRepeatDays()) {
                                // If it's a one-time alarm, disable it
                                repository.updateAlarmEnabled(alarmId, false)
                            }
                            Log.d(TAG, "Alarm dismissed: $alarmId")
                        }
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Failed to dismiss alarm", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleDismiss", e)
            }
        }
    }

    private fun handleAlarmTrigger(context: Context, alarmId: Long, label: String) {
        if (alarmId != -1L) {
            try {
                val scheduler = AlarmSchedulerService(context)
                scheduler.showAlarmNotification(alarmId, label)
                Log.d(TAG, "Alarm triggered: $alarmId")
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering alarm", e)
            }
        }
    }
}