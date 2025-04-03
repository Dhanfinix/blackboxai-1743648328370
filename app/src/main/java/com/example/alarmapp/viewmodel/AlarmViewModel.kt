package com.example.alarmapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alarmapp.data.AlarmEntity
import com.example.alarmapp.data.AlarmRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime

data class AlarmUiState(
    val alarms: List<AlarmEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // For alarm creation/editing
    val selectedHour: Int = LocalTime.now().hour,
    val selectedMinute: Int = LocalTime.now().minute,
    val selectedLabel: String = "",
    val isEditing: Boolean = false,
    val currentEditingAlarmId: Long? = null
)

sealed class AlarmEvent {
    data class ShowSnackbar(val message: String) : AlarmEvent()
    data class NavigateToEdit(val alarmId: Long?) : AlarmEvent()
    object NavigateBack : AlarmEvent()
}

class AlarmViewModel(private val repository: AlarmRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AlarmEvent>()
    val events: SharedFlow<AlarmEvent> = _events.asSharedFlow()

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.allAlarms.collect { alarms ->
                _uiState.update { 
                    it.copy(
                        alarms = alarms,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createAlarm(hour: Int, minute: Int, label: String = "") {
        viewModelScope.launch {
            val alarm = AlarmEntity.createOneTimeAlarm(hour, minute, label)
            repository.insertAlarm(alarm)
                .onSuccess { 
                    emitEvent(AlarmEvent.ShowSnackbar("Alarm created successfully"))
                    emitEvent(AlarmEvent.NavigateBack)
                }
                .onFailure { 
                    _uiState.update { it.copy(error = "Failed to create alarm") }
                }
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
                .onSuccess {
                    emitEvent(AlarmEvent.ShowSnackbar("Alarm updated successfully"))
                    emitEvent(AlarmEvent.NavigateBack)
                }
                .onFailure {
                    _uiState.update { it.copy(error = "Failed to update alarm") }
                }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
                .onSuccess {
                    emitEvent(AlarmEvent.ShowSnackbar("Alarm deleted"))
                }
                .onFailure {
                    _uiState.update { it.copy(error = "Failed to delete alarm") }
                }
        }
    }

    fun toggleAlarmEnabled(alarmId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateAlarmEnabled(alarmId, isEnabled)
                .onSuccess {
                    val message = if (isEnabled) "Alarm enabled" else "Alarm disabled"
                    emitEvent(AlarmEvent.ShowSnackbar(message))
                }
                .onFailure {
                    _uiState.update { it.copy(error = "Failed to update alarm status") }
                }
        }
    }

    fun startEditingAlarm(alarmId: Long) {
        viewModelScope.launch {
            repository.getAlarmById(alarmId)
                .onSuccess { alarm ->
                    alarm?.let {
                        _uiState.update { state ->
                            state.copy(
                                selectedHour = alarm.hour,
                                selectedMinute = alarm.minute,
                                selectedLabel = alarm.label,
                                isEditing = true,
                                currentEditingAlarmId = alarmId
                            )
                        }
                        emitEvent(AlarmEvent.NavigateToEdit(alarmId))
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(error = "Failed to load alarm") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun emitEvent(event: AlarmEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    class Factory(private val repository: AlarmRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
                return AlarmViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}