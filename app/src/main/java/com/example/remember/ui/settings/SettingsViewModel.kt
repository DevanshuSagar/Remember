package com.example.remember.ui.settings

import android.content.Context
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.remember.data.Repository
import com.example.remember.data.db.Settings
import com.example.remember.data.db.Theme
import com.example.remember.workers.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.min

data class SettingsState(
    val settings: Settings? = null,
    val intervalsError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSettings().collect { settingsFromDb ->
                if (settingsFromDb == null) {
                    val defaultSettings = Settings()
                    repository.saveSettings(defaultSettings)
                    _uiState.update { it.copy(settings = defaultSettings) }
                } else {
                    _uiState.update { it.copy(settings = settingsFromDb) }
                }
            }
        }
    }

//    fun changeIntervalsToDefaults() {
//        val defaultIntervals = "1,3,7,21"
//
//        _uiState.update { currentState ->
//            currentState.copy(settings = currentState.settings?.copy(globalIntervals = defaultIntervals))
//        }
//        _uiState.update { currentState ->
//            currentState.copy(intervalsError = null)
//        }
//        viewModelScope.launch {
//            uiState.value.settings?.let { repository.saveSettings(it) }
//        }
//    }

//    fun onGlobalIntervalsChanged(intervals: String) {
//        _uiState.update { currentState ->
//            currentState.copy(
//                settings = currentState.settings?.copy(globalIntervals = intervals)
//            )
//        }
//
//        val individualIntervals = intervals.split(',').filter { it.isNotBlank() }
//        val areValid = individualIntervals.all { it.trim().toIntOrNull() != null }
//
//        if (areValid && individualIntervals.isNotEmpty()) {
//            _uiState.update { it.copy(intervalsError = null) }
//            viewModelScope.launch {
//                uiState.value.settings?.let {
//                    repository.saveSettings(it)
//                }
//            }
//        } else if (individualIntervals.isEmpty()) {
//            _uiState.update { it.copy(intervalsError = "Invalid format. This field cannot be empty.") }
//        } else {
//            _uiState.update { it.copy(intervalsError = "Invalid format. Use numbers separated by commas.") }
//        }
//    }

    fun onNotificationTimeChanged(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            val totalMinute = 60 * hour + minute
            val currentSettings = uiState.value.settings
            if (currentSettings != null) {
                repository.saveSettings(currentSettings.copy(notificationTimeMinutes = totalMinute))
                scheduleDailyNotification(context, hour, minute)
            }
        }
    }

    fun scheduleDailyNotification(context: Context, hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)

        val now = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (scheduledTime.before(now)) {
            scheduledTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = scheduledTime.timeInMillis - now.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_Reminder_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    fun onThemeChanged(theme: Theme) {
        viewModelScope.launch {
            uiState.value.settings?.let { currentSettings->
                repository.saveSettings(currentSettings.copy(theme = theme))
            }
        }
    }
}