package com.example.remember.ui.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remember.data.db.Theme
import java.util.Locale

@Composable
fun SettingsScreen(
//    childNavController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val settings = uiState.settings

    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        if (settings != null) {
            ThemeDialog(
                currentTheme = settings.theme,
                onThemeSelected = {
                    viewModel.onThemeChanged(it)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }
    }

    if (settings == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            val totalMinutes = settings.notificationTimeMinutes
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val formattedTime = String.format(Locale.US, "%02d:%02d", hours, minutes)

            val timePickerDialog = TimePickerDialog(
                context,
                { _, selectedHour: Int, selectedMinute: Int ->
                    viewModel.onNotificationTimeChanged(
                        context,
                        selectedHour,
                        selectedMinute
                    )
                },
                hours,
                minutes,
                true
            )

            SettingItem(
                icon = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notification Time"
                    )
                },
                title = "Daily Notification Time",
                subtitle = "Receive a reminder at $formattedTime",
                onClick = { timePickerDialog.show() }
            )

            HorizontalDivider()

            SettingItem(
                icon = { /* Add an icon if you like, e.g., Icons.Default.Palette */ },
                title = "App Theme",
                subtitle = "Current: ${settings.theme.name.replaceFirstChar { it.titlecase(Locale.ROOT) }}",
                onClick = { showThemeDialog = true }
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ThemeDialog(
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                Theme.entries.forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == currentTheme),
                                onClick = { onThemeSelected(theme) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(theme.name.lowercase().replaceFirstChar { it.titlecase(Locale.ROOT) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}