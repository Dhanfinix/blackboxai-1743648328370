package com.example.alarmapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException

class AlarmRepository(private val alarmDao: AlarmDao) {
    
    // Get all alarms as a Flow
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    // Insert a new alarm
    suspend fun insertAlarm(alarm: AlarmEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = alarmDao.insertAlarm(alarm)
            Result.success(id)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Update an existing alarm
    suspend fun updateAlarm(alarm: AlarmEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            alarmDao.updateAlarm(alarm)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Delete an alarm
    suspend fun deleteAlarm(alarm: AlarmEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            alarmDao.deleteAlarm(alarm)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Get a specific alarm by ID
    suspend fun getAlarmById(alarmId: Long): Result<AlarmEntity?> = withContext(Dispatchers.IO) {
        try {
            val alarm = alarmDao.getAlarmById(alarmId)
            Result.success(alarm)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Update alarm enabled status
    suspend fun updateAlarmEnabled(alarmId: Long, isEnabled: Boolean): Result<Unit> = 
        withContext(Dispatchers.IO) {
            try {
                alarmDao.updateAlarmEnabled(alarmId, isEnabled)
                Result.success(Unit)
            } catch (e: IOException) {
                Result.failure(e)
            }
        }

    // Get all enabled alarms
    suspend fun getEnabledAlarms(): Result<List<AlarmEntity>> = withContext(Dispatchers.IO) {
        try {
            val alarms = alarmDao.getEnabledAlarms()
            Result.success(alarms)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Delete alarm by ID
    suspend fun deleteAlarmById(alarmId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            alarmDao.deleteAlarmById(alarmId)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Get total alarm count
    suspend fun getAlarmCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val count = alarmDao.getAlarmCount()
            Result.success(count)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AlarmRepository? = null

        fun getInstance(alarmDao: AlarmDao): AlarmRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AlarmRepository(alarmDao)
                INSTANCE = instance
                instance
            }
        }
    }
}