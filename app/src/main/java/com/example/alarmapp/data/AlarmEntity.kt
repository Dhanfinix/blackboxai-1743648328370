package com.example.alarmapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Time in 24-hour format (HH:mm)
    val hour: Int,
    val minute: Int,
    
    // Alarm label/name
    val label: String = "",
    
    // Alarm enabled status
    val isEnabled: Boolean = true,
    
    // Repeat settings for each day of the week
    val repeatOnSunday: Boolean = false,
    val repeatOnMonday: Boolean = false,
    val repeatOnTuesday: Boolean = false,
    val repeatOnWednesday: Boolean = false,
    val repeatOnThursday: Boolean = false,
    val repeatOnFriday: Boolean = false,
    val repeatOnSaturday: Boolean = false,
    
    // Vibration and sound settings
    val vibrate: Boolean = true,
    val soundUri: String = "default",
    
    // Snooze settings
    val snoozeEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 10,
    
    // Creation timestamp
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper function to check if alarm repeats on a specific day
    fun repeatsOn(dayOfWeek: DayOfWeek): Boolean {
        return when (dayOfWeek) {
            DayOfWeek.SUNDAY -> repeatOnSunday
            DayOfWeek.MONDAY -> repeatOnMonday
            DayOfWeek.TUESDAY -> repeatOnTuesday
            DayOfWeek.WEDNESDAY -> repeatOnWednesday
            DayOfWeek.THURSDAY -> repeatOnThursday
            DayOfWeek.FRIDAY -> repeatOnFriday
            DayOfWeek.SATURDAY -> repeatOnSaturday
        }
    }

    // Helper function to check if alarm repeats on any day
    fun hasRepeatDays(): Boolean {
        return repeatOnSunday || repeatOnMonday || repeatOnTuesday || 
               repeatOnWednesday || repeatOnThursday || repeatOnFriday || 
               repeatOnSaturday
    }

    // Format time as string (e.g., "08:30")
    fun getTimeString(): String {
        return String.format("%02d:%02d", hour, minute)
    }

    // Get list of repeat days as string (e.g., "Mon, Wed, Fri")
    fun getRepeatDaysString(): String {
        val days = mutableListOf<String>()
        if (repeatOnSunday) days.add("Sun")
        if (repeatOnMonday) days.add("Mon")
        if (repeatOnTuesday) days.add("Tue")
        if (repeatOnWednesday) days.add("Wed")
        if (repeatOnThursday) days.add("Thu")
        if (repeatOnFriday) days.add("Fri")
        if (repeatOnSaturday) days.add("Sat")
        return if (days.isEmpty()) "Once" else days.joinToString(", ")
    }

    companion object {
        // Factory method to create a one-time alarm
        fun createOneTimeAlarm(hour: Int, minute: Int, label: String = ""): AlarmEntity {
            return AlarmEntity(
                hour = hour,
                minute = minute,
                label = label,
                isEnabled = true
            )
        }

        // Factory method to create a weekday alarm
        fun createWeekdayAlarm(hour: Int, minute: Int, label: String = ""): AlarmEntity {
            return AlarmEntity(
                hour = hour,
                minute = minute,
                label = label,
                isEnabled = true,
                repeatOnMonday = true,
                repeatOnTuesday = true,
                repeatOnWednesday = true,
                repeatOnThursday = true,
                repeatOnFriday = true
            )
        }

        // Factory method to create a weekend alarm
        fun createWeekendAlarm(hour: Int, minute: Int, label: String = ""): AlarmEntity {
            return AlarmEntity(
                hour = hour,
                minute = minute,
                label = label,
                isEnabled = true,
                repeatOnSaturday = true,
                repeatOnSunday = true
            )
        }
    }
}